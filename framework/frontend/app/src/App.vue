<template>
  <OrientationManager ref="orientationManager">
    <div 
      class="app-container" 
      :style="orientationStyle"
    >
      <router-view />
    </div>
  </OrientationManager>
</template>

<script setup lang="ts">
/**
 * Componente raíz de la aplicación
 * 
 * Según ADR-010 y SPRINT-002:
 * - Renderizado horizontal permanente
 * - Escalado proporcional en orientación vertical
 * - Preservación de estado ante giro, segundo plano y retorno
 */

import { ref, computed, onMounted, onUnmounted } from 'vue'
import OrientationManager from './components/OrientationManager.vue'
import { useTheme } from './composables/useTheme'

// Inicializar el sistema de temas
useTheme()

const orientationManager = ref<InstanceType<typeof OrientationManager> | null>(null)

/**
 * Estilo computado para aplicar escalado en orientación vertical
 */
const orientationStyle = computed(() => {
  if (!orientationManager.value) return {}
  
  const { isPortrait, scale } = orientationManager.value
  
  if (isPortrait && scale < 1) {
    return {
      transform: `scale(${scale})`,
      transformOrigin: 'center center',
      width: `${100 / scale}%`,
      height: `${100 / scale}%`
    }
  }
  
  return {}
})

/**
 * Handler de visibilidad (segundo plano / retorno)
 * Preserva el estado de la aplicación
 */
function handleVisibilityChange(): void {
  if (document.hidden) {
    // Aplicación en segundo plano
    // El estado ya se preserva en stores con sessionStorage
    console.debug('App in background')
  } else {
    // Aplicación en primer plano
    console.debug('App in foreground')
  }
}

onMounted(() => {
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

onUnmounted(() => {
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})
</script>

<style>
/* Estilos globales se definen en style.css */
.app-container {
  width: 100%;
  height: 100%;
  transition: transform 0.3s ease;
}
</style>
