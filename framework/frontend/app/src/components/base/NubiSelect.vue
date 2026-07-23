<template>
  <div :class="['nubi-select', { 'nubi-select--open': isOpen, 'nubi-select--error': hasError, 'nubi-select--disabled': disabled }]">
    <label v-if="label" :id="labelId" class="nubi-select__label">
      {{ label }}
      <span v-if="required" class="nubi-select__required" aria-hidden="true">*</span>
    </label>
    
    <div
      ref="triggerRef"
      :class="['nubi-select__trigger']"
      role="combobox"
      :aria-expanded="isOpen"
      :aria-haspopup="'listbox'"
      :aria-labelledby="labelId"
      :aria-activedescendant="isOpen && focusedIndex >= 0 ? optionId(focusedIndex) : undefined"
      :tabindex="disabled ? -1 : 0"
      @click="toggleDropdown"
      @keydown="handleTriggerKeydown"
    >
      <span :class="['nubi-select__value', { 'nubi-select__value--placeholder': !hasSelection }]">
        {{ displayValue }}
      </span>
      <NubiIcon name="chevron-down" :size="20" class="nubi-select__arrow" />
    </div>
    
    <Teleport to="body">
      <div
        v-if="isOpen"
        ref="dropdownRef"
        :id="listboxId"
        role="listbox"
        :aria-labelledby="labelId"
        class="nubi-select__dropdown"
        :style="dropdownStyle"
      >
        <div v-if="options.length === 0" class="nubi-select__no-options">
          {{ t('components.select.noOptions') }}
        </div>
        
        <ul class="nubi-select__options">
          <li
            v-for="(option, index) in options"
            :id="optionId(index)"
            :key="getOptionValue(option)"
            role="option"
            :class="[
              'nubi-select__option',
              { 
                'nubi-select__option--selected': getOptionValue(option) === modelValue,
                'nubi-select__option--focused': focusedIndex === index
              }
            ]"
            :aria-selected="getOptionValue(option) === modelValue"
            @click="selectOption(option)"
            @mouseenter="focusedIndex = index"
          >
            {{ getOptionLabel(option) }}
          </li>
        </ul>
      </div>
    </Teleport>
    
    <div v-if="hasError && errorMessage" class="nubi-select__error" role="alert">
      <NubiIcon name="alert-circle" :size="14" />
      <span>{{ errorMessage }}</span>
    </div>
    
    <div v-else-if="hint" class="nubi-select__hint">
      {{ hint }}
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * NubiSelect - Selector de opción única
 * 
 * Características:
 * - Selección única entre múltiples opciones
 * - Dropdown accesible con focus trapping
 * - Opción por defecto
 * - Soporte v-model
 * - Navegación completa por teclado (Tab, Enter, Space, flechas, Escape)
 * - Accesibilidad WCAG AA (role="combobox", role="listbox", aria-activedescendant)
 * - Objetivo táctil mínimo 48x48dp
 * - Teleport del dropdown al body para evitar overflow
 */

import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick, useId } from 'vue'
import { useI18n } from 'vue-i18n'
import NubiIcon from './NubiIcon.vue'

interface SelectOption {
  value: string | number
  label: string
  disabled?: boolean
}

interface Props {
  /** Valor seleccionado (v-model) */
  modelValue?: string | number | null
  /** Opciones del selector */
  options?: (SelectOption | string | number)[]
  /** Label visible */
  label?: string
  /** Placeholder */
  placeholder?: string
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
  placeholder: '',
  disabled: false,
  required: false,
  error: '',
  hint: ''
})

const emit = defineEmits<{
  'update:modelValue': [value: string | number | null]
  blur: []
  error: [message: string | null]
}>()

const { t } = useI18n()
const uniqueId = useId()
const labelId = `nubi-select-label-${uniqueId}`
const listboxId = `nubi-select-listbox-${uniqueId}`

const triggerRef = ref<HTMLElement | null>(null)
const dropdownRef = ref<HTMLElement | null>(null)
const isOpen = ref(false)
const focusedIndex = ref(-1)
const touched = ref(false)
const dropdownStyle = ref<Record<string, string>>({})

