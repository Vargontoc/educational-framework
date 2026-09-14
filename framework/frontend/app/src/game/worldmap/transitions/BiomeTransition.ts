import { Scene } from "phaser"
import { WORLD_MAP_CONFIG, isDistantBiomePair } from "../config/worldMapConfig"

const TRANSITION_OVERLAY_DEPTH = 200
const TRANSITION_COLOR = 0x000000
// Bajo prefers-reduced-motion se acorta el fundido, pero nunca se elimina:
// un salto instantáneo a negro sólido (y de vuelta) sin ninguna animación
// se percibe como que la app se ha quedado colgada, no como una transición
// intencional — un fundido de opacidad corto no es el tipo de movimiento
// (parallax, zoom, giro) que reduced-motion pretende evitar.
const REDUCED_MOTION_FADE_DURATION = 200

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
    const duration = reducedMotion ? REDUCED_MOTION_FADE_DURATION : result.duration

    scene.tweens.add({
        targets: result.fadeOverlay,
        alpha: 1,
        duration,
        ease: 'Sine.easeIn',
        onComplete
    })

    return { overlay: result.fadeOverlay, duration }
}

export function fadeFromBlack(
    scene: Scene,
    overlay: Phaser.GameObjects.Rectangle,
    duration: number,
    onComplete?: () => void
): void {
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
