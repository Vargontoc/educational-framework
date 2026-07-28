<template>
  <button
    :class="['home-action', { 'home-action--has-family': hasFamily }]"
    :aria-label="actionLabel"
    @click="$emit('activate')"
  >
    <span class="home-action__text">
      {{ actionLabel }}
    </span>
  </button>
</template>

<script setup lang="ts">
/**
 * HomeAction - Accion principal superpuesta al avatar de Nubi
 * 
 * Segun FEAT-002 y SPRINT-008:
 * - Sin familia: muestra "Registrar familia"
 * - Con familia: muestra "Bienvenida familia <nombre>" (truncado a 50 chars)
 * - Objetivo tactil minimo 48x48dp (accesibilidad)
 * - Identificable sin depender solo de color o iconos
 * - i18n completo
 */

import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

interface Props {
  /** Indica si hay una familia registrada */
  hasFamily: boolean
  /** Nombre de la familia (ya truncado si aplica) */
  familyName: string
}

const props = defineProps<Props>()

const emit = defineEmits<{
  activate: []
}>()

const { t } = useI18n()

/**
 * Etiqueta de la accion principal segun el estado de familia
 */
const actionLabel = computed(() => {
  if (!props.hasFamily) {
    return t('views.home.registerFamily')
  }
  return t('views.home.welcomeFamily', { name: props.familyName })
})
</script>

<style scoped>
.home-action {
  /* Reset */
  border: none;
  cursor: pointer;
  font-family: inherit;
  
  /* Layout */
  display: flex;
  align-items: center;
  justify-content: center;
  
  /* Posicionamiento superpuesto al avatar */
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  
  /* Tamano */
  min-height: 48px;
  min-width: 48px;
  max-width: 90%;
  padding: var(--nubi-spacing-sm) var(--nubi-spacing-lg);
  
  /* Estilo visual */
  background-color: var(--nubi-color-primary);
  color: var(--nubi-color-white);
  border-radius: var(--nubi-radius-full);
  box-shadow: var(--nubi-shadow-lg);
  
  /* Typography */
  font-size: var(--nubi-font-size-base);
  font-weight: var(--nubi-font-weight-semibold);
  line-height: var(--nubi-line-height-tight);
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  
  /* Transitions */
  transition: all 0.3s ease;
  
  /* Focus visible */
  outline: none;
}

.home-action:hover {
  background-color: var(--nubi-color-primary-dark);
  transform: translateX(-50%) scale(1.02);
}

.home-action:active {
  transform: translateX(-50%) scale(0.98);
}

.home-action:focus-visible {
  box-shadow: 0 0 0 3px var(--nubi-color-focus), var(--nubi-shadow-lg);
}

.home-action--has-family {
  background-color: var(--nubi-color-secondary);
  color: var(--nubi-color-black);
}

.home-action--has-family:hover {
  background-color: var(--nubi-color-secondary-dark);
}

.home-action__text {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Portrait: HomeAction cambia de absolute a relative, se posiciona debajo del avatar */
@media (orientation: portrait) {
  .home-action {
    position: relative;
    bottom: auto;
    left: auto;
    transform: none;
    margin-top: var(--nubi-spacing-md);
  }
  
  .home-action:hover {
    transform: scale(1.02);
  }
  
  .home-action:active {
    transform: scale(0.98);
  }
}

/* Responsive: movil */
@media (max-width: 640px) {
  .home-action {
    font-size: var(--nubi-font-size-sm);
    padding: var(--nubi-spacing-xs) var(--nubi-spacing-md);
  }
}

/* Responsive: tablet */
@media (min-width: 641px) and (max-width: 1024px) {
  .home-action {
    font-size: var(--nubi-font-size-base);
  }
}
</style>
