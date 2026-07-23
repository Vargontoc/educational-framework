<template>
  <button
    :class="[
      'nubi-button',
      `nubi-button--${variant}`,
      `nubi-button--${size}`,
      { 'nubi-button--disabled': disabled, 'nubi-button--loading': loading }
    ]"
    :disabled="disabled || loading"
    :aria-disabled="disabled || loading"
    :aria-busy="loading"
    @click="handleClick"
  >
    <span v-if="loading" class="nubi-button__spinner" aria-hidden="true">
      <NubiIcon name="loader" :size="iconSize" class="animate-spin" />
    </span>
    <span v-if="$slots.icon && !loading" class="nubi-button__icon" aria-hidden="true">
      <slot name="icon" />
    </span>
    <span class="nubi-button__content">
      <slot />
    </span>
  </button>
</template>

<script setup lang="ts">
/**
 * NubiButton - Componente de botón principal
 * 
 * Variantes:
 * - primary: Color destacado, acción principal
 * - secondary: Menos prominente, acciones secundarias
 * - destructive: Color rojo suave, acciones irreversibles
 * 
 * Características:
 * - Objetivo táctil mínimo 48x48dp (accesibilidad)
 * - 4 estados: normal, hover, pressed, disabled
 * - Soporte para iconos y loading state
 * - Internacionalización completa
 * - Accesibilidad WCAG AA
 */

import NubiIcon from './NubiIcon.vue'
import { computed } from 'vue'
interface Props {
  /** Variante del botón */
  variant?: 'primary' | 'secondary' | 'destructive'
  /** Tamaño del botón */
  size?: 'sm' | 'md' | 'lg'
  /** Estado disabled */
  disabled?: boolean
  /** Estado loading (muestra spinner) */
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'primary',
  size: 'md',
  disabled: false,
  loading: false
})

const emit = defineEmits<{
  click: [event: MouseEvent]
}>()

/**
 * Tamaño del icono según el tamaño del botón
 */
const iconSize = computed(() => {
  switch (props.size) {
    case 'sm': return 16
    case 'lg': return 24
    default: return 20
  }
})

/**
 * Maneja el click del botón
 */
function handleClick(event: MouseEvent) {
  if (!props.disabled && !props.loading) {
    emit('click', event)
  }
}
</script>


<style scoped>
.nubi-button {
  /* Reset */
  border: none;
  background: none;
  cursor: pointer;
  font-family: inherit;
  
  /* Layout */
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--nubi-spacing-sm);
  
  /* Tamaño mínimo táctil (48x48dp) */
  min-height: 48px;
  min-width: 48px;
  
  /* Typography */
  font-weight: var(--nubi-font-weight-semibold);
  line-height: 1;
  
  /* Border radius */
  border-radius: var(--nubi-radius-md);
  
  /* Transitions */
  transition: all var(--nubi-duration-fast) var(--nubi-ease-in-out);
  
  /* Focus visible */
  outline: none;
}

.nubi-button:focus-visible {
  box-shadow: 0 0 0 3px var(--nubi-color-focus);
}

/* Sizes */
.nubi-button--sm {
  padding: var(--nubi-spacing-sm) var(--nubi-spacing-md);
  font-size: var(--nubi-font-size-sm);
}

.nubi-button--md {
  padding: var(--nubi-spacing-md) var(--nubi-spacing-lg);
  font-size: var(--nubi-font-size-base);
}

.nubi-button--lg {
  padding: var(--nubi-spacing-lg) var(--nubi-spacing-xl);
  font-size: var(--nubi-font-size-lg);
}

/* Primary variant */
.nubi-button--primary {
  background-color: var(--nubi-color-primary);
  color: var(--nubi-color-white);
}

.nubi-button--primary:hover:not(:disabled) {
  background-color: var(--nubi-color-primary-dark);
}

.nubi-button--primary:active:not(:disabled) {
  background-color: var(--nubi-color-primary-dark);
  transform: scale(0.98);
}

/* Secondary variant */
.nubi-button--secondary {
  background-color: transparent;
  color: var(--nubi-color-primary);
  border: 2px solid var(--nubi-color-primary);
}

.nubi-button--secondary:hover:not(:disabled) {
  background-color: var(--nubi-color-primary-light);
  color: var(--nubi-color-white);
}

.nubi-button--secondary:active:not(:disabled) {
  background-color: var(--nubi-color-primary);
  color: var(--nubi-color-white);
  transform: scale(0.98);
}

/* Destructive variant */
.nubi-button--destructive {
  background-color: var(--nubi-color-error);
  color: var(--nubi-color-white);
}

.nubi-button--destructive:hover:not(:disabled) {
  background-color: #dc2626;
}

.nubi-button--destructive:active:not(:disabled) {
  background-color: #b91c1c;
  transform: scale(0.98);
}

/* Disabled state */
.nubi-button--disabled,
.nubi-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Loading state */
.nubi-button--loading {
  cursor: wait;
}

.nubi-button__spinner {
  display: inline-flex;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.nubi-button__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.nubi-button__content {
  display: inline-flex;
  align-items: center;
}
</style>
