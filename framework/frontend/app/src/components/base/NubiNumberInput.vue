<template>
  <div :class="['nubi-number-input', { 'nubi-number-input--error': hasError, 'nubi-number-input--disabled': disabled }]">
    <label v-if="label" :for="inputId" class="nubi-number-input__label">
      {{ label }}
      <span v-if="required" class="nubi-number-input__required" aria-hidden="true">*</span>
    </label>
    
    <div class="nubi-number-input__wrapper">
      <button
        type="button"
        class="nubi-number-input__btn nubi-number-input__btn--decrement"
        :aria-label="t('components.numberInput.decrement')"
        :disabled="disabled || isAtMin"
        @click="decrement"
      >
        <NubiIcon name="minus" :size="20" />
      </button>
      
      <input
        :id="inputId"
        ref="inputRef"
        type="number"
        inputmode="numeric"
        :value="modelValue"
        :placeholder="placeholderText"
        :disabled="disabled"
        :readonly="readonly"
        :min="min"
        :max="max"
        :step="step"
        :required="required"
        :aria-invalid="hasError"
        :aria-describedby="descriptionId"
        :aria-valuenow="modelValue"
        :aria-valuemin="min"
        :aria-valuemax="max"
        class="nubi-number-input__field"
        @input="handleInput"
        @blur="handleBlur"
        @focus="handleFocus"
        @keydown.arrow-up.prevent="increment"
        @keydown.arrow-down.prevent="decrement"
      />
      
      <button
        type="button"
        class="nubi-number-input__btn nubi-number-input__btn--increment"
        :aria-label="t('components.numberInput.increment')"
        :disabled="disabled || isAtMax"
        @click="increment"
      >
        <NubiIcon name="plus" :size="20" />
      </button>
    </div>
    
    <div v-if="hasError && errorMessage" :id="descriptionId" class="nubi-number-input__error" role="alert">
      <NubiIcon name="alert-circle" :size="14" />
      <span>{{ errorMessage }}</span>
    </div>
    
    <div v-else-if="hint" :id="descriptionId" class="nubi-number-input__hint">
      {{ hint }}
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * NubiNumberInput - Campo de entrada numérico
 * 
 * Características:
 * - Teclado numérico en móvil (inputmode="numeric")
 * - Botones de incremento/decremento
 * - Validación de rango (min/max)
 * - Soporte v-model
 * - Navegación por teclado (flechas arriba/abajo)
 * - Accesibilidad WCAG AA (aria-valuenow, aria-valuemin, aria-valuemax)
 * - Objetivo táctil mínimo 48x48dp en botones
 */

import { ref, computed, useId } from 'vue'
import { useI18n } from 'vue-i18n'
import NubiIcon from './NubiIcon.vue'

interface Props {
  /** Valor del input (v-model) */
  modelValue?: number | null
  /** Label visible */
  label?: string
  /** Placeholder */
  placeholder?: string
  /** Valor mínimo */
  min?: number
  /** Valor máximo */
  max?: number
  /** Paso de incremento */
  step?: number
  /** Estado disabled */
  disabled?: boolean
  /** Estado readonly */
  readonly?: boolean
  /** Campo obligatorio */
  required?: boolean
  /** Mensaje de error */
  error?: string
  /** Mensaje de ayuda */
  hint?: string
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: null,
  label: '',
  placeholder: '',
  min: undefined,
  max: undefined,
  step: 1,
  disabled: false,
  readonly: false,
  required: false,
  error: '',
  hint: ''
})

const emit = defineEmits<{
  'update:modelValue': [value: number | null]
  blur: [event: FocusEvent]
  focus: [event: FocusEvent]
  error: [message: string | null]
}>()

const { t } = useI18n()
const inputRef = ref<HTMLInputElement | null>(null)
const touched = ref(false)
const uniqueId = useId()
const inputId = `nubi-number-input-${uniqueId}`
const descriptionId = `nubi-number-input-desc-${uniqueId}`

const placeholderText = computed(() => props.placeholder || t('components.numberInput.placeholder'))

const isAtMin = computed(() => {
  if (props.min === undefined || props.modelValue === null) return false
  return props.modelValue <= props.min
})

const isAtMax = computed(() => {
  if (props.max === undefined || props.modelValue === null) return false
  return props.modelValue >= props.max
})

/**
 * Valida el valor actual del input
 */
const validationError = computed(() => {
  if (!touched.value || props.modelValue === null) return null
  
  if (props.required && props.modelValue === null) {
    return t('common.required')
  }
  
  if (props.min !== undefined && props.modelValue < props.min) {
    return t('components.numberInput.min', { min: props.min })
  }
  
  if (props.max !== undefined && props.modelValue > props.max) {
    return t('components.numberInput.max', { max: props.max })
  }
  
  return null
})

