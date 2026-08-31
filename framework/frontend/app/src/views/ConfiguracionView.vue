<template>
  <div class="configuracion-view">

    <div v-if="loading" class="configuracion-view__loading">
      <NubiSpinner size="lg" />
    </div>

    <div v-else class="configuracion-view__sections">
      <NubiBreadcrumb :items="breadcrumbItems" />

      <!-- Sección 1: Audio general -->
      <ConfigSection
        :title="t('views.configuracion.sections.audioGeneral.title')"
        :description="t('views.configuracion.sections.audioGeneral.description')"
      >
        <ToggleWithPercentage
          :model-enabled="draft.audioGeneralEnabled"
          :model-percentage="draft.audioGeneralVolume"
          :label="t('views.configuracion.sections.audioGeneral.toggleLabel')"
          @update:enabled="(val) => onToggleChange('audioGeneral', val)"
          @update:percentage="(val) => onPercentageChange('audioGeneral', val)"
        />
      </ConfigSection>

      <!-- Sección 2: NPC -->
      <ConfigSection
        :title="t('views.configuracion.sections.npc.title')"
        :description="t('views.configuracion.sections.npc.description')"
      >
        <NubiToggle
          :model-value="draft.npcEnabled"
          @update:model-value="(val) => onToggleOnlyChange('npc', val)"
          :label="t('views.configuracion.sections.npc.toggleLabel')"
        />
      </ConfigSection>

      <!-- Sección 3: Voz del NPC -->
      <ConfigSection
        :title="t('views.configuracion.sections.npcVoice.title')"
        :description="t('views.configuracion.sections.npcVoice.description')"
      >
        <ToggleWithPercentage
          :model-enabled="draft.npcVoiceEnabled"
          :model-percentage="draft.npcVoiceVolume"
          :label="t('views.configuracion.sections.npcVoice.toggleLabel')"
          @update:enabled="(val) => onToggleChange('npcVoice', val)"
          @update:percentage="(val) => onPercentageChange('npcVoice', val)"
        />
      </ConfigSection>

      <!-- Sección 4: Voz narrativa -->
      <ConfigSection
        :title="t('views.configuracion.sections.narrativeVoice.title')"
        :description="t('views.configuracion.sections.narrativeVoice.description')"
      >
        <ToggleWithPercentage
          :model-enabled="draft.narrativeVoiceEnabled"
          :model-percentage="draft.narrativeVoiceVolume"
          :label="t('views.configuracion.sections.narrativeVoice.toggleLabel')"
          @update:enabled="(val) => onToggleChange('narrativeVoice', val)"
          @update:percentage="(val) => onPercentageChange('narrativeVoice', val)"
        />
      </ConfigSection>

      <!-- Sección 5: PIN familiar -->
      <ConfigSection
        :title="t('views.configuracion.sections.pin.title')"
        :description="t('views.configuracion.sections.pin.description')"
      >
        <div class="configuracion-view__pin-inputs">
          <NubiPinInput
            v-model="pinNew"
            :label="t('views.configuracion.sections.pin.newPinLabel')"
            :masked="true"
            :pin-length="4"
          />
          <NubiPinInput
            v-model="pinConfirm"
            :label="t('views.configuracion.sections.pin.confirmPinLabel')"
            :masked="true"
            :pin-length="4"
            :error="pinMismatch ? t('views.configuracion.sections.pin.mismatchError') : ''"
          />
        </div>
      </ConfigSection>

      <!-- Acción final -->
      <div class="configuracion-view__actions">
        <NubiButton
          @click="handleSave"
          :disabled="!hasAnyChanges || saving"
          :loading="saving"
        >
          {{ t('views.configuracion.saveButton') }}
        </NubiButton>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * ConfiguracionView - Vista de configuración global familiar
 * 
 * Según FEAT-005 y SPRINT-025:
 * - 5 secciones: Audio general, NPC, Voz del NPC, Voz narrativa, PIN familiar
 * - Integración con API (GET para cargar, PATCH para guardar)
 * - Validación de PIN (4 dígitos, confirmación, coincidencia)
 * - Acción única «Guardar cambios»
 * - Logout automático tras cambio de PIN exitoso
 * 
 * Privacidad infantil:
 * - No muestra datos de niños, progreso ni clasificaciones
 * - Exclusiva para adultos autenticados
 */

import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

import NubiSpinner from '../components/base/NubiSpinner.vue'
import NubiToggle from '../components/base/NubiToggle.vue'
import NubiPinInput from '../components/base/NubiPinInput.vue'
import NubiButton from '../components/base/NubiButton.vue'
import ConfigSection from '../components/config/ConfigSection.vue'
import ToggleWithPercentage from '../components/config/ToggleWithPercentage.vue'
import NubiBreadcrumb from '@/components/base/NubiBreadcrumb.vue'

