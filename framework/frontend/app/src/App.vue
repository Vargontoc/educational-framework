<template>
  <div class="app-wrapper">
    <router-view />
  </div>
</template>

<script setup lang="ts">
/**
 * Componente raíz de la aplicación
 * 
 * Según ADR-019 y SPRINT-010:
 * - Rediseño portrait real con estilos específicos por orientación
 * - No se usan rotaciones CSS ni escalados complejos
 * - El contenido se reacomoda naturalmente según la orientación
 * - Preservación de estado ante giro, segundo plano y retorno
 */

import { onMounted, onUnmounted } from 'vue'
import { useTheme } from './composables/useTheme'

// Inicializar el sistema de temas
useTheme()

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

/* Wrapper principal con dimensiones del viewport */
.app-wrapper {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background: var(--nubi-bg-surface, #ffffff);
}
</style>