const hasError = computed(() => !!props.error || !!validationError.value)
const errorMessage = computed(() => props.error || validationError.value)

function clamp(value: number): number {
  let result = value
  if (props.min !== undefined) result = Math.max(result, props.min)
  if (props.max !== undefined) result = Math.min(result, props.max)
  return result
}

function increment() {
  if (props.disabled || props.readonly) return
  const current = props.modelValue ?? 0
  const newValue = clamp(current + props.step)
  emit('update:modelValue', newValue)
}

function decrement() {
  if (props.disabled || props.readonly) return
  const current = props.modelValue ?? 0
  const newValue = clamp(current - props.step)
  emit('update:modelValue', newValue)
}

function handleInput(event: Event) {
  const target = event.target as HTMLInputElement
  const value = target.value === '' ? null : Number(target.value)
  emit('update:modelValue', value)
}

function handleBlur(event: FocusEvent) {
  touched.value = true
  emit('blur', event)
  emit('error', validationError.value)
}

function handleFocus(event: FocusEvent) {
  emit('focus', event)
}

function focus() {
  inputRef.value?.focus()
}

defineExpose({ focus, inputRef })
</script>

<style scoped>
.nubi-number-input {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-xs);
}

.nubi-number-input__label {
  font-size: var(--nubi-font-size-sm);
  font-weight: var(--nubi-font-weight-medium);
  color: var(--nubi-text-primary);
}

.nubi-number-input__required {
  color: var(--nubi-color-error);
  margin-left: var(--nubi-spacing-xs);
}

.nubi-number-input__wrapper {
  display: flex;
  align-items: center;
  border: var(--nubi-border-width-thick) solid var(--nubi-border-default);
  border-radius: var(--nubi-radius-md);
  background-color: var(--nubi-bg-surface);
  transition: border-color var(--nubi-duration-fast) var(--nubi-ease-in-out),
              box-shadow var(--nubi-duration-fast) var(--nubi-ease-in-out);
}

.nubi-number-input__wrapper:focus-within {
  border-color: var(--nubi-border-focus);
  box-shadow: 0 0 0 3px var(--nubi-color-focus);
}

.nubi-number-input--error .nubi-number-input__wrapper {
  border-color: var(--nubi-color-error);
}

.nubi-number-input--error .nubi-number-input__wrapper:focus-within {
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.3);
}

.nubi-number-input--disabled .nubi-number-input__wrapper {
  opacity: 0.5;
  cursor: not-allowed;
  background-color: var(--nubi-bg-surface-secondary);
}

.nubi-number-input__btn {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 48px;
  min-height: 48px;
  border: none;
  background: none;
  cursor: pointer;
  color: var(--nubi-color-primary);
  transition: background-color var(--nubi-duration-fast) var(--nubi-ease-in-out),
              color var(--nubi-duration-fast) var(--nubi-ease-in-out);
  flex-shrink: 0;
  border-radius: var(--nubi-radius-md);
}

.nubi-number-input__btn:hover:not(:disabled) {
  background-color: var(--nubi-color-primary-light);
  color: var(--nubi-color-white);
}

.nubi-number-input__btn:active:not(:disabled) {
  background-color: var(--nubi-color-primary);
}

.nubi-number-input__btn:disabled {
  color: var(--nubi-text-tertiary);
  cursor: not-allowed;
  opacity: 0.5;
}

.nubi-number-input__btn:focus-visible {
  outline: none;
  box-shadow: inset 0 0 0 2px var(--nubi-color-focus);
}

.nubi-number-input__field {
  flex: 1;
  border: none;
  background: none;
  padding: var(--nubi-spacing-sm) var(--nubi-spacing-xs);
  font-size: var(--nubi-font-size-base);
  font-family: var(--nubi-font-family-base);
  color: var(--nubi-text-primary);
  outline: none;
  text-align: center;
  width: 100%;
  min-height: 44px;
  -moz-appearance: textfield;
}

.nubi-number-input__field::-webkit-outer-spin-button,
.nubi-number-input__field::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.nubi-number-input__field::placeholder {
  color: var(--nubi-text-tertiary);
}

.nubi-number-input__field:disabled {
  cursor: not-allowed;
}

.nubi-number-input__error {
  display: flex;
  align-items: center;
  gap: var(--nubi-spacing-xs);
  font-size: var(--nubi-font-size-xs);
  color: var(--nubi-text-error);
}

.nubi-number-input__hint {
  font-size: var(--nubi-font-size-xs);
  color: var(--nubi-text-secondary);
}
</style>
