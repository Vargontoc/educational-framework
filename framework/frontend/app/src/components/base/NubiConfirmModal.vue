<template>
  <Teleport to="body">
    <Transition name="nubi-modal">
      <div
        v-if="modelValue"
        class="nubi-confirm-modal-overlay"
        @click.self="handleOverlayClick"
      >
        <div
          ref="modalRef"
          role="dialog"
          aria-modal="true"
          :aria-labelledby="titleId"
          :aria-describedby="messageId"
          class="nubi-confirm-modal"
          @keydown="handleKeydown"
        >
          <div v-if="icon" class="nubi-confirm-modal__icon">
            <NubiIcon :name="icon" :size="40" />
          </div>
          
          <h2 :id="titleId" class="nubi-confirm-modal__title">
            {{ title }}
          </h2>
          
          <p :id="messageId" class="nubi-confirm-modal__message">
            {{ message }}
          </p>
          
          <div class="nubi-confirm-modal__actions">
            <NubiButton variant="secondary" @click="handleCancel">
              {{ cancelText }}
            </NubiButton>
            <NubiButton :variant="confirmVariant" @click="handleConfirm">
              {{ confirmText }}
            </NubiButton>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
/**
 * NubiConfirmModal - Diálogo de confirmación
 * 
 * Características:
 * - Overlay oscuro
 * - Título, mensaje, botones confirmar y cancelar
 * - Focus trapping con Tab y Shift+Tab
 * - Bloqueo de scroll del body
 * - Cierre con Escape
 * - Accesibilidad con role="dialog" y aria-modal
 */

import { ref, watch, useId, nextTick, onBeforeUnmount } from 'vue'
import { useI18n } from 'vue-i18n'
import NubiIcon from './NubiIcon.vue'
import NubiButton from './NubiButton.vue'

interface Props {
  /** Mostrar modal (v-model) */
  modelValue: boolean
  /** Título del modal */
  title?: string
  /** Mensaje del modal */
  message?: string
  /** Texto del botón cancelar */
  cancelText?: string
  /** Texto del botón confirmar */
  confirmText?: string
  /** Variante del botón confirmar */
  confirmVariant?: 'primary' | 'destructive'
  /** Icono a mostrar */
  icon?: string
  /** Cerrar al hacer clic en el overlay */
  closeOnOverlay?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  title: '',
  message: '',
  cancelText: '',
  confirmText: '',
  confirmVariant: 'primary',
  icon: '',
  closeOnOverlay: true
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  confirm: []
  cancel: []
  close: []
}>()

const { t } = useI18n()
const modalRef = ref<HTMLElement | null>(null)
const titleId = `nubi-confirm-title-${useId()}`
const messageId = `nubi-confirm-message-${useId()}`

const cancelText = props.cancelText || t('components.confirmModal.cancel')
const confirmText = props.confirmText || t('components.confirmModal.confirm')

function closeModal() {
  emit('update:modelValue', false)
  emit('close')
}

function handleConfirm() {
  emit('confirm')
  closeModal()
}

function handleCancel() {
  emit('cancel')
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
      // Focus first button in modal
      const firstButton = modalRef.value?.querySelector<HTMLElement>('button')
      firstButton?.focus()
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
.nubi-confirm-modal-overlay {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--nubi-overlay-bg);
  z-index: 9000;
  padding: var(--nubi-spacing-lg);
}

.nubi-confirm-modal {
  background-color: var(--nubi-bg-surface);
  border-radius: var(--nubi-radius-xl);
  box-shadow: var(--nubi-shadow-xl);
  padding: var(--nubi-spacing-xl);
  max-width: 420px;
  width: 100%;
  text-align: center;
}

.nubi-confirm-modal__icon {
  margin-bottom: var(--nubi-spacing-md);
  color: var(--nubi-color-primary);
}

.nubi-confirm-modal__title {
  font-size: var(--nubi-font-size-xl);
  font-weight: var(--nubi-font-weight-bold);
  color: var(--nubi-text-primary);
  margin: 0 0 var(--nubi-spacing-sm) 0;
  line-height: var(--nubi-line-height-tight);
}

.nubi-confirm-modal__message {
  font-size: var(--nubi-font-size-base);
  color: var(--nubi-text-secondary);
  margin: 0 0 var(--nubi-spacing-xl) 0;
  line-height: var(--nubi-line-height-relaxed);
}

.nubi-confirm-modal__actions {
  display: flex;
  gap: var(--nubi-spacing-sm);
  justify-content: center;
}

/* Transition */
.nubi-modal-enter-active {
  transition: opacity var(--nubi-duration-normal) var(--nubi-ease-out);
}

.nubi-modal-enter-active .nubi-confirm-modal {
  transition: transform var(--nubi-duration-normal) var(--nubi-ease-bounce);
}

.nubi-modal-leave-active {
  transition: opacity var(--nubi-duration-fast) var(--nubi-ease-in);
}

.nubi-modal-leave-active .nubi-confirm-modal {
  transition: transform var(--nubi-duration-fast) var(--nubi-ease-in);
}

.nubi-modal-enter-from {
  opacity: 0;
}

.nubi-modal-enter-from .nubi-confirm-modal {
  transform: scale(0.9);
}

.nubi-modal-leave-to {
  opacity: 0;
}

.nubi-modal-leave-to .nubi-confirm-modal {
  transform: scale(0.95);
}
</style>
