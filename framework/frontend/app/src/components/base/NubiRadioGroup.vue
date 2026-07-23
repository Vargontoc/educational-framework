<template>
  <div :class="['nubi-radio-group', { 'nubi-radio-group--error': hasError, 'nubi-radio-group--disabled': disabled }]" role="radiogroup" :aria-labelledby="labelId">
    <label v-if="label" :id="labelId" class="nubi-radio-group__label">
      {{ label }}
      <span v-if="required" class="nubi-radio-group__required" aria-hidden="true">*</span>
    </label>
    
    <div :class="['nubi-radio-group__options', `nubi-radio-group__options--${direction}`]">
      <label
        v-for="(option, index) in normalizedOptions"
        :key="String(option.value)"
        :class="[
          'nubi-radio-group__option',
          { 
            'nubi-radio-group__option--selected': modelValue === option.value,
            'nubi-radio-group__option--disabled': disabled || option.disabled
          }
        ]"
      >
        <input
          :ref="(el) => setInputRef(el as HTMLInputElement, index)"
          type="radio"
          :name="groupName"
          :value="option.value"
          :checked="modelValue === option.value"
          :disabled="disabled || option.disabled"
          :aria-checked="modelValue === option.value"
          class="nubi-radio-group__input"
          @change="handleChange(option.value)"
          @keydown="handleKeydown($event, index)"
        />
        
        <span class="nubi-radio-group__radio" aria-hidden="true">
          <span v-if="modelValue === option.value" class="nubi-radio-group__dot" />
        </span>
        
        <span class="nubi-radio-group__option-label">
          {{ option.label }}
        </span>
      </label>
    </div>
    
    <div v-if="hasError && errorMessage" class="nubi-radio-group__error" role="alert">
      <NubiIcon name="alert-circle" :size="14" />
      <span>{{ errorMessage }}</span>
    </div>
    
    <div v-else-if="hint" class="nubi-radio-group__hint">
      {{ hint }}
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * NubiRadioGroup - Grupo de radio buttons
 * 
 * Características:
 * - Selección única entre opciones mutuamente excluyentes
 * - Label por opción
 * - Soporte v-model
 * - Navegación por teclado (flechas para mover entre opciones)
 * - Accesibilidad WCAG AA (role="radiogroup", aria-checked)
 * - Objetivo táctil mínimo 48x48dp
 * - Dirección configurable (vertical/horizontal)
 */

import { ref, computed, useId } from 'vue'
import NubiIcon from './NubiIcon.vue'

interface RadioOption {
  value: string | number
  label: string
  disabled?: boolean
}

interface Props {
  /** Valor seleccionado (v-model) */
  modelValue?: string | number | null
  /** Opciones del grupo */
  options?: (RadioOption | string | number)[]
  /** Label del grupo */
  label?: string
  /** Dirección de las opciones */
  direction?: 'vertical' | 'horizontal'
  /** Estado disabled */
  disabled?: boolean
  /** Campo obligatorio */
  required?: boolean
  /** Mensaje de error */
  error?: string
  /** Mensaje de ayuda */
  hint?: string
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: null,
  options: () => [],
  label: '',
  direction: 'vertical',
  disabled: false,
  required: false,
  error: '',
  hint: ''
})

const emit = defineEmits<{
  'update:modelValue': [value: string | number]
  blur: []
  error: [message: string | null]
}>()

const uniqueId = useId()
const labelId = `nubi-radio-group-label-${uniqueId}`
const groupName = `nubi-radio-group-${uniqueId}`
const inputRefs = ref<(HTMLInputElement | null)[]>([])
const touched = ref(false)

function setInputRef(el: HTMLInputElement | null, index: number) {
  inputRefs.value[index] = el
}

/**
 * Normaliza las opciones a formato RadioOption
 */
function normalizeOption(option: RadioOption | string | number): RadioOption {
  if (typeof option === 'string' || typeof option === 'number') {
    return { value: option, label: String(option) }
  }
  return option
}

const normalizedOptions = computed(() => props.options.map(normalizeOption))

