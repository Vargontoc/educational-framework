<template>
  <span :class="['nubi-badge', `nubi-badge--${variant}`, `nubi-badge--${size}`]">
    <NubiIcon v-if="icon" :name="icon" :size="iconSize" class="nubi-badge__icon" />
    <span class="nubi-badge__label">
      <slot>{{ label }}</slot>
    </span>
  </span>
</template>

<script setup lang="ts">
/**
 * NubiBadge - Etiqueta pequeña para estados o contadores
 * 
 * Características:
 * - 4 variantes: info, success, warning, error
 * - 2 tamaños: sm, md
 * - Icono opcional
 * - Accesibilidad WCAG AA
 */

import { computed } from 'vue'
import NubiIcon from './NubiIcon.vue'

interface Props {
  /** Texto del badge */
  label?: string
  /** Variante de color */
  variant?: 'info' | 'success' | 'warning' | 'error' | 'neutral'
  /** Tamaño del badge */
  size?: 'sm' | 'md'
  /** Icono opcional */
  icon?: string
}

const props = withDefaults(defineProps<Props>(), {
  label: '',
  variant: 'neutral',
  size: 'md',
  icon: ''
})

const iconSize = computed(() => props.size === 'sm' ? 12 : 14)
</script>

<style scoped>
.nubi-badge {
  display: inline-flex;
  align-items: center;
  gap: var(--nubi-spacing-xs);
  border-radius: var(--nubi-radius-full);
  font-weight: var(--nubi-font-weight-medium);
  white-space: nowrap;
}

.nubi-badge--sm {
  padding: 2px 8px;
  font-size: var(--nubi-font-size-xs);
}

.nubi-badge--md {
  padding: 4px 12px;
  font-size: var(--nubi-font-size-sm);
}

.nubi-badge--neutral {
  background-color: var(--nubi-bg-surface-tertiary);
  color: var(--nubi-text-secondary);
}

.nubi-badge--info {
  background-color: var(--nubi-bg-info);
  color: var(--nubi-text-info);
}

.nubi-badge--success {
  background-color: var(--nubi-bg-success);
  color: var(--nubi-text-success);
}

.nubi-badge--warning {
  background-color: var(--nubi-bg-warning);
  color: var(--nubi-text-warning);
}

.nubi-badge--error {
  background-color: var(--nubi-bg-error);
  color: var(--nubi-text-error);
}

.nubi-badge__icon {
  flex-shrink: 0;
}

.nubi-badge__label {
  line-height: 1;
}
</style>
