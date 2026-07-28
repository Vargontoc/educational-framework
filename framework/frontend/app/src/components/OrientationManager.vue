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

/**
 * Dimensiones base del diseño (canvas de diseño)
 * Aspect ratio 16:9 estándar para tablet landscape
 */
const DESIGN_WIDTH = 1280
const DESIGN_HEIGHT = 720

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
 * Cuando está en vertical, rotar 90° y escalar para llenar el viewport
 * El diseño base es 1280x720 (landscape), al rotar se intercambia: 720x1280
 */
function calculateScale(): void {
  if (isPortrait.value) {
    const viewportWidth = window.innerWidth
    const viewportHeight = window.innerHeight
    
    // Al rotar 90°, las dimensiones del diseño se intercambian:
    // - El ancho del diseño (1280) se convierte en alto visual
    // - El alto del diseño (720) se convierte en ancho visual
    // Calcular scale para que el contenido rotado llene completamente el viewport
    const scaleX = viewportWidth / DESIGN_HEIGHT // 720 -> ancho
    const scaleY = viewportHeight / DESIGN_WIDTH // 1280 -> alto
    
    // Usar max para llenar completamente el viewport (puede haber recorte mínimo)
    // O usar min para mantener aspect ratio completo (puede haber espacios)
    // Para adaptación al viewport real, usamos max
    scale.value = Math.max(scaleX, scaleY)
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

// Exponer scale y constantes de diseño para que el componente padre pueda aplicarlos
defineExpose({
  isPortrait,
  scale,
  DESIGN_WIDTH,
  DESIGN_HEIGHT
})
</script>

<style scoped>
.orientation-manager {
  width: 100%;
  height: 100%;
}

/* 
 * Cuando está en orientación vertical, centrar el contenido escalado
 */
.orientation-manager.is-portrait {
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
