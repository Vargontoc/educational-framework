<template>
  <div :class="['nubi-card', { 'nubi-card--clickable': clickable }]">
    <div v-if="$slots.image || image" class="nubi-card__image">
      <slot name="image">
        <img v-if="image" :src="image" :alt="imageAlt" />
      </slot>
    </div>
    
    <div class="nubi-card__content">
      <div v-if="title || $slots.header" class="nubi-card__header">
        <slot name="header">
          <h3 class="nubi-card__title">{{ title }}</h3>
        </slot>
      </div>
      
      <div v-if="description || $slots.default" class="nubi-card__body">
        <slot>
          <p class="nubi-card__description">{{ description }}</p>
        </slot>
      </div>
      
      <div v-if="$slots.footer || $slots.actions" class="nubi-card__footer">
        <slot name="footer">
          <div class="nubi-card__actions">
            <slot name="actions" />
          </div>
        </slot>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * NubiCard - Contenedor de contenido con estructura flexible
 * 
 * Características:
 * - Imagen opcional en la parte superior
 * - Título y descripción
 * - Slots para header, body, footer y actions
 * - Variante clickable con hover effect
 * - Accesibilidad WCAG AA
 */

interface Props {
  /** URL de la imagen (opcional) */
  image?: string
  /** Alt text para la imagen */
  imageAlt?: string
  /** Título de la card */
  title?: string
  /** Descripción de la card */
  description?: string
  /** Si la card es clickeable */
  clickable?: boolean
}

withDefaults(defineProps<Props>(), {
  image: '',
  imageAlt: '',
  title: '',
  description: '',
  clickable: false
})

defineEmits<{
  click: []
}>()
</script>

<style scoped>
.nubi-card {
  background-color: var(--nubi-bg-surface);
  border: 1px solid var(--nubi-border-default);
  border-radius: var(--nubi-radius-lg);
  overflow: hidden;
  transition: box-shadow var(--nubi-duration-fast) var(--nubi-ease-in-out),
              transform var(--nubi-duration-fast) var(--nubi-ease-in-out);
}

.nubi-card--clickable {
  cursor: pointer;
}

.nubi-card--clickable:hover {
  box-shadow: var(--nubi-shadow-md);
  transform: translateY(-2px);
}

.nubi-card--clickable:focus-visible {
  outline: none;
  box-shadow: 0 0 0 3px var(--nubi-color-focus);
}

.nubi-card__image {
  width: 100%;
  aspect-ratio: 16 / 9;
  overflow: hidden;
  background-color: var(--nubi-bg-surface-secondary);
}

.nubi-card__image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.nubi-card__content {
  padding: var(--nubi-spacing-md);
}

.nubi-card__header {
  margin-bottom: var(--nubi-spacing-sm);
}

.nubi-card__title {
  font-size: var(--nubi-font-size-lg);
  font-weight: var(--nubi-font-weight-semibold);
  color: var(--nubi-text-primary);
  margin: 0;
  line-height: var(--nubi-line-height-tight);
}

.nubi-card__body {
  margin-bottom: var(--nubi-spacing-md);
}

.nubi-card__description {
  font-size: var(--nubi-font-size-base);
  color: var(--nubi-text-secondary);
  margin: 0;
  line-height: var(--nubi-line-height-normal);
}

.nubi-card__footer {
  padding-top: var(--nubi-spacing-sm);
  border-top: 1px solid var(--nubi-border-default);
}

.nubi-card__actions {
  display: flex;
  gap: var(--nubi-spacing-sm);
  justify-content: flex-end;
}
</style>
