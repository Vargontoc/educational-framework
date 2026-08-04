<template>
  <div :class="['nubi-textarea', { 'nubi-textarea--error': hasError, 'nubi-textarea--disabled': disabled }]">
    <label :for="inputId" class="nubi-textarea__label">
      {{ label }}
      <span v-if="required" class="nubi-textarea__required" aria-hidden="true">*</span>
    </label>

    <textarea
      :id="inputId"
      ref="textareaRef"
      :value="modelValue"
      :placeholder="placeholder"
      :maxlength="maxLength"
      :disabled="disabled"
      :rows="rows"
      :required="required"
      :aria-invalid="hasError"
      :aria-describedby="descriptionId"
      class="nubi-textarea__input"
      @input="handleInput"
      @blur="handleBlur"
      @focus="handleFocus"
    />

    <div v-if="maxLength" class="nubi-textarea__counter">
      {{ currentLength }} / {{ maxLength }}
    </div>

    <div v-if="hasError && error" :id="descriptionId" class="nubi-textarea__error" role="alert">
      <NubiIcon name="alert-circle" :size="14" />
      <span>{{ error }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, useId } from 'vue'
import NubiIcon from './NubiIcon.vue'

interface Props {
  modelValue?: string
  label?: string
  placeholder?: string
  maxLength?: number
  error?: string
  disabled?: boolean
  rows?: number
  required?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  label: '',
  placeholder: '',
  maxLength: 2000,
  error: '',
  disabled: false,
  rows: 4,
  required: false
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const textareaRef = ref<HTMLTextAreaElement | null>(null)
const uniqueId = useId()
const inputId = `nubi-textarea-${uniqueId}`
const descriptionId = `nubi-textarea-desc-${uniqueId}`

const currentLength = computed(() => (props.modelValue || '').length)
const hasError = computed(() => !!props.error)

function handleInput(event: Event) {
  const target = event.target as HTMLTextAreaElement
  emit('update:modelValue', target.value)
}

function handleBlur() {
  // reserved for future validation
}

function handleFocus() {
  // reserved for future validation
}

function focus() {
  textareaRef.value?.focus()
}

defineExpose({ focus, textareaRef })
</script>

<style scoped>
.nubi-textarea {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-xs);
}

.nubi-textarea__label {
  font-size: var(--nubi-font-size-sm);
  font-weight: var(--nubi-font-weight-medium);
  color: var(--nubi-text-primary);
}

.nubi-textarea__required {
  color: var(--nubi-color-error);
  margin-left: var(--nubi-spacing-xs);
}

.nubi-textarea__input {
  border: var(--nubi-border-width-thick) solid var(--nubi-border-default);
  border-radius: var(--nubi-radius-md);
  background-color: var(--nubi-bg-surface);
  padding: var(--nubi-spacing-sm) var(--nubi-spacing-md);
  font-size: var(--nubi-font-size-base);
  font-family: var(--nubi-font-family-base);
  color: var(--nubi-text-primary);
  resize: vertical;
  min-height: 48px;
  outline: none;
  transition: border-color var(--nubi-duration-fast) var(--nubi-ease-in-out),
              box-shadow var(--nubi-duration-fast) var(--nubi-ease-in-out);
}

.nubi-textarea__input::placeholder {
  color: var(--nubi-text-tertiary);
}

.nubi-textarea__input:focus {
  border-color: var(--nubi-border-focus);
  box-shadow: 0 0 0 3px var(--nubi-color-focus);
}

.nubi-textarea--error .nubi-textarea__input {
  border-color: var(--nubi-color-error);
}

.nubi-textarea--error .nubi-textarea__input:focus {
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.3);
}

.nubi-textarea--disabled .nubi-textarea__input {
  opacity: 0.5;
  cursor: not-allowed;
  background-color: var(--nubi-bg-surface-secondary);
}

.nubi-textarea__input:disabled {
  cursor: not-allowed;
}

.nubi-textarea__counter {
  font-size: var(--nubi-font-size-xs);
  color: var(--nubi-text-tertiary);
  text-align: right;
}

.nubi-textarea__error {
  display: flex;
  align-items: center;
  gap: var(--nubi-spacing-xs);
  font-size: var(--nubi-font-size-xs);
  color: var(--nubi-text-error);
}
</style>
