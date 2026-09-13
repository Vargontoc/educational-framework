import { Scene } from "phaser"
import { WORLD_MAP_CONFIG, isDistantBiomePair } from "../config/worldMapConfig"

const TRANSITION_OVERLAY_DEPTH = 200
const TRANSITION_COLOR = 0x000000

export interface BiomeTransitionContext {
    scene: Scene
    fromBiome: string
    toBiome: string
}

export interface BiomeTransitionResult {
    fadeOverlay: Phaser.GameObjects.Rectangle
    duration: number
    isDistant: boolean
}

export function isDistantTransition(from: string, to: string): boolean {
    return isDistantBiomePair(from, to)
}

export function getArrivalFadeDuration(from: string, to: string): number {
    if (isDistantTransition(from, to)) {
        return WORLD_MAP_CONFIG.arrivalFadeDistant
    }
    return WORLD_MAP_CONFIG.arrivalFadeShort
}

export function createArrivalOverlay(context: BiomeTransitionContext): BiomeTransitionResult {
    const { scene, fromBiome, toBiome } = context
    const { viewportWidth, viewportHeight } = WORLD_MAP_CONFIG

    const fadeOverlay = scene.add.rectangle(
        viewportWidth / 2,
        viewportHeight / 2,
        viewportWidth,
        viewportHeight,
        TRANSITION_COLOR,
        0
    )
    fadeOverlay.setDepth(TRANSITION_OVERLAY_DEPTH)
    fadeOverlay.setScrollFactor(0)

    const duration = getArrivalFadeDuration(fromBiome, toBiome)
    const distant = isDistantTransition(fromBiome, toBiome)

    return { fadeOverlay, duration, isDistant: distant }
}

export function fadeToBlack(
    context: BiomeTransitionContext,
    onComplete: () => void
): { overlay: Phaser.GameObjects.Rectangle; duration: number } {
    const { scene } = context
    const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
    const result = createArrivalOverlay(context)

    if (reducedMotion) {
        result.fadeOverlay.setAlpha(1)
        onComplete()
        return { overlay: result.fadeOverlay, duration: 0 }
    }

    scene.tweens.add({
        targets: result.fadeOverlay,
        alpha: 1,
        duration: result.duration,
        ease: 'Sine.easeIn',
        onComplete
    })

    return { overlay: result.fadeOverlay, duration: result.duration }
}

export function fadeFromBlack(
    scene: Scene,
    overlay: Phaser.GameObjects.Rectangle,
    duration: number,
    onComplete?: () => void
): void {
    const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches

    if (reducedMotion) {
        overlay.destroy()
        onComplete?.()
        return
    }

    scene.tweens.add({
        targets: overlay,
        alpha: 0,
        duration,
        ease: 'Sine.easeOut',
        onComplete: () => {
            overlay.destroy()
            onComplete?.()
        }
    })
}
