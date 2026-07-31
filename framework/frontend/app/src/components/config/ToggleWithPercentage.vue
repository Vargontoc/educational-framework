<template>
  <div class="toggle-with-percentage" :class="{ 'toggle-with-percentage--disabled': disabled }">
    <div class="toggle-with-percentage__header">
      <label :id="labelId" class="toggle-with-percentage__label">{{ label }}</label>
      <NubiToggle
        :model-value="modelEnabled"
        @update:model-value="onToggleChange"
        :disabled="disabled"
        :aria-label="`${label} - Activar o desactivar`"
      />
    </div>
    <div v-if="modelEnabled" class="toggle-with-percentage__controls">
      <input
        type="range"
        :min="min"
        :max="max"
        :step="step"
        :value="modelPercentage"
        @input="onSliderChange"
        :aria-label="`${label} - Ajustar porcentaje`"
        :aria-valuenow="modelPercentage"
        :aria-valuemin="min"
        :aria-valuemax="max"
        class="toggle-with-percentage__slider"
        :disabled="disabled"
      />
      <NubiNumberInput
        :model-value="modelPercentage"
        @update:model-value="onNumberChange"
        :min="min"
        :max="max"
        :step="step"
        :disabled="disabled"
        :aria-label="`${label} - Valor numérico`"
        class="toggle-with-percentage__input"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * ToggleWithPercentage - Componente que combina toggle on/off con control de porcentaje
 * 
 * Características:
 * - Toggle on/off con NubiToggle
 * - Slider nativo para ajuste visual
 * - NubiNumberInput para ajuste preciso
 * - Acción rápida: establecer porcentaje a 0 apaga el toggle
 * - Sincronización bidireccional entre slider e input
 * 
 * Accesibilidad:
 * - Labels asociados mediante aria-labelledby
 * - Slider con aria-valuenow, aria-valuemin, aria-valuemax
 * - Objetivos táctiles ≥ 48dp
 */

import { useId } from 'vue'
import NubiToggle from '../base/NubiToggle.vue'
import NubiNumberInput from '../base/NubiNumberInput.vue'

interface Props {
  /** Estado del toggle (v-model:enabled) */
  modelEnabled: boolean
  /** Valor del porcentaje (v-model:percentage) */
  modelPercentage: number
  /** Label descriptivo */
  label: string
  /** Estado disabled */
  disabled?: boolean
  /** Valor mínimo */
  min?: number
  /** Valor máximo */
  max?: number
  /** Paso de incremento */
  step?: number
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false,
  min: 0,
  max: 100,
  step: 1
})

const emit = defineEmits<{
  'update:enabled': [value: boolean]
  'update:percentage': [value: number]
}>()

const uniqueId = useId()
const labelId = `toggle-percentage-label-${uniqueId}`

function onToggleChange(enabled: boolean) {
  emit('update:enabled', enabled)
}

function onSliderChange(event: Event) {
  const value = Number((event.target as HTMLInputElement).value)
  emit('update:percentage', value)
  if (value === 0) {
    emit('update:enabled', false)
  }
}

function onNumberChange(value: number | null) {
  if (value === null) return
  emit('update:percentage', value)
  if (value === 0) {
    emit('update:enabled', false)
  }
}
</script>

<style scoped>
.toggle-with-percentage {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-sm);
}

.toggle-with-percentage--disabled {
  opacity: 0.5;
  pointer-events: none;
}

.toggle-with-percentage__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--nubi-spacing-sm);
}

.toggle-with-percentage__label {
  font-size: var(--nubi-font-size-base);
  font-weight: var(--nubi-font-weight-medium);
  color: var(--nubi-text-primary);
  flex: 1;
}

.toggle-with-percentage__controls {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-sm);
  padding-left: var(--nubi-spacing-md);
}

.toggle-with-percentage__slider {
  width: 100%;
  height: 8px;
  border-radius: var(--nubi-radius-full);
  background: var(--nubi-bg-surface-secondary);
  outline: none;
  -webkit-appearance: none;
  appearance: none;
  cursor: pointer;
  min-height: 48px;
  padding: 20px 0;
  background-clip: content-box;
}

.toggle-with-percentage__slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 24px;
  height: 24px;
  border-radius: var(--nubi-radius-full);
  background: var(--nubi-color-primary);
  cursor: pointer;
  box-shadow: var(--nubi-shadow-sm);
  transition: transform var(--nubi-duration-fast) var(--nubi-ease-in-out);
}

.toggle-with-percentage__slider::-webkit-slider-thumb:hover {
  transform: scale(1.1);
}

.toggle-with-percentage__slider::-moz-range-thumb {
  width: 24px;
  height: 24px;
  border-radius: var(--nubi-radius-full);
  background: var(--nubi-color-primary);
  cursor: pointer;
  border: none;
  box-shadow: var(--nubi-shadow-sm);
}

.toggle-with-percentage__slider:focus-visible {
  box-shadow: 0 0 0 3px var(--nubi-color-focus);
}

.toggle-with-percentage__slider:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.toggle-with-percentage__input {
  max-width: 140px;
}
</style>
