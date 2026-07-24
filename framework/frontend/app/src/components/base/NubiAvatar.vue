<template>
  <div
    :class="['nubi-avatar', `nubi-avatar--${size}`]"
    :style="avatarStyle"
    role="img"
    :aria-label="ariaLabel"
  >
    <img
      v-if="src && !hasError"
      :src="src"
      :alt="alt || name"
      class="nubi-avatar__image"
      @error="handleError"
    />
    <span v-else class="nubi-avatar__fallback">
      {{ initials }}
    </span>
  </div>
</template>

<script setup lang="ts">
/**
 * NubiAvatar - Avatar circular/redondeado con fallback de iniciales
 * 
 * Características:
 * - 3 tamaños: sm (32px), md (48px), lg (64px)
 * - Imagen circular con fallback automático
 * - Iniciales generadas desde el nombre
 * - Color de fondo basado en el nombre
 * - Accesibilidad con role="img"
 */

import { computed, ref } from 'vue'

interface Props {
  /** URL de la imagen */
  src?: string
  /** Alt text para la imagen */
  alt?: string
  /** Nombre para generar iniciales */
  name?: string
  /** Tamaño del avatar */
  size?: 'sm' | 'md' | 'lg'
}

const props = withDefaults(defineProps<Props>(), {
  src: '',
  alt: '',
  name: '',
  size: 'md'
})

const hasError = ref(false)

function handleError() {
  hasError.value = true
}

/**
 * Genera las iniciales desde el nombre
 */
const initials = computed(() => {
  if (!props.name) return '?'
  
  const words = props.name.trim().split(/\s+/)
  if (words.length === 1) {
    return words[0].charAt(0).toUpperCase()
  }
  
  return (words[0].charAt(0) + words[words.length - 1].charAt(0)).toUpperCase()
})

/**
 * Genera un color de fondo basado en el nombre
 */
const backgroundColor = computed(() => {
  if (!props.name) return 'var(--nubi-color-primary)'
  
  const colors = [
    'var(--nubi-color-primary)',
    'var(--nubi-color-secondary)',
    'var(--nubi-color-accent)',
    'var(--nubi-color-info)',
    'var(--nubi-color-success)',
    'var(--nubi-color-warning)'
  ]
  
  let hash = 0
  for (let i = 0; i < props.name.length; i++) {
    hash = props.name.charCodeAt(i) + ((hash << 5) - hash)
  }
  
  return colors[Math.abs(hash) % colors.length]
})

const avatarStyle = computed(() => ({
  backgroundColor: props.src && !hasError.value ? 'transparent' : backgroundColor.value
}))

const ariaLabel = computed(() => props.alt || props.name || 'Avatar')
</script>

<style scoped>
.nubi-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--nubi-radius-full);
  overflow: hidden;
  flex-shrink: 0;
  font-weight: var(--nubi-font-weight-semibold);
  color: var(--nubi-color-white);
}

.nubi-avatar--sm {
  width: 32px;
  height: 32px;
  font-size: var(--nubi-font-size-xs);
}

.nubi-avatar--md {
  width: 48px;
  height: 48px;
  font-size: var(--nubi-font-size-base);
}

.nubi-avatar--lg {
  width: 64px;
  height: 64px;
  font-size: var(--nubi-font-size-xl);
}

.nubi-avatar__image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.nubi-avatar__fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  user-select: none;
}
</style>