function optionId(index: number) {
  return `nubi-select-option-${uniqueId}-${index}`
}

/**
 * Normaliza las opciones a formato SelectOption
 */
function normalizeOption(option: SelectOption | string | number): SelectOption {
  if (typeof option === 'string' || typeof option === 'number') {
    return { value: option, label: String(option) }
  }
  return option
}

function getOptionValue(option: SelectOption | string | number): string | number {
  const normalized = normalizeOption(option)
  return normalized.value
}

function getOptionLabel(option: SelectOption | string | number): string {
  const normalized = normalizeOption(option)
  return normalized.label
}

const hasSelection = computed(() => props.modelValue !== null && props.modelValue !== undefined)

const displayValue = computed(() => {
  if (!hasSelection.value) {
    return props.placeholder || t('components.select.placeholder')
  }
  
  const selected = props.options.find(opt => getOptionValue(opt) === props.modelValue)
  return selected ? getOptionLabel(selected) : String(props.modelValue)
})

const hasError = computed(() => !!props.error)
const errorMessage = computed(() => props.error)

function toggleDropdown() {
  if (props.disabled) return
  isOpen.value ? closeDropdown() : openDropdown()
}

function openDropdown() {
  if (props.disabled || props.options.length === 0) return
  isOpen.value = true
  
  // Posicionar dropdown
  nextTick(() => {
    updateDropdownPosition()
    
    // Enfocar opción seleccionada o primera
    const selectedIndex = props.options.findIndex(opt => getOptionValue(opt) === props.modelValue)
    focusedIndex.value = selectedIndex >= 0 ? selectedIndex : 0
  })
}

function closeDropdown() {
  isOpen.value = false
  focusedIndex.value = -1
  touched.value = true
  triggerRef.value?.focus()
  emit('blur')
}

function selectOption(option: SelectOption | string | number) {
  const value = getOptionValue(option)
  emit('update:modelValue', value)
  closeDropdown()
}

function updateDropdownPosition() {
  if (!triggerRef.value) return
  
  const rect = triggerRef.value.getBoundingClientRect()
  dropdownStyle.value = {
    position: 'fixed',
    top: `${rect.bottom + 4}px`,
    left: `${rect.left}px`,
    width: `${rect.width}px`,
    zIndex: '1000'
  }
}

function handleTriggerKeydown(event: KeyboardEvent) {
  const key = event.key
  
  switch (key) {
    case 'Enter':
    case ' ':
      event.preventDefault()
      if (isOpen.value && focusedIndex.value >= 0) {
        selectOption(props.options[focusedIndex.value])
      } else {
        toggleDropdown()
      }
      break
      
    case 'ArrowDown':
      event.preventDefault()
      if (!isOpen.value) {
        openDropdown()
      } else if (focusedIndex.value < props.options.length - 1) {
        focusedIndex.value++
      }
      break
      
    case 'ArrowUp':
      event.preventDefault()
      if (!isOpen.value) {
        openDropdown()
      } else if (focusedIndex.value > 0) {
        focusedIndex.value--
      }
      break
      
    case 'Escape':
      event.preventDefault()
      closeDropdown()
      break
      
    case 'Home':
      if (isOpen.value) {
        event.preventDefault()
        focusedIndex.value = 0
      }
      break
      
    case 'End':
      if (isOpen.value) {
        event.preventDefault()
        focusedIndex.value = props.options.length - 1
      }
      break
      
    case 'Tab':
      if (isOpen.value) {
        closeDropdown()
      }
      break
  }
}

function handleClickOutside(event: MouseEvent) {
  if (!isOpen.value) return
  
  const target = event.target as Node
  if (triggerRef.value?.contains(target) || dropdownRef.value?.contains(target)) {
    return
  }
  
  closeDropdown()
}

function handleScroll() {
  if (isOpen.value) {
    updateDropdownPosition()
  }
}

watch(isOpen, (value) => {
  if (value) {
    document.addEventListener('click', handleClickOutside)
    document.addEventListener('scroll', handleScroll, true)
    window.addEventListener('resize', updateDropdownPosition)
  } else {
    document.removeEventListener('click', handleClickOutside)
    document.removeEventListener('scroll', handleScroll, true)
    window.removeEventListener('resize', updateDropdownPosition)
  }
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
  document.removeEventListener('scroll', handleScroll, true)
  window.removeEventListener('resize', updateDropdownPosition)
})
</script>

