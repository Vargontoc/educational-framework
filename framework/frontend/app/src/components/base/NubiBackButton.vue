<template>
  <button
    class="nubi-back-button"
    :aria-label="labelText"
    @click="handleClick"
  >
    <NubiIcon name="arrow-left" :size="20" class="nubi-back-button__icon" />
    <span v-if="showLabel" class="nubi-back-button__label">
      {{ labelText }}
    </span>
  </button>
</template>

<script setup lang="ts">
/**
 * NubiBackButton - Botón de navegación hacia atrás
 * 
 * Características:
 * - Flecha + texto opcional
 * - Posición consistente (izquierda superior)
 * - Integración con router o callback personalizado
 * - Accesibilidad WCAG AA
 * - Objetivo táctil mínimo 48x48dp
 */

import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import NubiIcon from './NubiIcon.vue'

interface Props {
  /** Mostrar texto junto a la flecha */
  showLabel?: boolean
  /** Texto personalizado (usa i18n por defecto) */
  label?: string
  /** Ruta de destino (usa router.back() si no se especifica) */
  to?: string
  /** Usar router para navegación */
  useRouter?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showLabel: true,
  label: '',
  to: '',
  useRouter: true
})

const emit = defineEmits<{
  click: []
}>()

const { t } = useI18n()
const router = useRouter()

const labelText = computed(() => props.label || t('components.backButton.label'))

function handleClick() {
  emit('click')
  
  if (props.to && props.useRouter) {
    router.push(props.to)
  } else if (props.useRouter) {
    router.back()
  }
}
</script>

<style scoped>
.nubi-back-button {
  display: inline-flex;
  align-items: center;
  gap: var(--nubi-spacing-xs);
  min-width: 48px;
  min-height: 48px;
  padding: var(--nubi-spacing-sm) var(--nubi-spacing-md);
  border: none;
  background: none;
  cursor: pointer;
  color: var(--nubi-text-primary);
  font-family: var(--nubi-font-family-base);
  font-size: var(--nubi-font-size-base);
  font-weight: var(--nubi-font-weight-medium);
  border-radius: var(--nubi-radius-md);
  transition: background-color var(--nubi-duration-fast) var(--nubi-ease-in-out),
              color var(--nubi-duration-fast) var(--nubi-ease-in-out);
}

.nubi-back-button:hover {
  background-color: var(--nubi-bg-surface-secondary);
  color: var(--nubi-color-primary);
}

.nubi-back-button:active {
  background-color: var(--nubi-bg-surface-tertiary);
}

.nubi-back-button:focus-visible {
  outline: none;
  box-shadow: 0 0 0 3px var(--nubi-color-focus);
}

.nubi-back-button__icon {
  flex-shrink: 0;
}

.nubi-back-button__label {
  white-space: nowrap;
}
</style>
