<template>
  <div class="game-view">
    <h1>{{ $t('views.game.title') }}</h1>
    <p>{{ $t('views.game.description') }}</p>
    <p>{{ $t('views.game.childId', { childId: $route.params.childId }) }}</p>
  </div>
</template>

<script setup lang="ts">
/**
 * Vista GameView
 * Vista principal de juego para el niño
 * 
 * SPRINT-021: Documentación para implementación futura
 * 
 * Cuando se implemente el juego con Phaser:
 * - Phaser debe cargarse DINÁMICAMENTE para no afectar el bundle inicial
 * - NO usar import estático: import Phaser from 'phaser' ❌
 * - USAR import dinámico: const Phaser = await import('phaser') ✅
 * 
 * Ejemplo de implementación futura:
 * ```
 * const loadPhaserGame = async () => {
 *   const Phaser = await import('phaser')
 *   
 *   const config = {
 *     type: Phaser.AUTO,
 *     width: 800,
 *     height: 600,
 *     // ... configuración del juego
 *   }
 *   
 *   const game = new Phaser.Game(config)
 * }
 * ```
 * 
 * NOTA: Cuando Phaser se use, añadir en vite.config.ts:
 * manualChunks: { 'vendor-phaser': ['phaser'] }
 */
</script>

<style scoped>
.game-view {
  padding: 2rem;
}
</style>
