<template>
  <div
    :class="['nubi-alert', `nubi-alert--${type}`]"
    role="alert"
    :aria-live="type === 'error' ? 'assertive' : 'polite'"
  >
    <NubiIcon :name="iconName" :size="20" class="nubi-alert__icon" />
    
    <div class="nubi-alert__content">
      <p v-if="title" class="nubi-alert__title">{{ title }}</p>
      <p v-if="message || $slots.default" class="nubi-alert__message">
        <slot>{{ message }}</slot>
      </p>
    </div>
    
    <button
      v-if="dismissible"
      class="nubi-alert__close"
      :aria-label="t('components.alert.dismiss')"
      @click="handleDismiss"
    >
      <NubiIcon name="x" :size="16" />
    </button>
  </div>
</template>

<script setup lang="ts">
/**
 * NubiAlert - Mensaje persistente hasta cierre manual
 * 
 * Características:
 * - Tipos: info, warning, error, success
 * - Icono según tipo
 * - Título y mensaje
 * - Dismissible opcional
 * - Accesibilidad con role="alert" y aria-live
 */

import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import NubiIcon from './NubiIcon.vue'

interface Props {
  /** Tipo de alerta */
  type?: 'info' | 'warning' | 'error' | 'success'
  /** Título de la alerta */
  title?: string
  /** Mensaje de la alerta */
  message?: string
  /** Permitir cerrar la alerta */
  dismissible?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  type: 'info',
  title: '',
  message: '',
  dismissible: true
})

const emit = defineEmits<{
  dismiss: []
}>()

const { t } = useI18n()

const iconName = computed(() => {
  switch (props.type) {
    case 'success': return 'check-circle'
    case 'warning': return 'alert-triangle'
    case 'error': return 'alert-circle'
    case 'info': return 'info'
    default: return 'info'
  }
})

function handleDismiss() {
  emit('dismiss')
}
</script>

<style scoped>
.nubi-alert {
  display: flex;
  align-items: flex-start;
  gap: var(--nubi-spacing-sm);
  padding: var(--nubi-spacing-md);
  border-radius: var(--nubi-radius-lg);
  border: var(--nubi-border-width) solid;
}

.nubi-alert--info {
  background-color: var(--nubi-bg-info);
  border-color: var(--nubi-color-info);
  color: var(--nubi-text-info);
}

.nubi-alert--warning {
  background-color: var(--nubi-bg-warning);
  border-color: var(--nubi-color-warning);
  color: var(--nubi-text-warning);
}

.nubi-alert--error {
  background-color: var(--nubi-bg-error);
  border-color: var(--nubi-color-error);
  color: var(--nubi-text-error);
}

.nubi-alert--success {
  background-color: var(--nubi-bg-success);
  border-color: var(--nubi-color-success);
  color: var(--nubi-text-success);
}

.nubi-alert__icon {
  flex-shrink: 0;
  margin-top: 2px;
}

.nubi-alert__content {
  flex: 1;
  min-width: 0;
}

.nubi-alert__title {
  font-size: var(--nubi-font-size-base);
  font-weight: var(--nubi-font-weight-semibold);
  margin: 0 0 var(--nubi-spacing-xs) 0;
  line-height: var(--nubi-line-height-tight);
}

.nubi-alert__message {
  font-size: var(--nubi-font-size-sm);
  margin: 0;
  line-height: var(--nubi-line-height-normal);
}

.nubi-alert__close {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 32px;
  min-height: 32px;
  padding: var(--nubi-spacing-xs);
  border: none;
  background: none;
  cursor: pointer;
  color: currentColor;
  opacity: 0.7;
  border-radius: var(--nubi-radius-sm);
  transition: opacity var(--nubi-duration-fast) var(--nubi-ease-in-out),
              background-color var(--nubi-duration-fast) var(--nubi-ease-in-out);
  flex-shrink: 0;
}

.nubi-alert__close:hover {
  opacity: 1;
  background-color: rgba(0, 0, 0, 0.1);
}

.nubi-alert__close:focus-visible {
  outline: none;
  box-shadow: 0 0 0 2px currentColor;
  opacity: 1;
}
</style>
