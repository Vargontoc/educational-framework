<template>

    <div  ref="gameContainer" class="game-view"></div>
</template>

<script setup lang="ts">
import { Game } from 'phaser';
import { onMounted, onUnmounted, ref, watch } from 'vue';
import { LoadingScene } from '@/components/game/LoadingScene';
import { BaseStateScene } from '@/components/game/BaseStateScene';
import { FarewellScene } from '@/components/game/FarewellScene';
import { OrientationRequiredScene } from '@/components/game/OrientationRequiredScene';
import { useRoute } from 'vue-router';
import { useGlobalConfig } from '@/composables/useGlobalConfig';
import { useGameOrientation } from '@/composables/useGameOrientation';

const gameContainer = ref(null)
const route = useRoute()
const { persisted: globalConfig } = useGlobalConfig()
const { isPortrait } = useGameOrientation()

let gameInstance = null as unknown as Game;


const loadPhaserGame = async () => {
    const Phaser = await import('phaser')
    
    const config = {
      type: Phaser.AUTO,
      width: 1280,
      height: 720,
      parent: gameContainer.value,
      scene: [LoadingScene, BaseStateScene, FarewellScene, OrientationRequiredScene],
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

    watch(isPortrait, (newVal) => {
      if (!gameInstance) return

      const scenePlugin = gameInstance.scene

      if (newVal) {
        const activeScene = scenePlugin.scenes.find((s: any) => s.scene.isActive())
        if (activeScene && activeScene.scene.key !== 'orientation-required') {
          gameInstance.registry.set('previousSceneKey', activeScene.scene.key)
          activeScene.scene.pause()
          scenePlugin.start('orientation-required')
        }
      } else {
        const previousKey = gameInstance.registry.get('previousSceneKey') as string
        if (previousKey) {
          scenePlugin.stop('orientation-required')
          const previousScene = scenePlugin.getScene(previousKey)
          if (previousScene) {
            previousScene.scene.resume()
          }
          gameInstance.registry.set('previousSceneKey', null)
        }
      }
    })

    if (typeof window !== 'undefined' && (window as any).Cypress) {
      const w = window as any
      w.__NUBI_GAME_STATE__ = {
        get childId() { return gameInstance.registry?.get('childId') ?? null },
        get npcEnabled() { return gameInstance.registry?.get('npcEnabled') ?? false },
        get ttsEnabled() { return gameInstance.registry?.get('ttsEnabled') ?? false },
        get activeScene() {
          try {
            const scenes = gameInstance.scene?.scenes ?? []
            const active = scenes.find((s: any) => s.scene?.isActive?.())
            return active?.scene?.key ?? null
          } catch { return null }
        },
        get sceneKeys() {
          try {
            return gameInstance.scene?.scenes?.map((s: any) => s.scene?.key).filter(Boolean) ?? []
          } catch { return [] }
        },
        get wsReadyState(): number | null {
          try {
            const scenes = gameInstance.scene?.scenes ?? []
            const active = scenes.find((s: any) => s.scene?.isActive?.()) as any
            return active?.websocket?.readyState ?? null
          } catch { return null }
        },
        injectWsEvent(event: unknown) {
          try {
            const scenes = gameInstance.scene?.scenes ?? []
            const active = scenes.find((s: any) => s.scene?.isActive?.()) as any
            if (active && typeof active.readEvent === 'function') {
              active.readEvent(event)
            }
          } catch { /* noop */ }
        },
        closeWs() {
          try {
            const scenes = gameInstance.scene?.scenes ?? []
            const active = scenes.find((s: any) => s.scene?.isActive?.()) as any
            if (active?.websocket) {
              active.websocket.close()
            }
          } catch { /* noop */ }
        }
      }
    }
}

onMounted(() => {
  loadPhaserGame()
})

onUnmounted(() => {
  if (typeof window !== 'undefined' && (window as any).Cypress) {
    delete (window as any).__NUBI_GAME_STATE__
  }
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
  background-color: #028af8;
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
