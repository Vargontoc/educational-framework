<template>
  <div :class="['nubi-text-input', { 'nubi-text-input--error': hasError, 'nubi-text-input--disabled': disabled }]">
    <label v-if="label" :for="inputId" class="nubi-text-input__label">
      {{ label }}
      <span v-if="required" class="nubi-text-input__required" aria-hidden="true">*</span>
    </label>
    
    <div class="nubi-text-input__wrapper">
      <span v-if="$slots.prefix || prefixIcon" class="nubi-text-input__prefix" aria-hidden="true">
        <slot name="prefix">
          <NubiIcon v-if="prefixIcon" :name="prefixIcon" :size="20" />
        </slot>
      </span>
      
      <input
        :id="inputId"
        ref="inputRef"
        :type="type"
        :value="modelValue"
        :placeholder="placeholderText"
        :disabled="disabled"
        :readonly="readonly"
        :maxlength="maxLength"
        :minlength="minLength"
        :required="required"
        :autocomplete="autocomplete"
        :aria-invalid="hasError"
        :aria-describedby="descriptionId"
        class="nubi-text-input__field"
        @input="handleInput"
        @blur="handleBlur"
        @focus="handleFocus"
      />
      
      <span v-if="$slots.suffix || suffixIcon" class="nubi-text-input__suffix" aria-hidden="true">
        <slot name="suffix">
          <NubiIcon v-if="suffixIcon" :name="suffixIcon" :size="20" />
        </slot>
      </span>
    </div>
    
    <div v-if="hasError && errorMessage" :id="descriptionId" class="nubi-text-input__error" role="alert">
      <NubiIcon name="alert-circle" :size="14" />
      <span>{{ errorMessage }}</span>
    </div>
    
    <div v-else-if="hint" :id="descriptionId" class="nubi-text-input__hint">
      {{ hint }}
    </div>
    
    <div v-if="maxLength && showCounter" class="nubi-text-input__counter">
      {{ $t('components.textInput.charactersCount', { count: currentLength, max: maxLength }) }}
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * NubiTextInput - Campo de entrada de texto
 * 
 * Características:
 * - Label visible siempre
 * - Placeholder configurable
 * - Validación en tiempo real (onBlur, onChange)
 * - Estados error/hint
 * - Soporte v-model
 * - Iconos prefix/suffix
 * - Contador de caracteres
 * - Accesibilidad WCAG AA (aria-invalid, aria-describedby)
 * - Objetivo táctil mínimo 48x48dp
 */

import { ref, computed, useId } from 'vue'
import { useI18n } from 'vue-i18n'
import NubiIcon from './NubiIcon.vue'

interface Props {
  /** Valor del input (v-model) */
  modelValue?: string
  /** Label visible */
  label?: string
  /** Placeholder */
  placeholder?: string
  /** Tipo de input */
  type?: 'text' | 'email' | 'password' | 'tel' | 'url'
  /** Estado disabled */
  disabled?: boolean
  /** Estado readonly */
  readonly?: boolean
  /** Campo obligatorio */
  required?: boolean
  /** Longitud máxima */
  maxLength?: number
  /** Longitud mínima */
  minLength?: number
  /** Autocompletar */
  autocomplete?: string
  /** Mensaje de error */
  error?: string
  /** Mensaje de ayuda */
  hint?: string
  /** Icono prefix */
  prefixIcon?: string
  /** Icono suffix */
  suffixIcon?: string
  /** Mostrar contador de caracteres */
  showCounter?: boolean
  /** Modo de validación */
  validateOn?: 'blur' | 'change' | 'submit'
  /** Función de validación personalizada */
  validator?: (value: string) => string | null
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  label: '',
  placeholder: '',
  type: 'text',
  disabled: false,
  readonly: false,
  required: false,
  maxLength: undefined,
  minLength: undefined,
  autocomplete: 'off',
  error: '',
  hint: '',
  prefixIcon: '',
  suffixIcon: '',
  showCounter: false,
  validateOn: 'blur',
  validator: undefined
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  blur: [event: FocusEvent]
  focus: [event: FocusEvent]
  error: [message: string | null]
}>()

const { t } = useI18n()
const inputRef = ref<HTMLInputElement | null>(null)
const isFocused = ref(false)
const touched = ref(false)
const uniqueId = useId()
const inputId = `nubi-text-input-${uniqueId}`
const descriptionId = `nubi-text-input-desc-${uniqueId}`

