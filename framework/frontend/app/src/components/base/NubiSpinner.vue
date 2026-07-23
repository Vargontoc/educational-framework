<template>
  <div
    :class="[
      'nubi-spinner',
      `nubi-spinner--${size}`,
      { 'nubi-spinner--overlay': overlay }
    ]"
    role="status"
    :aria-label="label"
  >
    <svg class="nubi-spinner__svg" viewBox="0 0 50 50">
      <circle
        class="nubi-spinner__circle"
        cx="25"
        cy="25"
        r="20"
        fill="none"
        stroke-width="4"
      />
    </svg>
    <span v-if="showLabel" class="nubi-spinner__label">{{ label }}</span>
  </div>
</template>

<script setup lang="ts">
/**
 * NubiSpinner - Indicador de carga circular
 * 
 * Características:
 * - Animación suave de rotación
 * - Overlay parcial o total
 * - 3 tamaños: sm, md, lg
 * - Accesibilidad con role="status"
 * - Label opcional visible
 */

import { useI18n } from 'vue-i18n'

const { t } = useI18n()

interface Props {
  /** Tamaño del spinner */
  size?: 'sm' | 'md' | 'lg'
  /** Mostrar como overlay */
  overlay?: boolean
  /** Label accesible */
  label?: string
  /** Mostrar label visible */
  showLabel?: boolean
}

withDefaults(defineProps<Props>(), {
  size: 'md',
  overlay: false,
  label: () => t('common.loading'),
  showLabel: false
})
</script>

<style scoped>
.nubi-spinner {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  gap: var(--nubi-spacing-sm);
}

.nubi-spinner__svg {
  animation: rotate 2s linear infinite;
}

.nubi-spinner__circle {
  stroke: var(--nubi-color-primary);
  stroke-linecap: round;
  animation: dash 1.5s ease-in-out infinite;
}

@keyframes rotate {
  100% {
    transform: rotate(360deg);
  }
}

@keyframes dash {
  0% {
    stroke-dasharray: 1, 150;
    stroke-dashoffset: 0;
  }
  50% {
    stroke-dasharray: 90, 150;
    stroke-dashoffset: -35;
  }
  100% {
    stroke-dasharray: 90, 150;
    stroke-dashoffset: -124;
  }
}

/* Sizes */
.nubi-spinner--sm .nubi-spinner__svg {
  width: 24px;
  height: 24px;
}

.nubi-spinner--md .nubi-spinner__svg {
  width: 40px;
  height: 40px;
}

.nubi-spinner--lg .nubi-spinner__svg {
  width: 64px;
  height: 64px;
}

/* Overlay mode */
.nubi-spinner--overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--nubi-overlay-bg);
  z-index: 10;
}

.nubi-spinner__label {
  font-size: var(--nubi-font-size-sm);
  color: var(--nubi-text-secondary);
  font-weight: var(--nubi-font-weight-medium);
}
</style>
