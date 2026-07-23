<template>
  <button
    :class="[
      'nubi-icon-button',
      `nubi-icon-button--${size}`,
      { 'nubi-icon-button--disabled': disabled }
    ]"
    :disabled="disabled"
    :aria-disabled="disabled"
    :aria-label="label"
    :title="tooltip || label"
    @click="handleClick"
  >
    <NubiIcon :name="icon" :size="iconSize" />
  </button>
</template>

<script setup lang="ts">
/**
 * NubiIconButton - Botón de acción rápida con icono
 * 
 * Características:
 * - Objetivo táctil mínimo 48x48dp (accesibilidad)
 * - Tooltip al hover
 * - 3 estados: normal, hover, disabled
 * - Forma circular
 * - Accesibilidad WCAG AA
 */

import NubiIcon from './NubiIcon.vue'

interface Props {
  /** Nombre del icono (de Lucide o custom) */
  icon: string
  /** Label accesible (obligatorio para screen readers) */
  label: string
  /** Tooltip opcional (si no se proporciona, usa label) */
  tooltip?: string
  /** Tamaño del botón */
  size?: 'sm' | 'md' | 'lg'
  /** Estado disabled */
  disabled?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  size: 'md',
  disabled: false
})

const emit = defineEmits<{
  click: [event: MouseEvent]
}>()

/**
 * Tamaño del icono según el tamaño del botón
 */
const iconSize = computed(() => {
  switch (props.size) {
    case 'sm': return 20
    case 'lg': return 28
    default: return 24
  }
})

/**
 * Maneja el click del botón
 */
function handleClick(event: MouseEvent) {
  if (!props.disabled) {
    emit('click', event)
  }
}
</script>

<script lang="ts">
import { computed } from 'vue'
</script>

<style scoped>
.nubi-icon-button {
  /* Reset */
  border: none;
  background: none;
  cursor: pointer;
  
  /* Layout */
  display: inline-flex;
  align-items: center;
  justify-content: center;
  
  /* Forma circular */
  border-radius: var(--nubi-radius-full);
  
  /* Tamaño mínimo táctil (48x48dp) */
  min-width: 48px;
  min-height: 48px;
  
  /* Colors */
  background-color: var(--nubi-bg-surface-secondary);
  color: var(--nubi-text-primary);
  
  /* Transitions */
  transition: all var(--nubi-duration-fast) var(--nubi-ease-in-out);
  
  /* Focus visible */
  outline: none;
}

.nubi-icon-button:hover:not(:disabled) {
  background-color: var(--nubi-color-primary-light);
  color: var(--nubi-color-primary-dark);
}

.nubi-icon-button:active:not(:disabled) {
  background-color: var(--nubi-color-primary);
  color: var(--nubi-color-white);
  transform: scale(0.95);
}

.nubi-icon-button:focus-visible {
  box-shadow: 0 0 0 3px var(--nubi-color-focus);
}

/* Sizes */
.nubi-icon-button--sm {
  width: 48px;
  height: 48px;
}

.nubi-icon-button--md {
  width: 56px;
  height: 56px;
}

.nubi-icon-button--lg {
  width: 64px;
  height: 64px;
}

/* Disabled state */
.nubi-icon-button--disabled,
.nubi-icon-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
