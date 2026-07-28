<template>
  <div :class="['nubi-pin-input', { 'nubi-pin-input--error': hasError, 'nubi-pin-input--complete': isComplete }]">
    <label v-if="label" class="nubi-pin-input__label">
      {{ label }}
    </label>
    
    <div class="nubi-pin-input__digits" role="group" :aria-label="t('components.pinInput.label')">
      <div
        v-for="(_, index) in pinLength"
        :key="index"
        :class="[
          'nubi-pin-input__digit-wrapper',
          { 'nubi-pin-input__digit-wrapper--focused': focusedIndex === index, 'nubi-pin-input__digit-wrapper--filled': digits[index] !== '' }
        ]"
      >
        <input
          :ref="(el) => setInputRef(el as HTMLInputElement, index)"
          type="text"
          inputmode="numeric"
          maxlength="1"
          :value="digits[index] || ''"
          :disabled="disabled"
          :aria-label="t('components.pinInput.digit', { position: index + 1, total: pinLength })"
          :aria-invalid="hasError"
          :class="['nubi-pin-input__digit', { 'nubi-pin-input__digit--masked': masked && digits[index] }]"
          @input="(e) => handleDigitInput(e, index)"
          @keydown="(e) => handleKeydown(e, index)"
          @focus="handleFocus(index)"
          @blur="handleBlur(index)"
          @paste.prevent
        />
        <span v-if="digits[index] && masked" class="nubi-pin-input__mask" aria-hidden="true">•</span>
      </div>
    </div>
    
    <div v-if="isComplete" class="nubi-pin-input__feedback" role="status">
      <NubiIcon name="check-circle" :size="16" />
      <span>{{ t('components.pinInput.complete') }}</span>
    </div>
    
    <div v-if="hasError && errorMessage" class="nubi-pin-input__error" role="alert">
      <NubiIcon name="alert-circle" :size="14" />
      <span>{{ errorMessage }}</span>
    </div>
    
    <div v-if="showClear && hasValue" class="nubi-pin-input__clear">
      <button
        type="button"
        class="nubi-pin-input__clear-btn"
        :aria-label="t('components.pinInput.clear')"
        @click="clear"
      >
        <NubiIcon name="x" :size="16" />
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * NubiPinInput - Entrada de PIN de 4 dígitos numéricos
 * 
 * Características:
 * - Exactamente 4 dígitos numéricos (configurable)
 * - Estilo teclado móvil (inputmode="numeric")
 * - Dígitos ocultos (modo masked)
 * - Feedback visual al completar
 * - Auto-avance entre campos de dígitos
 * - Navegación por teclado (flechas, backspace)
 * - Accesibilidad WCAG AA
 * - Objetivo táctil mínimo 48x48dp
 */

import { ref, computed, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import NubiIcon from './NubiIcon.vue'

interface Props {
  /** Valor del PIN (v-model) */
  modelValue?: string
  /** Longitud del PIN */
  pinLength?: number
  /** Label visible */
  label?: string
  /** Ocultar dígitos */
  masked?: boolean
  /** Estado disabled */
  disabled?: boolean
  /** Mostrar botón de borrar */
  showClear?: boolean
  /** Mensaje de error */
  error?: string
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  pinLength: 4,
  label: '',
  masked: false,
  disabled: false,
  showClear: true,
  error: ''
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  complete: [value: string]
  error: [message: string | null]
}>()

const { t } = useI18n()
const inputRefs = ref<(HTMLInputElement | null)[]>([])
const focusedIndex = ref<number | null>(null)
const touched = ref(false)

const label = computed(() => props.label || t('components.pinInput.label'))

/**
 * Array reactivo de dígitos individuales
 */
const digits = computed(() => {
  const result: string[] = []
  for (let i = 0; i < props.pinLength; i++) {
    result[i] = props.modelValue[i] || ''
  }
  return result
})

const hasValue = computed(() => props.modelValue.length > 0)
const isComplete = computed(() => props.modelValue.length === props.pinLength)
const hasError = computed(() => !!props.error)
const errorMessage = computed(() => props.error)

function setInputRef(el: HTMLInputElement | null, index: number) {
  inputRefs.value[index] = el
}

function updateValue(newDigits: string[]) {
  const value = newDigits.join('')
  emit('update:modelValue', value)
  
  if (value.length === props.pinLength) {
    emit('complete', value)
  }
}

function handleDigitInput(event: Event, index: number) {
  const target = event.target as HTMLInputElement
  const value = target.value.replace(/[^0-9]/g, '')
  
  if (value.length === 0) return
  
  const newDigits = [...digits.value]
  newDigits[index] = value.charAt(0)
  updateValue(newDigits)
  
  // Auto-avance al siguiente campo
  if (index < props.pinLength - 1 && value.length > 0) {
    nextTick(() => {
      inputRefs.value[index + 1]?.focus()
    })
  }
}

function handleKeydown(event: KeyboardEvent, index: number) {
  const key = event.key
  
  // Backspace: borrar y retroceder
  if (key === 'Backspace') {
    event.preventDefault()
    const newDigits = [...digits.value]
    
    if (newDigits[index] !== '') {
      newDigits[index] = ''
      updateValue(newDigits)
    } else if (index > 0) {
      newDigits[index - 1] = ''
      updateValue(newDigits)
      nextTick(() => {
        inputRefs.value[index - 1]?.focus()
      })
    }
    return
  }
  
  // Flecha izquierda: retroceder foco
  if (key === 'ArrowLeft' && index > 0) {
    event.preventDefault()
    inputRefs.value[index - 1]?.focus()
    return
  }
  
  // Flecha derecha: avanzar foco
  if (key === 'ArrowRight' && index < props.pinLength - 1) {
    event.preventDefault()
    inputRefs.value[index + 1]?.focus()
    return
  }
  
  // Solo permitir dígitos numéricos
  if (!/^[0-9]$/.test(key) && !event.ctrlKey && !event.metaKey) {
    event.preventDefault()
  }
}

function handleFocus(index: number) {
  focusedIndex.value = index
}

function handleBlur(index: number) {
  if (focusedIndex.value === index) {
    focusedIndex.value = null
  }
  touched.value = true
}

function clear() {
  const newDigits = Array(props.pinLength).fill('')
  updateValue(newDigits)
  nextTick(() => {
    inputRefs.value[0]?.focus()
  })
}

/**
 * Enfoca el primer dígito programáticamente
 */
function focus() {
  inputRefs.value[0]?.focus()
}

defineExpose({ focus, clear })
</script>

<style scoped>
.nubi-pin-input {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--nubi-spacing-sm);
}

