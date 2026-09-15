import { Scene } from "phaser"

const BAR_WIDTH = 200
const BAR_HEIGHT = 12
const BAR_RADIUS = 6
const BAR_BG_COLOR = 0xE0E0E0
const BAR_FILL_COLOR_START = 0x4CAF50
const BAR_DEPTH = 10
const BAR_TOP_MARGIN = 20
const ANIMATION_DURATION = 300
const REDUCED_MOTION_DURATION = 100

export class RoundProgressBar {
    private bg: Phaser.GameObjects.Rectangle
    private fill: Phaser.GameObjects.Image
    private scene: Scene
    private reducedMotion: boolean
    private currentWidth: number = 0

    constructor(scene: Scene) {
        this.scene = scene
        this.reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches

        const viewportCenterX = scene.scale.width / 2
        const barY = BAR_TOP_MARGIN + BAR_HEIGHT / 2

        // Background bar
        this.bg = scene.add.rectangle(viewportCenterX, barY, BAR_WIDTH, BAR_HEIGHT, BAR_BG_COLOR)
        this.bg.setOrigin(0.5, 0.5)
        this.bg.setDepth(BAR_DEPTH)
        this.bg.setScrollFactor(0)

        // Generate fill texture
        const fillGraphics = scene.add.graphics()
        fillGraphics.fillStyle(BAR_FILL_COLOR_START, 1)
        fillGraphics.fillRoundedRect(0, 0, BAR_WIDTH, BAR_HEIGHT, BAR_RADIUS)

        const fillTextureKey = '__roundProgressBarFill'
        if (scene.textures.exists(fillTextureKey)) {
            scene.textures.remove(fillTextureKey)
        }
        fillGraphics.generateTexture(fillTextureKey, BAR_WIDTH, BAR_HEIGHT)
        fillGraphics.destroy()

        // Fill bar (positioned at left edge of background)
        const fillX = viewportCenterX - BAR_WIDTH / 2
        const fillY = barY - BAR_HEIGHT / 2
        this.fill = scene.add.image(fillX, fillY, fillTextureKey)
        this.fill.setOrigin(0, 0)
        this.fill.setDepth(BAR_DEPTH + 1)
        this.fill.setScrollFactor(0)
        this.fill.setCrop(0, 0, 0, BAR_HEIGHT)
    }

    updateProgress(roundIndex: number, totalRounds: number): void {
        if (totalRounds <= 0) return

        const ratio = Math.min(Math.max(roundIndex / totalRounds, 0), 1)
        const targetWidth = ratio * BAR_WIDTH
        const duration = this.reducedMotion ? REDUCED_MOTION_DURATION : ANIMATION_DURATION

        const startWidth = this.currentWidth
        this.scene.tweens.addCounter({
            from: startWidth,
            to: targetWidth,
            duration,
            ease: 'Cubic.out',
            onUpdate: (tween) => {
                const value = tween.getValue() ?? 0
                this.currentWidth = value
                this.fill.setCrop(0, 0, value, BAR_HEIGHT)
            }
        })
    }

    destroy(): void {
        this.bg.destroy()
        this.fill.destroy()
    }
}
