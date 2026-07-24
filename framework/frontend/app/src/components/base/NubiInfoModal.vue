<template>
  <Teleport to="body">
    <Transition name="nubi-modal">
      <div
        v-if="modelValue"
        class="nubi-info-modal-overlay"
        @click.self="handleOverlayClick"
      >
        <div
          ref="modalRef"
          role="dialog"
          aria-modal="true"
          :aria-labelledby="titleId"
          class="nubi-info-modal"
          @keydown="handleKeydown"
        >
          <div class="nubi-info-modal__header">
            <h2 :id="titleId" class="nubi-info-modal__title">
              {{ title }}
            </h2>
            <button
              ref="closeButtonRef"
              class="nubi-info-modal__close"
              :aria-label="closeText"
              @click="handleClose"
            >
              <NubiIcon name="x" :size="20" />
            </button>
          </div>
          
          <div class="nubi-info-modal__body">
            <slot>
              <p v-if="message" class="nubi-info-modal__message">{{ message }}</p>
            </slot>
          </div>
          
          <div v-if="$slots.footer" class="nubi-info-modal__footer">
            <slot name="footer" />
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
/**
 * NubiInfoModal - Modal informativo
 * 
 * Características:
 * - Solo botón de cierre
 * - Scroll interno si contenido extenso
 * - Focus trapping con Tab y Shift+Tab
 * - Bloqueo de scroll del body
 * - Cierre con Escape
 * - Accesibilidad con role="dialog" y aria-modal
 */

import { ref, watch, useId, nextTick, onBeforeUnmount } from 'vue'
import { useI18n } from 'vue-i18n'
import NubiIcon from './NubiIcon.vue'

interface Props {
  /** Mostrar modal (v-model) */
  modelValue: boolean
  /** Título del modal */
  title?: string
  /** Mensaje del modal */
  message?: string
  /** Texto del botón cerrar */
  closeText?: string
  /** Cerrar al hacer clic en el overlay */
  closeOnOverlay?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  title: '',
  message: '',
  closeText: '',
  closeOnOverlay: true
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  close: []
}>()

const { t } = useI18n()
const modalRef = ref<HTMLElement | null>(null)
const closeButtonRef = ref<HTMLButtonElement | null>(null)
const titleId = `nubi-info-title-${useId()}`

const closeText = props.closeText || t('components.infoModal.close')

function closeModal() {
  emit('update:modelValue', false)
  emit('close')
}

function handleClose() {
  closeModal()
}

function handleOverlayClick() {
  if (props.closeOnOverlay) {
    closeModal()
  }
}

function handleKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    event.preventDefault()
    closeModal()
    return
  }
  
  // Focus trapping
  if (event.key === 'Tab' && modalRef.value) {
    const focusableElements = modalRef.value.querySelectorAll<HTMLElement>(
      'button:not([disabled]), [href], input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])'
    )
    
    if (focusableElements.length === 0) return
    
    const firstElement = focusableElements[0]
    const lastElement = focusableElements[focusableElements.length - 1]
    
    if (event.shiftKey) {
      if (document.activeElement === firstElement) {
        event.preventDefault()
        lastElement.focus()
      }
    } else {
      if (document.activeElement === lastElement) {
        event.preventDefault()
        firstElement.focus()
      }
    }
  }
}

function lockBodyScroll() {
  document.body.style.overflow = 'hidden'
}

function unlockBodyScroll() {
  document.body.style.overflow = ''
}

watch(() => props.modelValue, (isOpen) => {
  if (isOpen) {
    lockBodyScroll()
    nextTick(() => {
      closeButtonRef.value?.focus()
    })
  } else {
    unlockBodyScroll()
  }
})

onBeforeUnmount(() => {
  unlockBodyScroll()
})
</script>

<style scoped>
.nubi-info-modal-overlay {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--nubi-overlay-bg);
  z-index: 9000;
  padding: var(--nubi-spacing-lg);
}

.nubi-info-modal {
  background-color: var(--nubi-bg-surface);
  border-radius: var(--nubi-radius-xl);
  box-shadow: var(--nubi-shadow-xl);
  max-width: 560px;
  width: 100%;
  max-height: calc(100vh - 64px);
  display: flex;
  flex-direction: column;
}

.nubi-info-modal__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--nubi-spacing-lg);
  border-bottom: var(--nubi-border-width) solid var(--nubi-border-default);
  flex-shrink: 0;
}

.nubi-info-modal__title {
  font-size: var(--nubi-font-size-xl);
  font-weight: var(--nubi-font-weight-bold);
  color: var(--nubi-text-primary);
  margin: 0;
  line-height: var(--nubi-line-height-tight);
}

.nubi-info-modal__close {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 48px;
  min-height: 48px;
  padding: var(--nubi-spacing-sm);
  border: none;
  background: none;
  cursor: pointer;
  color: var(--nubi-text-secondary);
  border-radius: var(--nubi-radius-md);
  transition: color var(--nubi-duration-fast) var(--nubi-ease-in-out),
              background-color var(--nubi-duration-fast) var(--nubi-ease-in-out);
}

.nubi-info-modal__close:hover {
  color: var(--nubi-text-primary);
  background-color: var(--nubi-bg-surface-secondary);
}

.nubi-info-modal__close:focus-visible {
  outline: none;
  box-shadow: 0 0 0 3px var(--nubi-color-focus);
}

.nubi-info-modal__body {
  padding: var(--nubi-spacing-lg);
  overflow-y: auto;
  flex: 1;
}

.nubi-info-modal__message {
  font-size: var(--nubi-font-size-base);
  color: var(--nubi-text-secondary);
  margin: 0;
  line-height: var(--nubi-line-height-relaxed);
}

.nubi-info-modal__footer {
  padding: var(--nubi-spacing-lg);
  border-top: var(--nubi-border-width) solid var(--nubi-border-default);
  flex-shrink: 0;
}

/* Transition */
.nubi-modal-enter-active {
  transition: opacity var(--nubi-duration-normal) var(--nubi-ease-out);
}

.nubi-modal-enter-active .nubi-info-modal {
  transition: transform var(--nubi-duration-normal) var(--nubi-ease-bounce);
}

.nubi-modal-leave-active {
  transition: opacity var(--nubi-duration-fast) var(--nubi-ease-in);
}

.nubi-modal-leave-active .nubi-info-modal {
  transition: transform var(--nubi-duration-fast) var(--nubi-ease-in);
}

.nubi-modal-enter-from {
  opacity: 0;
}

.nubi-modal-enter-from .nubi-info-modal {
  transform: scale(0.9);
}

.nubi-modal-leave-to {
  opacity: 0;
}

.nubi-modal-leave-to .nubi-info-modal {
  transform: scale(0.95);
}
</style>
