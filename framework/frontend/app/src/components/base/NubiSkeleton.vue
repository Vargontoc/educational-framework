<template>
  <div
    :class="[
      'nubi-skeleton',
      `nubi-skeleton--${variant}`,
      { 'nubi-skeleton--animate': animate }
    ]"
    :style="customStyle"
    role="presentation"
    aria-hidden="true"
  />
</template>

<script setup lang="ts">
/**
 * NubiSkeleton - Placeholder animado para contenido en carga
 * 
 * Variantes:
 * - line: Línea de texto
 * - circle: Avatar o imagen circular
 * - rectangle: Bloque rectangular
 * - card: Card completa
 * 
 * Características:
 * - Animación de pulso suave
 * - Formas configurables
 * - Tamaños personalizables
 * - Accesibilidad (aria-hidden)
 */
import { computed } from 'vue'
interface Props {
  /** Variante del skeleton */
  variant?: 'line' | 'circle' | 'rectangle' | 'card'
  /** Ancho (CSS value) */
  width?: string
  /** Alto (CSS value) */
  height?: string
  /** Animar */
  animate?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'line',
  width: '100%',
  height: 'auto',
  animate: true
})

/**
 * Estilo personalizado según variante
 */
const customStyle = computed(() => {
  const style: Record<string, string> = {
    width: props.width,
    height: props.height
  }
  
  // Alturas por defecto según variante
  if (props.height === 'auto') {
    switch (props.variant) {
      case 'line':
        style.height = '16px'
        break
      case 'circle':
        style.height = props.width
        break
      case 'rectangle':
        style.height = '100px'
        break
      case 'card':
        style.height = '200px'
        break
    }
  }
  
  return style
})
</script>

<style scoped>
.nubi-skeleton {
  background-color: var(--nubi-bg-surface-tertiary);
  position: relative;
  overflow: hidden;
}

/* Variantes */
.nubi-skeleton--line {
  border-radius: var(--nubi-radius-sm);
}

.nubi-skeleton--circle {
  border-radius: var(--nubi-radius-full);
}

.nubi-skeleton--rectangle {
  border-radius: var(--nubi-radius-md);
}

.nubi-skeleton--card {
  border-radius: var(--nubi-radius-lg);
}

/* Animación de pulso */
.nubi-skeleton--animate::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    90deg,
    transparent,
    var(--nubi-color-white-transparent),
    transparent
  );
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}
</style>
