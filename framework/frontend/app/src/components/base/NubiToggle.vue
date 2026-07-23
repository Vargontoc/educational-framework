<template>
  <label :class="['nubi-toggle', { 'nubi-toggle--checked': modelValue, 'nubi-toggle--disabled': disabled }]">
    <input
      ref="inputRef"
      type="checkbox"
      role="switch"
      :checked="modelValue"
      :disabled="disabled"
      :aria-checked="modelValue"
      :aria-label="ariaLabelText"
      class="nubi-toggle__input"
      @change="handleChange"
    />
    
    <span class="nubi-toggle__track" aria-hidden="true">
      <span class="nubi-toggle__thumb" />
    </span>
    
    <span v-if="label || $slots.default" class="nubi-toggle__label">
      <slot>{{ label }}</slot>
    </span>
    
    <span v-if="showStatus" class="nubi-toggle__status" aria-hidden="true">
      {{ modelValue ? t('common.on') : t('common.off') }}
    </span>
  </label>
</template>

<script setup lang="ts">
/**
 * NubiToggle - Interruptor on/off
 * 
 * Características:
 * - Alternativa visual on/off para configuraciones frecuentes
 * - Animación suave (200ms)
 * - Soporte v-model
 * - Accesibilidad WCAG AA (role="switch", aria-checked)
 * - Objetivo táctil mínimo 48x48dp
 * - Label opcional con estado visible
 */

import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'

interface Props {
  /** Valor del toggle (v-model) */
  modelValue?: boolean
  /** Label visible */
  label?: string
  /** Estado disabled */
  disabled?: boolean
  /** Mostrar texto de estado (on/off) */
  showStatus?: boolean
  /** Aria-label para accesibilidad */
  ariaLabel?: string
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  label: '',
  disabled: false,
  showStatus: false,
  ariaLabel: ''
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const { t } = useI18n()
const inputRef = ref<HTMLInputElement | null>(null)

const ariaLabelText = computed(() => props.ariaLabel || props.label || t('components.toggle.label'))

function handleChange() {
  if (props.disabled) return
  emit('update:modelValue', !props.modelValue)
}

function focus() {
  inputRef.value?.focus()
}

defineExpose({ focus, inputRef })
</script>

<style scoped>
.nubi-toggle {
  display: inline-flex;
  align-items: center;
  gap: var(--nubi-spacing-sm);
  cursor: pointer;
  min-height: 48px;
  user-select: none;
}

.nubi-toggle--disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.nubi-toggle__input {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
  pointer-events: none;
}

.nubi-toggle__track {
  position: relative;
  display: flex;
  align-items: center;
  width: 52px;
  height: 32px;
  min-width: 52px;
  border-radius: var(--nubi-radius-full);
  background-color: var(--nubi-border-strong);
  transition: background-color var(--nubi-duration-fast) var(--nubi-ease-in-out);
  padding: 2px;
}

.nubi-toggle__input:focus-visible + .nubi-toggle__track {
  box-shadow: 0 0 0 3px var(--nubi-color-focus);
}

.nubi-toggle--checked .nubi-toggle__track {
  background-color: var(--nubi-color-primary);
}

.nubi-toggle__thumb {
  width: 28px;
  height: 28px;
  border-radius: var(--nubi-radius-full);
  background-color: var(--nubi-color-white);
  box-shadow: var(--nubi-shadow-sm);
  transition: transform var(--nubi-duration-fast) var(--nubi-ease-in-out);
}

.nubi-toggle--checked .nubi-toggle__thumb {
  transform: translateX(20px);
}

.nubi-toggle__label {
  font-size: var(--nubi-font-size-base);
  color: var(--nubi-text-primary);
  line-height: var(--nubi-line-height-normal);
}

.nubi-toggle__status {
  font-size: var(--nubi-font-size-sm);
  color: var(--nubi-text-secondary);
  font-weight: var(--nubi-font-weight-medium);
  min-width: 70px;
}

.nubi-toggle--disabled .nubi-toggle__label {
  color: var(--nubi-text-tertiary);
}
</style>