const hasError = computed(() => !!props.error)
const errorMessage = computed(() => props.error)

function handleChange(value: string | number) {
  if (props.disabled) return
  emit('update:modelValue', value)
  touched.value = true
}

function handleKeydown(event: KeyboardEvent, currentIndex: number) {
  const key = event.key
  const options = normalizedOptions.value
  let nextIndex = currentIndex
  
  switch (key) {
    case 'ArrowDown':
    case 'ArrowRight':
      event.preventDefault()
      nextIndex = (currentIndex + 1) % options.length
      // Skip disabled options
      while (options[nextIndex].disabled && nextIndex !== currentIndex) {
        nextIndex = (nextIndex + 1) % options.length
      }
      break
      
    case 'ArrowUp':
    case 'ArrowLeft':
      event.preventDefault()
      nextIndex = (currentIndex - 1 + options.length) % options.length
      // Skip disabled options
      while (options[nextIndex].disabled && nextIndex !== currentIndex) {
        nextIndex = (nextIndex - 1 + options.length) % options.length
      }
      break
      
    default:
      return
  }
  
  if (!options[nextIndex].disabled) {
    inputRefs.value[nextIndex]?.focus()
    handleChange(options[nextIndex].value)
  }
}

function focus() {
  // Focus first non-disabled option
  const firstEnabled = normalizedOptions.value.findIndex(opt => !opt.disabled)
  if (firstEnabled >= 0) {
    inputRefs.value[firstEnabled]?.focus()
  }
}

defineExpose({ focus })
</script>

<style scoped>
.nubi-radio-group {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-xs);
}

.nubi-radio-group__label {
  font-size: var(--nubi-font-size-sm);
  font-weight: var(--nubi-font-weight-medium);
  color: var(--nubi-text-primary);
}

.nubi-radio-group__required {
  color: var(--nubi-color-error);
  margin-left: var(--nubi-spacing-xs);
}

.nubi-radio-group__options {
  display: flex;
  gap: var(--nubi-spacing-sm);
}

.nubi-radio-group__options--vertical {
  flex-direction: column;
}

.nubi-radio-group__options--horizontal {
  flex-direction: row;
  flex-wrap: wrap;
}

.nubi-radio-group__option {
  display: inline-flex;
  align-items: center;
  gap: var(--nubi-spacing-sm);
  cursor: pointer;
  min-height: 48px;
  user-select: none;
  padding: var(--nubi-spacing-xs) 0;
}

.nubi-radio-group__option--disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.nubi-radio-group__input {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
  pointer-events: none;
}

.nubi-radio-group__radio {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  min-width: 24px;
  border: var(--nubi-border-width-thick) solid var(--nubi-border-strong);
  border-radius: var(--nubi-radius-full);
  background-color: var(--nubi-bg-surface);
  transition: border-color var(--nubi-duration-fast) var(--nubi-ease-in-out),
              box-shadow var(--nubi-duration-fast) var(--nubi-ease-in-out);
}

.nubi-radio-group__input:focus-visible + .nubi-radio-group__radio {
  box-shadow: 0 0 0 3px var(--nubi-color-focus);
  border-color: var(--nubi-border-focus);
}

.nubi-radio-group__option--selected .nubi-radio-group__radio {
  border-color: var(--nubi-color-primary);
}

.nubi-radio-group__dot {
  width: 12px;
  height: 12px;
  border-radius: var(--nubi-radius-full);
  background-color: var(--nubi-color-primary);
}

.nubi-radio-group__option-label {
  font-size: var(--nubi-font-size-base);
  color: var(--nubi-text-primary);
  line-height: var(--nubi-line-height-normal);
}

.nubi-radio-group__option--disabled .nubi-radio-group__option-label {
  color: var(--nubi-text-tertiary);
}

.nubi-radio-group__error {
  display: flex;
  align-items: center;
  gap: var(--nubi-spacing-xs);
  font-size: var(--nubi-font-size-xs);
  color: var(--nubi-text-error);
}

.nubi-radio-group__hint {
  font-size: var(--nubi-font-size-xs);
  color: var(--nubi-text-secondary);
}
</style>
