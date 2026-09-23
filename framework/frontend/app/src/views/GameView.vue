<template>

    <div  ref="gameContainer" class="game-view"></div>
</template>

<script setup lang="ts">
import Phaser from 'phaser';
import { SpinePlugin } from '@esotericsoftware/spine-phaser-v4';
import { onMounted, onUnmounted, ref, watch } from 'vue';
import { LoadingScene } from '@/game/LoadingScene';
import { BaseStateScene } from '@/game/BaseStateScene';
import { FarewellScene } from '@/game/FarewellScene';
import { OrientationRequiredScene } from '@/game/OrientationRequiredScene';
import { WorldMapScene } from '@/game/WorldMapScene';
import { RecognitionGameScene } from '@/game/RecognitionGameScene';
import { MemoryGameScene } from '@/game/MemoryGameScene';
import { useRoute } from 'vue-router';
import { useGlobalConfig } from '@/composables/useGlobalConfig';
import { useGameOrientation } from '@/composables/useGameOrientation';

const gameContainer = ref(null)
const route = useRoute()
const { persisted: globalConfig } = useGlobalConfig()
const { isPortrait } = useGameOrientation()

let gameInstance = null as unknown as Phaser.Game;

// Import estático (no dinámico) de Phaser: @esotericsoftware/spine-phaser-v4 ya
// importa Phaser de forma estática internamente (y por tanto lo empaqueta de forma
// eager de todos modos, ver worldmap-extensibility.md), así que el import dinámico
// aquí ya no aportaba code-splitting real y arriesgaba una doble instancia del
// módulo "phaser" en el servidor de desarrollo de Vite (una vía este import
// dinámico, otra vía el import estático de spine-phaser-v4) — eso hacía que
// SpinePlugin registrara `add.spine`/`load.spineBinary` en una instancia distinta
// de la que usan las escenas, con el síntoma "this.scene.add.spine is not a function".
const loadPhaserGame = async () => {
    const config = {
      type: Phaser.AUTO,
      width: 1280,
      height: 720,
      parent: gameContainer.value,
      scene: [LoadingScene, BaseStateScene, WorldMapScene, RecognitionGameScene, MemoryGameScene, FarewellScene, OrientationRequiredScene],
      // Canvas transparente: el minijuego pone su fondo (imagen a viewport completo) detras del canvas.
      // El resto de escenas se ven igual: el contenedor .game-view ya tiene el mismo azul.
      transparent: true,
      backgroundColor: "#028af8",
      plugins: {
        scene: [
          { key: 'spine.SpinePlugin', plugin: SpinePlugin, mapping: 'spine' }
        ]
      },
      callbacks: {
        preBoot: (game: { registry: { set: (arg0: string, arg1: unknown) => void; }; }) => {
          game.registry.set('childId', route.params.childId)
          game.registry.set('npcEnabled', globalConfig.value.npcEnabled)
          game.registry.set('voiceEnabled', globalConfig.value.npcVoiceEnabled)
        }
      },
      scale: {
        mode: Phaser.Scale.ENVELOP,
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
        setPreference(key: string, value: boolean) {
          gameInstance.registry?.set(key, value)
        },
        startRecognitionScene() {
          try {
            const scenes = gameInstance.scene?.scenes ?? []
            const active = scenes.find((s: any) => s.scene?.isActive?.()) as any
            if (!active) return
            active.scene.start('recognition-game', {
              websocket: active.websocket,
              biome: active.currentBiome,
              sessionId: active.sessionId,
              childId: active.childId
            })
          } catch { /* noop */ }
        },
        startMemoryScene() {
          try {
            const scenes = gameInstance.scene?.scenes ?? []
            const active = scenes.find((s: any) => s.scene?.isActive?.()) as any
            if (!active) return
            active.scene.start('memory-game', {
              websocket: active.websocket,
              biome: active.currentBiome,
              sessionId: active.sessionId,
              childId: active.childId
            })
          } catch { /* noop */ }
        },
        getMemoryData() {
          try {
            const scenes = gameInstance.scene?.scenes ?? []
            const active = scenes.find((s: any) => s.scene?.isActive?.()) as any
            return active?.getBoardSnapshot?.() ?? null
          } catch { return null }
        },
        tapCard(cardId: string) {
          const scenes = gameInstance.scene?.scenes ?? []
          const active = scenes.find((s: any) => s.scene?.isActive?.()) as any
          active?.cards?.get?.(cardId)?.container?.emit('pointerdown')
        },
        seedAudioBuffer(audioId: string) {
          const cache = gameInstance.registry?.get('audioCache')
          const ctx = new AudioContext()
          cache?.set(audioId, ctx.createBuffer(1, 4410, 44100))
          void ctx.close()
          gameInstance.registry?.get('audioService')?.emit('audio-received', audioId)
        },
        hasAudioBuffer(audioId: string): boolean {
          return gameInstance.registry?.get('audioCache')?.has(audioId) ?? false
        },
        spyDynamicAudio(): string[] {
          const played: string[] = []
          const service = gameInstance.registry?.get('audioService')
          service.playDynamic = async (audioId: string) => { played.push(audioId) }
          w.__NUBI_PLAYED_AUDIO__ = played
          return played
        },
        getScaleInfo() {
          const sc = gameInstance.scale
          return { width: sc.width, height: sc.height, displayScale: sc.displayScale.x, mode: sc.scaleMode }
        },
        getLayoutSizes() {
          const scenes = gameInstance.scene?.scenes ?? []
          const active = scenes.find((s: any) => s.scene?.isActive?.()) as any
          return active?.sizes ? { ...active.sizes } : null
        },
        textureExists(key: string): boolean {
          return gameInstance.textures?.exists(key) ?? false
        },
        tapNubi() {
          const scenes = gameInstance.scene?.scenes ?? []
          const active = scenes.find((s: any) => s.scene?.isActive?.()) as any
          active?.nubiLayer?.nubiSprite?.emit('pointerdown')
        },
        spyWsSend() {
          const scenes = gameInstance.scene?.scenes ?? []
          const active = scenes.find((s: any) => s.scene?.isActive?.()) as any
          const ws = active?.websocket
          if (!ws) return
          const sent: string[] = []
          const original = ws.send.bind(ws)
          ws.send = (data: string) => { sent.push(String(data)) }
          w.__NUBI_SENT__ = sent
          void original
        },
        tapOption(optionIndex: number) {
          const scenes = gameInstance.scene?.scenes ?? []
          const active = scenes.find((s: any) => s.scene?.isActive?.()) as any
          const target = active?.images?.find((img: any) =>
            img.getData?.('optionIndex') === optionIndex && img.type !== 'Text')
          target?.emit('pointerdown')
        },
        tapExitButton() {
          const scenes = gameInstance.scene?.scenes ?? []
          const active = scenes.find((s: any) => s.scene?.isActive?.()) as any
          active?.exitButton?.hitZone?.emit('pointerdown')
        },
        closeWs() {
          try {
            const scenes = gameInstance.scene?.scenes ?? []
            const active = scenes.find((s: any) => s.scene?.isActive?.()) as any
            if (active?.websocket) {
              active.websocket.close()
            }
          } catch { /* noop */ }
        },
        getSceneData() {
          try {
            const scenes = gameInstance.scene?.scenes ?? []
            const active = scenes.find((s: any) => s.scene?.isActive?.()) as any
            if (!active || !active.images) return null
            return {
              images: active.images.map((img: any) => ({
                elementId: img.getData?.('elementId') ?? null,
                x: img.x,
                y: img.y,
                displayWidth: img.displayWidth ?? img.width ?? 0,
                displayHeight: img.displayHeight ?? img.height ?? 0,
                inputEnabled: img.input?.enabled ?? false,
                type: img.type ?? 'unknown',
                alpha: img.alpha ?? 1.0,
                isStimulus: img.getData?.('isStimulus') ?? false,
                tint: img.isTinted ? img.tintTopLeft : null,
                textureKey: img.texture?.key ?? null,
                scalePercent: img.getData?.('scalePercent') ?? null,
                optionIndex: img.getData?.('optionIndex') ?? null,
                // tactile side in logical units (the hit area lives in the object's local, unscaled space)
                hitDisplayWidth: img.input?.hitArea ? img.input.hitArea.width * Math.abs(img.scaleX ?? 1) : null,
                // composite (COLOR): splash + item children, relative to the container centre
                children: Array.isArray(img.list)
                  ? img.list.map((c: any) => ({
                      name: c.name,
                      key: c.texture?.key ?? null,
                      x: c.x,
                      y: c.y,
                      width: c.displayWidth,
                      height: c.displayHeight
                    }))
                  : null
              })),
              selectedColorItem: active.selectedColorItem ?? '',
              showIcon: active.showIcon ?? null,
              comparisonMode: active.comparisonMode ?? false,
              backgroundKey: active.backgroundKey ?? null,
              minElementHitSize: active.minElementHitSize ?? null,
              startingGame: active.startingGame ?? null,
              touchEnableTimerActive: !!active.touchEnableTimer,
              guideChromGraphicsExists: !!active.guideChromGraphics,
              recognitionCategory: active.recognitionCategory ?? ''
            }
          } catch { return null }
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