const placeholderText = computed(() => props.placeholder || t('components.textInput.placeholder'))
const currentLength = computed(() => (props.modelValue || '').length)

/**
 * Valida el valor actual del input
 */
const validationError = computed(() => {
  if (!touched.value) return null
  
  const value = props.modelValue || ''
  
  // Validación personalizada
  if (props.validator) {
    return props.validator(value)
  }
  
  // Campo obligatorio vacío
  if (props.required && value.trim() === '') {
    return t('components.textInput.required')
  }
  
  // Longitud mínima
  if (props.minLength && value.length > 0 && value.length < props.minLength) {
    return t('components.textInput.minLength', { min: props.minLength })
  }
  
  // Longitud máxima
  if (props.maxLength && value.length > props.maxLength) {
    return t('components.textInput.maxLength', { max: props.maxLength })
  }
  
  return null
})

const hasError = computed(() => !!props.error || !!validationError.value)
const errorMessage = computed(() => props.error || validationError.value)

function handleInput(event: Event) {
  const target = event.target as HTMLInputElement
  emit('update:modelValue', target.value)
  
  if (props.validateOn === 'change' && touched.value) {
    emit('error', validationError.value)
  }
}

function handleBlur(event: FocusEvent) {
  isFocused.value = false
  touched.value = true
  emit('blur', event)
  
  if (props.validateOn === 'blur') {
    emit('error', validationError.value)
  }
}

function handleFocus(event: FocusEvent) {
  isFocused.value = true
  emit('focus', event)
}

/**
 * Enfoca el input programáticamente
 */
function focus() {
  inputRef.value?.focus()
}

defineExpose({ focus, inputRef })
</script>

<style scoped>
.nubi-text-input {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-xs);
}

.nubi-text-input__label {
  font-size: var(--nubi-font-size-sm);
  font-weight: var(--nubi-font-weight-medium);
  color: var(--nubi-text-primary);
}

.nubi-text-input__required {
  color: var(--nubi-color-error);
  margin-left: var(--nubi-spacing-xs);
}

.nubi-text-input__wrapper {
  display: flex;
  align-items: center;
  border: var(--nubi-border-width-thick) solid var(--nubi-border-default);
  border-radius: var(--nubi-radius-md);
  background-color: var(--nubi-bg-surface);
  transition: border-color var(--nubi-duration-fast) var(--nubi-ease-in-out),
              box-shadow var(--nubi-duration-fast) var(--nubi-ease-in-out);
  min-height: 48px;
}

.nubi-text-input__wrapper:focus-within {
  border-color: var(--nubi-border-focus);
  box-shadow: 0 0 0 3px var(--nubi-color-focus);
}

.nubi-text-input--error .nubi-text-input__wrapper {
  border-color: var(--nubi-color-error);
}

.nubi-text-input--error .nubi-text-input__wrapper:focus-within {
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.3);
}

.nubi-text-input--disabled .nubi-text-input__wrapper {
  opacity: 0.5;
  cursor: not-allowed;
  background-color: var(--nubi-bg-surface-secondary);
}

.nubi-text-input__prefix,
.nubi-text-input__suffix {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 var(--nubi-spacing-sm);
  color: var(--nubi-text-tertiary);
  flex-shrink: 0;
}

.nubi-text-input__field {
  flex: 1;
  border: none;
  background: none;
  padding: var(--nubi-spacing-sm) var(--nubi-spacing-md);
  font-size: var(--nubi-font-size-base);
  font-family: var(--nubi-font-family-base);
  color: var(--nubi-text-primary);
  outline: none;
  width: 100%;
  min-height: 44px;
}

.nubi-text-input__field::placeholder {
  color: var(--nubi-text-tertiary);
}

.nubi-text-input__field:disabled {
  cursor: not-allowed;
}

.nubi-text-input__error {
  display: flex;
  align-items: center;
  gap: var(--nubi-spacing-xs);
  font-size: var(--nubi-font-size-xs);
  color: var(--nubi-text-error);
}

.nubi-text-input__hint {
  font-size: var(--nubi-font-size-xs);
  color: var(--nubi-text-secondary);
}

.nubi-text-input__counter {
  font-size: var(--nubi-font-size-xs);
  color: var(--nubi-text-tertiary);
  text-align: right;
}
</style>
