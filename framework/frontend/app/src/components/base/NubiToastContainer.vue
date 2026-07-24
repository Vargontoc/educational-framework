<template>
  <Teleport to="body">
    <div :class="['nubi-toast-container', `nubi-toast-container--${position}`]" aria-live="polite">
      <TransitionGroup name="nubi-toast">
        <div
          v-for="toast in toasts"
          :key="toast.id"
          :class="['nubi-toast', `nubi-toast--${toast.type}`]"
          role="status"
        >
          <NubiIcon :name="getIcon(toast.type)" :size="18" class="nubi-toast__icon" />
          
          <span class="nubi-toast__message">{{ toast.message }}</span>
          
          <button
            class="nubi-toast__close"
            :aria-label="t('components.toast.dismiss')"
            @click="remove(toast.id)"
          >
            <NubiIcon name="x" :size="14" />
          </button>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
/**
 * NubiToastContainer - Contenedor de notificaciones toast
 * 
 * Características:
 * - Renderiza toasts del composable useToast()
 * - Posición configurable
 * - Animaciones de entrada/salida con TransitionGroup
 * - Cola de múltiples notificaciones
 */

import { useI18n } from 'vue-i18n'
import { useToast } from '../../composables/useToast'
import NubiIcon from './NubiIcon.vue'

interface Props {
  /** Posición del contenedor */
  position?: 'top-right' | 'top-left' | 'bottom-right' | 'bottom-left' | 'top-center' | 'bottom-center'
}

withDefaults(defineProps<Props>(), {
  position: 'top-right'
})

const { t } = useI18n()
const { toasts, remove } = useToast()

function getIcon(type: string): string {
  switch (type) {
    case 'success': return 'check-circle'
    case 'error': return 'alert-circle'
    case 'warning': return 'alert-triangle'
    case 'info': return 'info'
    default: return 'info'
  }
}
</script>

<style scoped>
.nubi-toast-container {
  position: fixed;
  z-index: 10000;
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-sm);
  max-width: 400px;
  pointer-events: none;
}

.nubi-toast-container--top-right {
  top: var(--nubi-spacing-lg);
  right: var(--nubi-spacing-lg);
}

.nubi-toast-container--top-left {
  top: var(--nubi-spacing-lg);
  left: var(--nubi-spacing-lg);
}

.nubi-toast-container--bottom-right {
  bottom: var(--nubi-spacing-lg);
  right: var(--nubi-spacing-lg);
}

.nubi-toast-container--bottom-left {
  bottom: var(--nubi-spacing-lg);
  left: var(--nubi-spacing-lg);
}

.nubi-toast-container--top-center {
  top: var(--nubi-spacing-lg);
  left: 50%;
  transform: translateX(-50%);
}

.nubi-toast-container--bottom-center {
  bottom: var(--nubi-spacing-lg);
  left: 50%;
  transform: translateX(-50%);
}

.nubi-toast {
  display: flex;
  align-items: center;
  gap: var(--nubi-spacing-sm);
  padding: var(--nubi-spacing-sm) var(--nubi-spacing-md);
  border-radius: var(--nubi-radius-lg);
  box-shadow: var(--nubi-shadow-lg);
  min-width: 280px;
  max-width: 400px;
  pointer-events: auto;
  border-left: 4px solid;
}

.nubi-toast--info {
  background-color: var(--nubi-bg-surface);
  border-left-color: var(--nubi-color-info);
  color: var(--nubi-text-primary);
}

.nubi-toast--success {
  background-color: var(--nubi-bg-surface);
  border-left-color: var(--nubi-color-success);
  color: var(--nubi-text-primary);
}

.nubi-toast--warning {
  background-color: var(--nubi-bg-surface);
  border-left-color: var(--nubi-color-warning);
  color: var(--nubi-text-primary);
}

.nubi-toast--error {
  background-color: var(--nubi-bg-surface);
  border-left-color: var(--nubi-color-error);
  color: var(--nubi-text-primary);
}

.nubi-toast__icon {
  flex-shrink: 0;
}

.nubi-toast--info .nubi-toast__icon {
  color: var(--nubi-color-info);
}

.nubi-toast--success .nubi-toast__icon {
  color: var(--nubi-color-success);
}

.nubi-toast--warning .nubi-toast__icon {
  color: var(--nubi-color-warning);
}

.nubi-toast--error .nubi-toast__icon {
  color: var(--nubi-color-error);
}

.nubi-toast__message {
  flex: 1;
  font-size: var(--nubi-font-size-sm);
  line-height: var(--nubi-line-height-normal);
}

.nubi-toast__close {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 28px;
  min-height: 28px;
  padding: var(--nubi-spacing-xs);
  border: none;
  background: none;
  cursor: pointer;
  color: var(--nubi-text-tertiary);
  border-radius: var(--nubi-radius-sm);
  transition: color var(--nubi-duration-fast) var(--nubi-ease-in-out),
              background-color var(--nubi-duration-fast) var(--nubi-ease-in-out);
  flex-shrink: 0;
}

.nubi-toast__close:hover {
  color: var(--nubi-text-primary);
  background-color: var(--nubi-bg-surface-secondary);
}

.nubi-toast__close:focus-visible {
  outline: none;
  box-shadow: 0 0 0 2px var(--nubi-color-focus);
}

/* Transition animations */
.nubi-toast-enter-active {
  animation: nubi-toast-slide-in 300ms var(--nubi-ease-out);
}

.nubi-toast-leave-active {
  animation: nubi-toast-slide-out 200ms var(--nubi-ease-in);
}

.nubi-toast-move {
  transition: transform 300ms var(--nubi-ease-in-out);
}

@keyframes nubi-toast-slide-in {
  from {
    opacity: 0;
    transform: translateX(100%);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes nubi-toast-slide-out {
  from {
    opacity: 1;
    transform: translateX(0);
    max-height: 100px;
  }
  to {
    opacity: 0;
    transform: translateX(100%);
    max-height: 0;
    padding-top: 0;
    padding-bottom: 0;
    margin-bottom: 0;
  }
}
</style>
