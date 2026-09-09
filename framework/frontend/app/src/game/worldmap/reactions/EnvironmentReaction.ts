import { Scene } from "phaser"
import { WORLD_MAP_CONFIG } from "../config/worldMapConfig"

const SCALE_PULSE_TO = 1.2
const SCALE_LEG_DURATION = 220
const REDUCED_MOTION_FADE_DURATION = 400
const FLASH_COLOR = 0xffffff
const PARTICLE_COLOR = 0xffffff

export type ReactionTarget = Phaser.GameObjects.Shape | Phaser.GameObjects.Image

export class EnvironmentReaction {
    private scene: Scene

    constructor(scene: Scene) {
        this.scene = scene
    }

    play(target: ReactionTarget) {
        this.scene.tweens.killTweensOf(target)

        const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
        if (reducedMotion) {
            this.playReducedMotion(target)
            return
        }

        this.playScalePulse(target)
        this.playTintFlash(target)
        this.spawnFadingParticle(target)
    }

    private playReducedMotion(target: ReactionTarget) {
        target.setAlpha(0.5)
        this.scene.tweens.add({
            targets: target,
            alpha: 1,
            duration: REDUCED_MOTION_FADE_DURATION,
            ease: 'Sine.easeInOut'
        })
    }

    private playScalePulse(target: ReactionTarget) {
        target.setScale(1)
        this.scene.tweens.add({
            targets: target,
            scaleX: SCALE_PULSE_TO,
            scaleY: SCALE_PULSE_TO,
            duration: SCALE_LEG_DURATION,
            yoyo: true,
            ease: 'Sine.easeOut'
        })
    }

    private playTintFlash(target: ReactionTarget) {
        const shapeWithFill = target as unknown as { fillColor?: number; setFillStyle?: (color: number, alpha?: number) => unknown }
        if (typeof shapeWithFill.setFillStyle !== 'function' || shapeWithFill.fillColor === undefined) return

        const originalColor = shapeWithFill.fillColor
        shapeWithFill.setFillStyle(FLASH_COLOR)

        this.scene.time.delayedCall(WORLD_MAP_CONFIG.reactionMinDuration, () => {
            if (target.active) {
                shapeWithFill.setFillStyle?.(originalColor)
            }
        })
    }

    private spawnFadingParticle(target: ReactionTarget) {
        const particle = this.scene.add.circle(target.x, target.y - 30, 6, PARTICLE_COLOR, 0.9)
        target.parentContainer?.add(particle)

        this.scene.tweens.add({
            targets: particle,
            y: particle.y - 30,
            scale: 1.8,
            alpha: 0,
            duration: WORLD_MAP_CONFIG.reactionMaxDuration,
            ease: 'Sine.easeOut',
            onComplete: () => particle.destroy()
        })
    }
}