<style scoped>
.nubi-select {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-xs);
  position: relative;
}

.nubi-select__label {
  font-size: var(--nubi-font-size-sm);
  font-weight: var(--nubi-font-weight-medium);
  color: var(--nubi-text-primary);
}

.nubi-select__required {
  color: var(--nubi-color-error);
  margin-left: var(--nubi-spacing-xs);
}

.nubi-select__trigger {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 48px;
  padding: var(--nubi-spacing-sm) var(--nubi-spacing-md);
  border: var(--nubi-border-width-thick) solid var(--nubi-border-default);
  border-radius: var(--nubi-radius-md);
  background-color: var(--nubi-bg-surface);
  cursor: pointer;
  transition: border-color var(--nubi-duration-fast) var(--nubi-ease-in-out),
              box-shadow var(--nubi-duration-fast) var(--nubi-ease-in-out);
  outline: none;
}

.nubi-select__trigger:focus-visible {
  border-color: var(--nubi-border-focus);
  box-shadow: 0 0 0 3px var(--nubi-color-focus);
}

.nubi-select--open .nubi-select__trigger {
  border-color: var(--nubi-border-focus);
}

.nubi-select--error .nubi-select__trigger {
  border-color: var(--nubi-color-error);
}

.nubi-select--disabled .nubi-select__trigger {
  opacity: 0.5;
  cursor: not-allowed;
  background-color: var(--nubi-bg-surface-secondary);
}

.nubi-select__value {
  font-size: var(--nubi-font-size-base);
  color: var(--nubi-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.nubi-select__value--placeholder {
  color: var(--nubi-text-tertiary);
}

.nubi-select__arrow {
  color: var(--nubi-text-tertiary);
  transition: transform var(--nubi-duration-fast) var(--nubi-ease-in-out);
  flex-shrink: 0;
  margin-left: var(--nubi-spacing-sm);
}

.nubi-select--open .nubi-select__arrow {
  transform: rotate(180deg);
}

.nubi-select__error {
  display: flex;
  align-items: center;
  gap: var(--nubi-spacing-xs);
  font-size: var(--nubi-font-size-xs);
  color: var(--nubi-text-error);
}

.nubi-select__hint {
  font-size: var(--nubi-font-size-xs);
  color: var(--nubi-text-secondary);
}
</style>

<style>
/* Dropdown styles (not scoped - rendered in Teleport) */
.nubi-select__dropdown {
  background-color: var(--nubi-bg-surface);
  border: var(--nubi-border-width-thick) solid var(--nubi-border-focus);
  border-radius: var(--nubi-radius-md);
  box-shadow: var(--nubi-shadow-lg);
  max-height: 240px;
  overflow-y: auto;
}

.nubi-select__no-options {
  padding: var(--nubi-spacing-md);
  text-align: center;
  color: var(--nubi-text-tertiary);
  font-size: var(--nubi-font-size-sm);
}

.nubi-select__options {
  list-style: none;
  margin: 0;
  padding: var(--nubi-spacing-xs) 0;
}

.nubi-select__option {
  padding: var(--nubi-spacing-sm) var(--nubi-spacing-md);
  font-size: var(--nubi-font-size-base);
  color: var(--nubi-text-primary);
  cursor: pointer;
  min-height: 48px;
  display: flex;
  align-items: center;
  transition: background-color var(--nubi-duration-fast) var(--nubi-ease-in-out);
}

.nubi-select__option--focused {
  background-color: var(--nubi-bg-surface-secondary);
}

.nubi-select__option--selected {
  background-color: var(--nubi-color-primary-light);
  color: var(--nubi-color-white);
  font-weight: var(--nubi-font-weight-medium);
}

.nubi-select__option:hover {
  background-color: var(--nubi-bg-surface-tertiary);
}

.nubi-select__option--selected:hover {
  background-color: var(--nubi-color-primary);
}
</style>