.nubi-pin-input__label {
  font-size: var(--nubi-font-size-sm);
  font-weight: var(--nubi-font-weight-medium);
  color: var(--nubi-text-primary);
}

.nubi-pin-input__digits {
  display: flex;
  gap: var(--nubi-spacing-sm);
  justify-content: center;
}

.nubi-pin-input__digit-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nubi-pin-input__digit {
  width: 48px;
  height: 56px;
  border: var(--nubi-border-width-thick) solid var(--nubi-border-default);
  border-radius: var(--nubi-radius-lg);
  background-color: var(--nubi-bg-surface);
  font-size: var(--nubi-font-size-2xl);
  font-weight: var(--nubi-font-weight-bold);
  font-family: var(--nubi-font-family-base);
  color: var(--nubi-text-primary);
  text-align: center;
  outline: none;
  caret-color: transparent;
  transition: border-color var(--nubi-duration-fast) var(--nubi-ease-in-out),
              box-shadow var(--nubi-duration-fast) var(--nubi-ease-in-out);
}

.nubi-pin-input__digit:focus {
  border-color: var(--nubi-border-focus);
  box-shadow: 0 0 0 3px var(--nubi-color-focus);
}

.nubi-pin-input__digit-wrapper--filled .nubi-pin-input__digit {
  border-color: var(--nubi-color-primary);
}

.nubi-pin-input__digit-wrapper--focused .nubi-pin-input__digit {
  border-color: var(--nubi-border-focus);
}

.nubi-pin-input--error .nubi-pin-input__digit {
  border-color: var(--nubi-color-error);
}

.nubi-pin-input--complete .nubi-pin-input__digit {
  border-color: var(--nubi-color-success);
}

.nubi-pin-input__digit--masked {
  color: transparent !important;
  caret-color: transparent !important;
  -webkit-text-fill-color: transparent;
}

.nubi-pin-input__mask {
  position: absolute;
  font-size: var(--nubi-font-size-3xl);
  color: var(--nubi-text-primary);
  pointer-events: none;
}

.nubi-pin-input__feedback {
  display: flex;
  align-items: center;
  gap: var(--nubi-spacing-xs);
  font-size: var(--nubi-font-size-sm);
  color: var(--nubi-text-success);
  font-weight: var(--nubi-font-weight-medium);
}

.nubi-pin-input__error {
  display: flex;
  align-items: center;
  gap: var(--nubi-spacing-xs);
  font-size: var(--nubi-font-size-xs);
  color: var(--nubi-text-error);
}

.nubi-pin-input__clear {
  margin-top: var(--nubi-spacing-xs);
}

.nubi-pin-input__clear-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 48px;
  min-height: 48px;
  border: none;
  background: none;
  cursor: pointer;
  color: var(--nubi-text-secondary);
  border-radius: var(--nubi-radius-md);
  transition: color var(--nubi-duration-fast) var(--nubi-ease-in-out),
              background-color var(--nubi-duration-fast) var(--nubi-ease-in-out);
}

.nubi-pin-input__clear-btn:hover {
  color: var(--nubi-text-primary);
  background-color: var(--nubi-bg-surface-secondary);
}

.nubi-pin-input__clear-btn:focus-visible {
  outline: none;
  box-shadow: 0 0 0 3px var(--nubi-color-focus);
}

@media (max-height: 500px) {
  .nubi-pin-input {
    gap: var(--nubi-spacing-xs);
  }

  .nubi-pin-input__label {
    font-size: var(--nubi-font-size-xs);
  }

  .nubi-pin-input__digit {
    width: 40px;
    height: 44px;
    font-size: var(--nubi-font-size-xl);
  }
}
</style>
