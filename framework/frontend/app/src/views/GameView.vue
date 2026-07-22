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
 * En sprints posteriores: world map, minijuegos, interacción con NPC
 */
</script>

<style scoped>
.game-view {
  padding: 2rem;
}
</style>
