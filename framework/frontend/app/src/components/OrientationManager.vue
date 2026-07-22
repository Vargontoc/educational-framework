<template>
  <div class="orientation-manager" :class="{ 'is-portrait': isPortrait }">
    <slot />
  </div>
</template>

<script setup lang="ts">
/**
 * Componente de gestión de orientación
 * 
 * Según ADR-010 y SPRINT-002:
 * - Renderizado horizontal permanente incluso en orientación física vertical
 * - No mostrar indicaciones para girar el dispositivo
 * - Escalado proporcional mediante CSS transform
 * 
 * Estrategia:
 * - Detectar orientación del dispositivo
 * - Aplicar escalado CSS para mantener composición horizontal
 * - No bloquear sensores de movimiento (para futuros minijuegos)
 */

import { ref, onMounted, onUnmounted } from 'vue'

const isPortrait = ref(false)
const scale = ref(1)

/**
 * Detectar orientación actual
 */
function detectOrientation(): void {
  isPortrait.value = window.innerHeight > window.innerWidth
  calculateScale()
}

/**
 * Calcular factor de escala para mantener composición horizontal
 * Cuando está en vertical, escalar para que quepa en el viewport
 */
function calculateScale(): void {
  if (isPortrait.value) {
    // En vertical, escalar para que el ancho del viewport sea el "alto" del diseño horizontal
    const viewportWidth = window.innerWidth
    const viewportHeight = window.innerHeight
    
    // El diseño horizontal espera un aspect ratio landscape (ej: 16:9 o similar)
    // Calculamos el scale para que quepa en el viewport vertical
    const targetWidth = viewportHeight * (16 / 9) // Asumimos aspect ratio 16:9
    scale.value = viewportWidth / targetWidth
  } else {
    scale.value = 1
  }
}

/**
 * Handler de cambio de tamaño/orientación
 */
function handleResize(): void {
  detectOrientation()
}

onMounted(() => {
  detectOrientation()
  
  // Escuchar cambios de orientación y resize
  window.addEventListener('resize', handleResize)
  window.addEventListener('orientationchange', handleResize)
  
  // Intentar usar Screen Orientation API si está disponible
  if (screen.orientation && screen.orientation.addEventListener) {
    screen.orientation.addEventListener('change', handleResize)
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('orientationchange', handleResize)
  
  if (screen.orientation && screen.orientation.removeEventListener) {
    screen.orientation.removeEventListener('change', handleResize)
  }
})

// Exponer scale para que el componente padre pueda aplicarlo
defineExpose({
  isPortrait,
  scale
})
</script>

<style scoped>
.orientation-manager {
  width: 100%;
  height: 100%;
  overflow: hidden;
}

/* 
 * Cuando está en orientación vertical, aplicar escalado
 * El contenido se escala para mantener la composición horizontal
 */
.orientation-manager.is-portrait {
  display: flex;
  align-items: center;
  justify-content: center;
}

.orientation-manager.is-portrait > :deep(*) {
  transform-origin: center center;
  /* El scale se aplica inline desde el componente padre si es necesario */
}
</style>
