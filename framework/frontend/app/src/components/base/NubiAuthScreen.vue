<template>
  <div class="nubi-auth-screen">
    <div class="nubi-auth-screen__container">
      <div class="nubi-auth-screen__header">
        <h1 class="nubi-auth-screen__title">{{ t('components.authScreen.title') }}</h1>
        <p class="nubi-auth-screen__subtitle">{{ t('components.authScreen.subtitle') }}</p>
      </div>
      
      <div class="nubi-auth-screen__content">
        <NubiPinInput
          v-model="pin"
          :label="t('components.pinInput.label')"
          :masked="true"
          :error="errorMessage"
          @complete="handlePinComplete"
        />
        
        <div v-if="isValidating" class="nubi-auth-screen__validating">
          <NubiSpinner size="sm" />
          <span>{{ t('components.authScreen.validating') }}</span>
        </div>
      </div>
      
      <div class="nubi-auth-screen__footer">
        <NubiButton variant="secondary" @click="handleForgot">
          {{ t('components.authScreen.forgot') }}
        </NubiButton>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * NubiAuthScreen - Pantalla de autenticación con PIN
 * 
 * Características:
 * - Integración con NubiPinInput
 * - Validación de PIN (mock o real)
 * - Mensaje de error tras fallo
 * - Estado de validación con spinner
 * - Botón "¿Olvidaste tu PIN?"
 */

import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import NubiPinInput from './NubiPinInput.vue'
import NubiButton from './NubiButton.vue'
import NubiSpinner from './NubiSpinner.vue'

interface Props {
  /** PIN esperado (para validación local) */
  expectedPin?: string
  /** Función de validación personalizada */
  validatePin?: (pin: string) => Promise<boolean>
}

const props = withDefaults(defineProps<Props>(), {
  expectedPin: '1234',
  validatePin: undefined
})

const emit = defineEmits<{
  success: [pin: string]
  error: [pin: string]
  forgot: []
}>()

const { t } = useI18n()

const pin = ref('')
const errorMessage = ref('')
const isValidating = ref(false)

async function handlePinComplete(completedPin: string) {
  errorMessage.value = ''
  isValidating.value = true
  
  try {
    let isValid = false
    
    if (props.validatePin) {
      isValid = await props.validatePin(completedPin)
    } else {
      // Validación local (mock)
      isValid = completedPin === props.expectedPin
    }
    
    if (isValid) {
      emit('success', completedPin)
    } else {
      errorMessage.value = t('components.authScreen.error')
      pin.value = ''
      emit('error', completedPin)
    }
  } catch (error) {
    errorMessage.value = t('components.authScreen.error')
    pin.value = ''
    emit('error', completedPin)
  } finally {
    isValidating.value = false
  }
}

function handleForgot() {
  emit('forgot')
}
</script>

<style scoped>
.nubi-auth-screen {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: var(--nubi-spacing-lg);
  background-color: var(--nubi-bg-surface-secondary);
}

.nubi-auth-screen__container {
  width: 100%;
  max-width: 400px;
  background-color: var(--nubi-bg-surface);
  border-radius: var(--nubi-radius-xl);
  padding: var(--nubi-spacing-2xl);
  box-shadow: var(--nubi-shadow-lg);
}

.nubi-auth-screen__header {
  text-align: center;
  margin-bottom: var(--nubi-spacing-xl);
}

.nubi-auth-screen__title {
  font-size: var(--nubi-font-size-2xl);
  font-weight: var(--nubi-font-weight-bold);
  color: var(--nubi-text-primary);
  margin: 0 0 var(--nubi-spacing-sm) 0;
}

.nubi-auth-screen__subtitle {
  font-size: var(--nubi-font-size-base);
  color: var(--nubi-text-secondary);
  margin: 0;
}

.nubi-auth-screen__content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--nubi-spacing-lg);
  margin-bottom: var(--nubi-spacing-xl);
}

.nubi-auth-screen__validating {
  display: flex;
  align-items: center;
  gap: var(--nubi-spacing-sm);
  color: var(--nubi-text-secondary);
  font-size: var(--nubi-font-size-sm);
}

.nubi-auth-screen__footer {
  display: flex;
  justify-content: center;
}
</style>
