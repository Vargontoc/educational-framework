<template>

    <div  ref="gameContainer" class="game-view"></div>
</template>

<script setup lang="ts">
import { Game } from 'phaser';
import { onMounted, onUnmounted, ref } from 'vue';
import { LoadingScene } from '@/components/game/LoadingScene';
import {  WorldMapScene } from '@/components/game/WorldMapScene';
import { useRoute } from 'vue-router';

const gameContainer = ref(null)
const route = useRoute()

let gameInstance = null as unknown as Game;


const loadPhaserGame = async () => {
    const Phaser = await import('phaser')
    
    const config = {
      type: Phaser.AUTO,
      width: 800,
      height: 600,
      parent: gameContainer.value,
      scene: [LoadingScene, WorldMapScene],
      backgroundColor: "#028af8",
      callbacks: {
        preBoot: (game: { registry: { set: (arg0: string, arg1: string | string[]) => void; }; }) => {
          game.registry.set('childId', route.params.childId )
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

</style>
