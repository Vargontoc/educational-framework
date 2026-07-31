<template>
  <NubiCard class="config-section" :class="{ 'config-section--disabled': disabled }">
    <template #header>
      <div class="config-section__header">
        <h3 class="config-section__title">{{ title }}</h3>
        <p v-if="description" class="config-section__description">{{ description }}</p>
      </div>
    </template>
    <div class="config-section__content">
      <slot />
    </div>
  </NubiCard>
</template>

<script setup lang="ts">
/**
 * ConfigSection - Wrapper reutilizable para secciones de configuración
 * 
 * Características:
 * - Usa NubiCard como contenedor base
 * - Título y descripción opcional
 * - Estado disabled con opacidad reducida
 * - Slot para contenido de controles
 * 
 * Accesibilidad:
 * - Estructura semántica con h3
 * - Descripción asociada visualmente
 */

import NubiCard from '../base/NubiCard.vue'

interface Props {
  /** Título de la sección */
  title: string
  /** Descripción breve (opcional) */
  description?: string
  /** Estado disabled */
  disabled?: boolean
}

withDefaults(defineProps<Props>(), {
  description: '',
  disabled: false
})
</script>

<style scoped>
.config-section {
  margin-bottom: var(--nubi-spacing-md);
}

.config-section--disabled {
  opacity: 0.6;
  pointer-events: none;
}

.config-section__header {
  display: flex;
  flex-direction: column;
  gap: var(--nubi-spacing-xs);
}

.config-section__title {
  font-size: var(--nubi-font-size-lg);
  font-weight: var(--nubi-font-weight-semibold);
  color: var(--nubi-text-primary);
  margin: 0;
  line-height: var(--nubi-line-height-tight);
}

.config-section__description {
  font-size: var(--nubi-font-size-sm);
  color: var(--nubi-text-secondary);
  margin: 0;
  line-height: var(--nubi-line-height-normal);
}

.config-section__content {
  margin-top: var(--nubi-spacing-sm);
}
</style>
