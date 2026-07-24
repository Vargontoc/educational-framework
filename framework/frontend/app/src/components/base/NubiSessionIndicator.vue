<template>
  <div :class="['nubi-session-indicator', { 'nubi-session-indicator--warning': isWarning, 'nubi-session-indicator--expired': isExpired }]">
    <NubiIcon :name="iconName" :size="16" class="nubi-session-indicator__icon" />
    <div class="nubi-session-indicator__content">
      <span class="nubi-session-indicator__label">{{ label }}</span>
      <span v-if="!isExpired" class="nubi-session-indicator__time">{{ formattedTime }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * NubiSessionIndicator - Indicador de tiempo restante de sesión
 * 
 * Características:
 * - Muestra tiempo restante antes de logout automático
 * - Aviso visual cuando queda poco tiempo (< 1 minuto)
 * - Estado expirado
 * - Icono dinámico según estado
 */

import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import NubiIcon from './NubiIcon.vue'

interface Props {
  /** Tiempo restante en segundos */
  timeLeft: number
  /** Label personalizado */
  label?: string
}

const props = withDefaults(defineProps<Props>(), {
  label: ''
})

const { t } = useI18n()

const isWarning = computed(() => props.timeLeft > 0 && props.timeLeft < 60)
const isExpired = computed(() => props.timeLeft <= 0)

const iconName = computed(() => {
  if (isExpired.value) return 'x-circle'
  if (isWarning.value) return 'alert-triangle'
  return 'clock'
})

const label = computed(() => {
  if (props.label) return props.label
  if (isExpired.value) return t('components.sessionIndicator.expired')
  if (isWarning.value) return t('components.sessionIndicator.warning')
  return t('components.sessionIndicator.label')
})

const formattedTime = computed(() => {
  const minutes = Math.floor(props.timeLeft / 60)
  const seconds = props.timeLeft % 60
  return `${minutes}:${seconds.toString().padStart(2, '0')}`
})
</script>

<style scoped>
.nubi-session-indicator {
  display: flex;
  align-items: center;
  gap: var(--nubi-spacing-sm);
  padding: var(--nubi-spacing-sm) var(--nubi-spacing-md);
  background-color: var(--nubi-bg-surface-secondary);
  border-radius: var(--nubi-radius-md);
  border: 1px solid var(--nubi-border-default);
}

.nubi-session-indicator--warning {
  background-color: var(--nubi-bg-warning);
  border-color: var(--nubi-color-warning);
}

.nubi-session-indicator--warning .nubi-session-indicator__icon,
.nubi-session-indicator--warning .nubi-session-indicator__label,
.nubi-session-indicator--warning .nubi-session-indicator__time {
  color: var(--nubi-text-warning);
}

.nubi-session-indicator--expired {
  background-color: var(--nubi-bg-error);
  border-color: var(--nubi-color-error);
}

.nubi-session-indicator--expired .nubi-session-indicator__icon,
.nubi-session-indicator--expired .nubi-session-indicator__label {
  color: var(--nubi-text-error);
}

.nubi-session-indicator__icon {
  flex-shrink: 0;
  color: var(--nubi-text-secondary);
}

.nubi-session-indicator__content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nubi-session-indicator__label {
  font-size: var(--nubi-font-size-xs);
  color: var(--nubi-text-secondary);
  line-height: 1;
}

.nubi-session-indicator__time {
  font-size: var(--nubi-font-size-sm);
  font-weight: var(--nubi-font-weight-semibold);
  color: var(--nubi-text-primary);
  font-variant-numeric: tabular-nums;
  line-height: 1;
}
</style>
