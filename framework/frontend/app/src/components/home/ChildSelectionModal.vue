<template>
  <NubiInfoModal
    :model-value="modelValue"
    :title="t('views.home.childSelectionTitle')"
    @update:model-value="$emit('update:modelValue', $event)"
    @close="$emit('close')"
  >
    <div class="child-selection-modal__content">
      <p>{{ t('views.home.childSelectionPlaceholder') }}</p>
    </div>
  </NubiInfoModal>
</template>

<script setup lang="ts">
/**
 * ChildSelectionModal - Modal de seleccion/alta de ninos (estructura basica)
 * 
 * Segun FEAT-002 y SPRINT-008:
 * - Solo estructura basica: abrir/cerrar
 * - No implementa seleccion, formularios ni validaciones
 * - El contenido se completara en sprints futuros
 * - Usa NubiInfoModal como base
 */

import { useI18n } from 'vue-i18n'
import NubiInfoModal from '../base/NubiInfoModal.vue'

interface Props {
  /** Controlar visibilidad del modal (v-model) */
  modelValue: boolean
}

defineProps<Props>()

defineEmits<{
  'update:modelValue': [value: boolean]
  close: []
}>()

const { t } = useI18n()
</script>

<style scoped>
.child-selection-modal__content {
  padding: var(--nubi-spacing-md) 0;
}

.child-selection-modal__content p {
  font-size: var(--nubi-font-size-base);
  color: var(--nubi-text-secondary);
  line-height: var(--nubi-line-height-relaxed);
  margin: 0;
}
</style>
