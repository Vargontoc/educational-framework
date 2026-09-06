<template>

    <div  ref="gameContainer" class="game-view"></div>
</template>

<script setup lang="ts">
import { Game } from 'phaser';
import { onMounted, onUnmounted, ref } from 'vue';
import { LoadingScene } from '@/components/game/LoadingScene';
import { BaseStateScene } from '@/components/game/BaseStateScene';
import { FarewellScene } from '@/components/game/FarewellScene';
import { useRoute } from 'vue-router';
import { useGlobalConfig } from '@/composables/useGlobalConfig';

const gameContainer = ref(null)
const route = useRoute()
const { persisted: globalConfig } = useGlobalConfig()

let gameInstance = null as unknown as Game;


const loadPhaserGame = async () => {
    const Phaser = await import('phaser')
    
    const config = {
      type: Phaser.AUTO,
      width: 800,
      height: 600,
      parent: gameContainer.value,
      scene: [LoadingScene, BaseStateScene, FarewellScene],
      backgroundColor: "#028af8",
      callbacks: {
        preBoot: (game: { registry: { set: (arg0: string, arg1: unknown) => void; }; }) => {
          game.registry.set('childId', route.params.childId)
          game.registry.set('npcEnabled', globalConfig.value.npcEnabled)
          game.registry.set('voiceEnabled', globalConfig.value.npcVoiceEnabled)
        }
      },
      scale: {
        mode: Phaser.Scale.FIT,
        autoCenter: Phaser.Scale.CENTER_BOTH
    }
  }
    
    gameInstance = new Phaser.Game(config)
}

onMounted(() => {
  loadPhaserGame()
})

onUnmounted(() => {
  if(gameInstance) {
    gameInstance.destroy(true)
  }
})
</script>

<style scoped>
.game-view {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background-color: var(--nubi-bg-surface-secondary);
}

.game-view :deep(canvas) {
  max-width: 100%;
  max-height: 100%;
  touch-action: none;
}

@media (max-width: 480px) {
  .game-view {
    height: 100dvh;
  }
}

@media (min-width: 768px) and (max-width: 1024px) {
  .game-view {
    padding: var(--nubi-spacing-md);
  }
}
</style>