import { useGlobalConfig } from '../composables/useGlobalConfig'
import { useParentalSession } from '../composables/useParentalSession'
import { useToast } from '../composables/useToast'
import { getFamily, updateFamilyConfig } from '../services/familyService'
import type { FamilyUpdatePayload } from '../types/family-config'

const { t } = useI18n()
const router = useRouter()
const toast = useToast()
const { logout } = useParentalSession()

// Estado de configuración global
const {
  draft,
  hasChanges,
  initialize,
  onToggleChange,
  onToggleOnlyChange,
  onPercentageChange,
  getModifiedFields
} = useGlobalConfig()

const breadcrumbItems = computed(() => [
  { label: t('views.panel.title'), to: '/panel' },
  { label: t('views.experiencie.familiar.lecture') }
])

// Estado de la vista
const loading = ref(true)
const saving = ref(false)

// Estado del PIN
const pinNew = ref('')
const pinConfirm = ref('')

// Validación de PIN
const pinMismatch = computed(() => {
  return (
    pinNew.value.length === 4 &&
    pinConfirm.value.length === 4 &&
    pinNew.value !== pinConfirm.value
  )
})

// Verificar si hay cambios de PIN válidos
const hasValidPinChange = computed(() => {
  return (
    pinNew.value.length === 4 &&
    pinConfirm.value.length === 4 &&
    pinNew.value === pinConfirm.value
  )
})

// Verificar si hay algún cambio (config o PIN)
const hasAnyChanges = computed(() => {
  return hasChanges.value || hasValidPinChange.value
})

/**
 * Carga los datos de familia desde la API
 */
async function loadFamilyData() {
  loading.value = true
  try {
    const familyData = await getFamily()
    if (familyData) {
      initialize(familyData)
    }
  } catch (error) {
    toast.error(t('views.configuracion.saveError'))
  } finally {
    loading.value = false
  }
}

/**
 * Maneja el guardado de configuración
 */
async function handleSave() {
  // Validar PIN si hay campos rellenados
  if (pinNew.value.length === 4 && pinConfirm.value.length === 4) {
    if (pinNew.value !== pinConfirm.value) {
      toast.error(t('views.configuracion.sections.pin.mismatchError'))
      return
    }
  }

  // Construir payload
  const modifiedFields = getModifiedFields()
  const payload: FamilyUpdatePayload = { ...modifiedFields }

  // Incluir PIN si es válido
  if (hasValidPinChange.value) {
    payload.pin = pinNew.value
  }

  // Verificar si hay cambios
  if (Object.keys(payload).length === 0) {
    return
  }

  // Enviar PATCH
  saving.value = true
  try {
    const result = await updateFamilyConfig(payload)

    // Éxito
    if (hasValidPinChange.value) {
      // Cambio de PIN → logout + redirect
      toast.info(t('views.configuracion.pinChangedLogout'))
      await logout()
      router.replace({ name: 'Home' })
    } else {
      // Sin cambio de PIN → toast éxito + actualizar persisted
      toast.success(t('views.configuracion.saveSuccess'))
      initialize(result)
      // Limpiar campos PIN
      pinNew.value = ''
      pinConfirm.value = ''
    }
  } catch (error) {
    // Manejo de errores
    const apiError = error as { status?: number }
    if (apiError.status === 0) {
      toast.error(t('errors.networkError'))
    } else if (apiError.status === 400) {
      toast.error(t('errors.validationError'))
    } else if (apiError.status === 401) {
      // Token expirado → logout automático
      await logout()
      router.replace({ name: 'Home' })
    } else {
      toast.error(t('errors.genericError'))
    }
  } finally {
    saving.value = false
  }
}

// Cargar datos al montar
onMounted(() => {
  loadFamilyData()
})
</script>

<style scoped>
.configuracion-view {
  max-width: 720px;
  margin: 0 auto;
  padding: var(--nubi-spacing-lg);
  overflow-y: auto;
}

.configuracion-view__header {
  margin-bottom: var(--nubi-spacing-lg);
}

.configuracion-view__title {
  font-size: var(--nubi-font-size-2xl);
  font-weight: var(--nubi-font-weight-bold);
  color: var(--nubi-text-primary);
  margin: 0;
}

.configuracion-view__loading {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

.configuracion-view__sections {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-md);
}

.configuracion-view__pin-inputs {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-md);
}

.configuracion-view__actions {
  display: flex;
  justify-content: flex-end;
  padding-top: var(--nubi-spacing-md);
  border-top: 1px solid var(--nubi-border-default);
}

/* Responsive */
@media (max-width: 640px) {
  .configuracion-view {
    padding: var(--nubi-spacing-md);
  }

  .configuracion-view__title {
    font-size: var(--nubi-font-size-xl);
  }
}
</style>
