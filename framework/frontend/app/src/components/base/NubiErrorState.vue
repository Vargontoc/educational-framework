<template>
  <div class="nubi-error-state">
    <div class="nubi-error-state__icon">
      <NubiIcon name="alert-circle" :size="64" />
    </div>
    
    <h3 class="nubi-error-state__title">
      {{ title }}
    </h3>
    
    <p v-if="message" class="nubi-error-state__message">
      {{ message }}
    </p>
    
    <p v-if="details" class="nubi-error-state__details">
      {{ details }}
    </p>
    
    <div v-if="showRetry" class="nubi-error-state__action">
      <NubiButton variant="primary" @click="$emit('retry')">
        {{ retryLabel }}
      </NubiButton>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * NubiErrorState - Vista para errores
 * 
 * Características:
 * - Icono de error
 * - Título y mensaje claros
 * - Detalles opcionales
 * - Botón de reintentar
 * - Accesibilidad completa
 */

import { useI18n } from 'vue-i18n'
import NubiIcon from './NubiIcon.vue'
import NubiButton from './NubiButton.vue'

const { t } = useI18n()

interface Props {
  /** Título del error */
  title?: string
  /** Mensaje de error */
  message?: string
  /** Detalles técnicos (opcional) */
  details?: string
  /** Mostrar botón de reintentar */
  showRetry?: boolean
  /** Label del botón de reintentar */
  retryLabel?: string
}

withDefaults(defineProps<Props>(), {
  title: () => t('components.errorState.defaultTitle'),
  message: () => t('components.errorState.defaultMessage'),
  showRetry: true,
  retryLabel: () => t('components.errorState.defaultRetry')
})

defineEmits<{
  retry: []
}>()
</script>

<style scoped>
.nubi-error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: var(--nubi-spacing-2xl);
  gap: var(--nubi-spacing-lg);
  min-height: 300px;
}

.nubi-error-state__icon {
  color: var(--nubi-color-error);
  margin-bottom: var(--nubi-spacing-md);
}

.nubi-error-state__title {
  font-size: var(--nubi-font-size-xl);
  font-weight: var(--nubi-font-weight-semibold);
  color: var(--nubi-text-primary);
  margin: 0;
}

.nubi-error-state__message {
  font-size: var(--nubi-font-size-base);
  color: var(--nubi-text-secondary);
  margin: 0;
  max-width: 400px;
  line-height: var(--nubi-line-height-relaxed);
}

.nubi-error-state__details {
  font-size: var(--nubi-font-size-sm);
  color: var(--nubi-text-tertiary);
  margin: 0;
  max-width: 500px;
  padding: var(--nubi-spacing-md);
  background-color: var(--nubi-bg-surface-secondary);
  border-radius: var(--nubi-radius-md);
  font-family: monospace;
  text-align: left;
}

.nubi-error-state__action {
  margin-top: var(--nubi-spacing-md);
}
</style>
