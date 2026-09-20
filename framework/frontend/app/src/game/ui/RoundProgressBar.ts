import { Scene } from "phaser"
import type { LayoutSizes } from "../utils/ResponsiveLayout"

const BAR_BG_COLOR = 0xE0E0E0
const BAR_FILL_COLOR_START = 0x4CAF50
const BAR_DEPTH = 10
const ANIMATION_DURATION = 300
const REDUCED_MOTION_DURATION = 100
const FILL_TEXTURE_KEY = '__roundProgressBarFill'

export class RoundProgressBar {
    private bg!: Phaser.GameObjects.Rectangle
    private fill!: Phaser.GameObjects.Image
    private scene: Scene
    private reducedMotion: boolean
    private ratio: number = 0
    private barWidth: number = 0
    private barHeight: number = 0
    private progressTween?: Phaser.Tweens.Tween

    constructor(scene: Scene, sizes: LayoutSizes) {
        this.scene = scene
        this.reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
        this.build(sizes)
    }

    private build(sizes: LayoutSizes): void {
        const scene = this.scene
        this.barWidth = sizes.progressBarWidth
        this.barHeight = sizes.progressBarHeight

        const viewportCenterX = sizes.viewportWidth / 2
        const barY = sizes.progressBarTop + this.barHeight / 2

        // Background bar
        this.bg = scene.add.rectangle(viewportCenterX, barY, this.barWidth, this.barHeight, BAR_BG_COLOR)
        this.bg.setOrigin(0.5, 0.5)
        this.bg.setDepth(BAR_DEPTH)
        this.bg.setScrollFactor(0)

        // Generate fill texture
        const fillGraphics = scene.add.graphics()
        fillGraphics.fillStyle(BAR_FILL_COLOR_START, 1)
        fillGraphics.fillRoundedRect(0, 0, this.barWidth, this.barHeight, this.barHeight / 2)

        if (scene.textures.exists(FILL_TEXTURE_KEY)) {
            scene.textures.remove(FILL_TEXTURE_KEY)
        }
        fillGraphics.generateTexture(FILL_TEXTURE_KEY, this.barWidth, this.barHeight)
        fillGraphics.destroy()

        // Fill bar (positioned at left edge of background)
        const fillX = viewportCenterX - this.barWidth / 2
        const fillY = barY - this.barHeight / 2
        this.fill = scene.add.image(fillX, fillY, FILL_TEXTURE_KEY)
        this.fill.setOrigin(0, 0)
        this.fill.setDepth(BAR_DEPTH + 1)
        this.fill.setScrollFactor(0)
        this.applyRatio(this.ratio)
    }

    private applyRatio(ratio: number): void {
        this.ratio = ratio
        this.fill.setCrop(0, 0, ratio * this.barWidth, this.barHeight)
    }

    updateProgress(roundIndex: number, totalRounds: number): void {
        if (totalRounds <= 0) return

        const target = Math.min(Math.max(roundIndex / totalRounds, 0), 1)
        const duration = this.reducedMotion ? REDUCED_MOTION_DURATION : ANIMATION_DURATION

        this.progressTween?.stop()
        this.progressTween = this.scene.tweens.addCounter({
            from: this.ratio,
            to: target,
            duration,
            ease: 'Cubic.out',
            onUpdate: (tween) => this.applyRatio(tween.getValue() ?? 0)
        })
    }

    /** Reconstruye la barra con los nuevos tamaños conservando el progreso. */
    resize(sizes: LayoutSizes): void {
        this.bg.destroy()
        this.fill.destroy()
        this.build(sizes)
    }

    destroy(): void {
        this.progressTween?.stop()
        this.bg.destroy()
        this.fill.destroy()
    }
}
