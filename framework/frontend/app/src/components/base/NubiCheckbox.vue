<template>
  <label :class="['nubi-checkbox', { 'nubi-checkbox--disabled': disabled, 'nubi-checkbox--checked': isChecked, 'nubi-checkbox--indeterminate': isIndeterminate }]">
    <input
      ref="inputRef"
      type="checkbox"
      :checked="isChecked"
      :disabled="disabled"
      :indeterminate="isIndeterminate"
      :aria-checked="isIndeterminate ? 'mixed' : isChecked"
      class="nubi-checkbox__input"
      @change="handleChange"
    />
    
    <span class="nubi-checkbox__box" aria-hidden="true">
      <NubiIcon v-if="isChecked" name="check" :size="16" class="nubi-checkbox__icon" />
      <NubiIcon v-else-if="isIndeterminate" name="minus" :size="16" class="nubi-checkbox__icon" />
    </span>
    
    <span v-if="label || $slots.default" class="nubi-checkbox__label">
      <slot>{{ label }}</slot>
    </span>
  </label>
</template>

<script setup lang="ts">
/**
 * NubiCheckbox - Casilla de verificación
 * 
 * Características:
 * - Opción binaria con label claro
 * - Estados checked/unchecked/indeterminate
 * - Soporte v-model
 * - Accesibilidad WCAG AA (aria-checked="mixed" para indeterminate)
 * - Objetivo táctil mínimo 48x48dp
 * - Navegación por teclado (Space para toggle)
 */

import { ref, computed } from 'vue'
import NubiIcon from './NubiIcon.vue'

interface Props {
  /** Valor del checkbox (v-model) */
  modelValue?: boolean | 'indeterminate'
  /** Label visible */
  label?: string
  /** Estado disabled */
  disabled?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  label: '',
  disabled: false
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean | 'indeterminate']
}>()

const inputRef = ref<HTMLInputElement | null>(null)

const isChecked = computed(() => props.modelValue === true)
const isIndeterminate = computed(() => props.modelValue === 'indeterminate')

function handleChange() {
  if (props.disabled) return
  
  // Toggle: true -> false, false -> true, indeterminate -> true
  const newValue = props.modelValue === true ? false : true
  emit('update:modelValue', newValue)
}

function focus() {
  inputRef.value?.focus()
}

defineExpose({ focus, inputRef })
</script>

<style scoped>
.nubi-checkbox {
  display: inline-flex;
  align-items: center;
  gap: var(--nubi-spacing-sm);
  cursor: pointer;
  min-height: 48px;
  user-select: none;
}

.nubi-checkbox--disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.nubi-checkbox__input {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
  pointer-events: none;
}

.nubi-checkbox__box {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  min-width: 24px;
  border: var(--nubi-border-width-thick) solid var(--nubi-border-strong);
  border-radius: var(--nubi-radius-sm);
  background-color: var(--nubi-bg-surface);
  transition: background-color var(--nubi-duration-fast) var(--nubi-ease-in-out),
              border-color var(--nubi-duration-fast) var(--nubi-ease-in-out),
              box-shadow var(--nubi-duration-fast) var(--nubi-ease-in-out);
}

.nubi-checkbox__input:focus-visible + .nubi-checkbox__box {
  box-shadow: 0 0 0 3px var(--nubi-color-focus);
  border-color: var(--nubi-border-focus);
}

.nubi-checkbox--checked .nubi-checkbox__box,
.nubi-checkbox--indeterminate .nubi-checkbox__box {
  background-color: var(--nubi-color-primary);
  border-color: var(--nubi-color-primary);
}

.nubi-checkbox__icon {
  color: var(--nubi-color-white);
}

.nubi-checkbox__label {
  font-size: var(--nubi-font-size-base);
  color: var(--nubi-text-primary);
  line-height: var(--nubi-line-height-normal);
}

.nubi-checkbox--disabled .nubi-checkbox__label {
  color: var(--nubi-text-tertiary);
}
</style>
