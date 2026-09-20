import { Scene } from "phaser"
import type { LayoutSizes } from "../utils/ResponsiveLayout"

const DOUBLE_TAP_WINDOW = 2000
const BUTTON_DEPTH = 10
const BUTTON_BG_COLOR = 0x000000
const BUTTON_BG_ALPHA = 0.25
const BUTTON_BG_RADIUS_RATIO = 0.2
const ICON_COLOR = 0xFFFFFF
const ICON_THICKNESS_RATIO = 0.08
const ICON_PADDING_RATIO = 0.3
const ALPHA_IDLE = 0.55
const ALPHA_ACTIVE = 0.9
const ALPHA_ARMED = 1
const EXIT_EVENT = 'exit-button-double-tap'

export class ExitButton {
    private scene: Scene
    private container: Phaser.GameObjects.Container
    private bg: Phaser.GameObjects.Graphics
    private icon: Phaser.GameObjects.Graphics
    private hitZone: Phaser.GameObjects.Zone
    private lastTapTime = 0

    constructor(scene: Scene, sizes: LayoutSizes) {
        this.scene = scene

        this.bg = scene.add.graphics()
        this.icon = scene.add.graphics()

        this.container = scene.add.container(0, 0, [this.bg, this.icon])
        this.container.setDepth(BUTTON_DEPTH)
        this.container.setScrollFactor(0)
        this.container.setAlpha(ALPHA_IDLE)

        this.hitZone = scene.add.zone(0, 0, 1, 1)
        this.hitZone.setDepth(BUTTON_DEPTH)
        this.hitZone.setScrollFactor(0)
        this.hitZone.setInteractive({ useHandCursor: false })
        this.hitZone.on('pointerover', () => this.setResting(ALPHA_ACTIVE))
        this.hitZone.on('pointerout', () => this.setResting(ALPHA_IDLE))
        this.hitZone.on('pointerdown', () => this.handleTap())

        this.resize(sizes)
    }

    /** Recoloca y redibuja el botón (esquina superior izquierda) con los tamaños dados. */
    resize(sizes: LayoutSizes): void {
        const size = sizes.buttonSize
        const half = size / 2
        const x = sizes.buttonMargin + half
        const y = sizes.buttonMargin + half
        const iconPadding = size * ICON_PADDING_RATIO

        this.bg.clear()
        this.bg.fillStyle(BUTTON_BG_COLOR, BUTTON_BG_ALPHA)
        this.bg.fillRoundedRect(-half, -half, size, size, size * BUTTON_BG_RADIUS_RATIO)

        this.icon.clear()
        this.icon.lineStyle(size * ICON_THICKNESS_RATIO, ICON_COLOR, 1)
        this.icon.beginPath()
        this.icon.moveTo(-half + iconPadding, -half + iconPadding)
        this.icon.lineTo(half - iconPadding, half - iconPadding)
        this.icon.moveTo(half - iconPadding, -half + iconPadding)
        this.icon.lineTo(-half + iconPadding, half - iconPadding)
        this.icon.strokePath()

        this.container.setPosition(x, y)
        this.hitZone.setPosition(x, y)
        this.hitZone.setSize(size, size)
    }

    private setResting(alpha: number): void {
        if (this.lastTapTime > 0) return
        this.container.setAlpha(alpha)
    }

    private handleTap(): void {
        const now = Date.now()
        if (this.lastTapTime > 0 && now - this.lastTapTime <= DOUBLE_TAP_WINDOW) {
            this.lastTapTime = 0
            this.container.setAlpha(ALPHA_IDLE)
            this.scene.events.emit(EXIT_EVENT)
            return
        }

        this.lastTapTime = now
        this.container.setAlpha(ALPHA_ARMED)
        this.scene.time.delayedCall(DOUBLE_TAP_WINDOW, () => {
            if (this.lastTapTime === now) {
                this.lastTapTime = 0
                this.container.setAlpha(ALPHA_IDLE)
            }
        })
    }

    destroy(): void {
        this.hitZone.destroy()
        this.container.destroy()
    }
}
