<template>
  <div class="nubi-empty-state">
    <div v-if="$slots.icon || icon" class="nubi-empty-state__icon">
      <slot name="icon">
        <NubiIcon v-if="icon" :name="icon" :size="64" />
      </slot>
    </div>
    
    <h3 class="nubi-empty-state__title">
      {{ title }}
    </h3>
    
    <p v-if="description || $slots.description" class="nubi-empty-state__description">
      <slot name="description">
        {{ description }}
      </slot>
    </p>
    
    <div v-if="$slots.action || actionLabel" class="nubi-empty-state__action">
      <slot name="action">
        <NubiButton v-if="actionLabel" variant="primary" @click="$emit('action')">
          {{ actionLabel }}
        </NubiButton>
      </slot>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * NubiEmptyState - Vista para cuando no hay datos
 * 
 * Características:
 * - Icono opcional (o slot personalizado)
 * - Título y descripción
 * - Acción sugerida (botón opcional)
 * - Totalmente personalizable con slots
 * - Accesibilidad completa
 */

import NubiIcon from './NubiIcon.vue'
import NubiButton from './NubiButton.vue'

interface Props {
  /** Título del empty state */
  title: string
  /** Descripción opcional */
  description?: string
  /** Nombre del icono (opcional) */
  icon?: string
  /** Label del botón de acción (opcional) */
  actionLabel?: string
}

defineProps<Props>()

defineEmits<{
  action: []
}>()
</script>

<style scoped>
.nubi-empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: var(--nubi-spacing-2xl);
  gap: var(--nubi-spacing-lg);
  min-height: 300px;
}

.nubi-empty-state__icon {
  color: var(--nubi-text-tertiary);
  margin-bottom: var(--nubi-spacing-md);
}

.nubi-empty-state__title {
  font-size: var(--nubi-font-size-xl);
  font-weight: var(--nubi-font-weight-semibold);
  color: var(--nubi-text-primary);
  margin: 0;
}

.nubi-empty-state__description {
  font-size: var(--nubi-font-size-base);
  color: var(--nubi-text-secondary);
  margin: 0;
  max-width: 400px;
  line-height: var(--nubi-line-height-relaxed);
}

.nubi-empty-state__action {
  margin-top: var(--nubi-spacing-md);
}
</style>
