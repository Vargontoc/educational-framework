import { Scene } from "phaser"
import { WORLD_MAP_CONFIG } from "../config/worldMapConfig"

const EDGE_HINT_DEPTH = 50
const EDGE_GLOW_WIDTH = 80
const EDGE_GLOW_ALPHA_ACTIVE = 0.35
const EDGE_GLOW_ALPHA_IDLE = 0
const EDGE_COLOR = 0xfff9c4
const REDUCED_MOTION_ALPHA = 0.25
const FADE_IN_DURATION = 400
const FADE_OUT_DURATION = 300

export class EdgeHintLayer {
    private scene: Scene
    private glowLeft?: Phaser.GameObjects.Graphics
    private glowRight?: Phaser.GameObjects.Graphics
    private active = false
    private reducedMotion: boolean
    private rightEdgeSuppressed = false

    constructor(scene: Scene) {
        this.scene = scene
        this.reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
    }

    setRightEdgeSuppressed(suppressed: boolean): void {
        this.rightEdgeSuppressed = suppressed
        if (suppressed && this.glowRight) {
            this.glowRight.setAlpha(EDGE_GLOW_ALPHA_IDLE)
        }
    }

    create(): void {
        this.destroy()

        const { viewportWidth, viewportHeight } = WORLD_MAP_CONFIG

        this.glowLeft = this.scene.add.graphics()
        this.glowLeft.setDepth(EDGE_HINT_DEPTH)
        this.drawEdgeGlow(this.glowLeft, 0, 0, EDGE_GLOW_WIDTH, viewportHeight, 'left')
        this.glowLeft.setAlpha(EDGE_GLOW_ALPHA_IDLE)

        this.glowRight = this.scene.add.graphics()
        this.glowRight.setDepth(EDGE_HINT_DEPTH)
        this.drawEdgeGlow(this.glowRight, viewportWidth - EDGE_GLOW_WIDTH, 0, EDGE_GLOW_WIDTH, viewportHeight, 'right')
        this.glowRight.setAlpha(EDGE_GLOW_ALPHA_IDLE)
    }

    updateProximity(offset: number, maxScrollOffset: number): void {
        if (!this.glowLeft || !this.glowRight) return

        const threshold = WORLD_MAP_CONFIG.edgeProximityThreshold
        const remainingRight = maxScrollOffset - offset
        const nearRightEdge = !this.rightEdgeSuppressed && remainingRight <= threshold && maxScrollOffset > 0
        const nearLeftEdge = offset <= threshold && maxScrollOffset > 0

        const shouldActivate = nearLeftEdge || nearRightEdge

        if (shouldActivate && !this.active) {
            this.activate(nearLeftEdge, nearRightEdge)
        } else if (!shouldActivate && this.active) {
            this.deactivate()
        } else if (shouldActivate && this.active) {
            this.updateActiveSides(nearLeftEdge, nearRightEdge)
        }
    }

    private activate(nearLeft: boolean, nearRight: boolean): void {
        this.active = true

        if (this.reducedMotion) {
            if (nearLeft) this.glowLeft?.setAlpha(REDUCED_MOTION_ALPHA)
            if (nearRight) this.glowRight?.setAlpha(REDUCED_MOTION_ALPHA)
            return
        }

        if (nearLeft) {
            this.scene.tweens.add({
                targets: this.glowLeft,
                alpha: EDGE_GLOW_ALPHA_ACTIVE,
                duration: FADE_IN_DURATION,
                ease: 'Sine.easeOut'
            })
        }
        if (nearRight) {
            this.scene.tweens.add({
                targets: this.glowRight,
                alpha: EDGE_GLOW_ALPHA_ACTIVE,
                duration: FADE_IN_DURATION,
                ease: 'Sine.easeOut'
            })
        }
    }

    private deactivate(): void {
        this.active = false

        if (this.reducedMotion) {
            this.glowLeft?.setAlpha(EDGE_GLOW_ALPHA_IDLE)
            this.glowRight?.setAlpha(EDGE_GLOW_ALPHA_IDLE)
            return
        }

        this.scene.tweens.add({
            targets: [this.glowLeft, this.glowRight].filter(Boolean),
            alpha: EDGE_GLOW_ALPHA_IDLE,
            duration: FADE_OUT_DURATION,
            ease: 'Sine.easeIn'
        })
    }

    private updateActiveSides(nearLeft: boolean, nearRight: boolean): void {
        if (this.reducedMotion) {
            this.glowLeft?.setAlpha(nearLeft ? REDUCED_MOTION_ALPHA : EDGE_GLOW_ALPHA_IDLE)
            this.glowRight?.setAlpha(nearRight ? REDUCED_MOTION_ALPHA : EDGE_GLOW_ALPHA_IDLE)
            return
        }

        const leftAlpha = this.glowLeft?.alpha ?? EDGE_GLOW_ALPHA_IDLE
        if (nearLeft && leftAlpha < EDGE_GLOW_ALPHA_ACTIVE && this.glowLeft) {
            this.scene.tweens.add({
                targets: this.glowLeft,
                alpha: EDGE_GLOW_ALPHA_ACTIVE,
                duration: FADE_IN_DURATION,
                ease: 'Sine.easeOut'
            })
        } else if (!nearLeft && leftAlpha > EDGE_GLOW_ALPHA_IDLE && this.glowLeft) {
            this.scene.tweens.add({
                targets: this.glowLeft,
                alpha: EDGE_GLOW_ALPHA_IDLE,
                duration: FADE_OUT_DURATION,
                ease: 'Sine.easeIn'
            })
        }

        const rightAlpha = this.glowRight?.alpha ?? EDGE_GLOW_ALPHA_IDLE
        if (nearRight && rightAlpha < EDGE_GLOW_ALPHA_ACTIVE && this.glowRight) {
            this.scene.tweens.add({
                targets: this.glowRight,
                alpha: EDGE_GLOW_ALPHA_ACTIVE,
                duration: FADE_IN_DURATION,
                ease: 'Sine.easeOut'
            })
        } else if (!nearRight && rightAlpha > EDGE_GLOW_ALPHA_IDLE && this.glowRight) {
            this.scene.tweens.add({
                targets: this.glowRight,
                alpha: EDGE_GLOW_ALPHA_IDLE,
                duration: FADE_OUT_DURATION,
                ease: 'Sine.easeIn'
            })
        }
    }

    private drawEdgeGlow(
        graphics: Phaser.GameObjects.Graphics,
        x: number,
        y: number,
        width: number,
        height: number,
        side: 'left' | 'right'
    ): void {
        const steps = 8
        const stepWidth = width / steps

        for (let i = 0; i < steps; i++) {
            const alpha = (1 - i / steps) * 0.6
            graphics.fillStyle(EDGE_COLOR, alpha)

            if (side === 'left') {
                graphics.fillRect(x + i * stepWidth, y, stepWidth, height)
            } else {
                graphics.fillRect(x + (steps - 1 - i) * stepWidth, y, stepWidth, height)
            }
        }
    }

    destroy(): void {
        this.glowLeft?.destroy()
        this.glowLeft = undefined
        this.glowRight?.destroy()
        this.glowRight = undefined
        this.active = false
    }
}
