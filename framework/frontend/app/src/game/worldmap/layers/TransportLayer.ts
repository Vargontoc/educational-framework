import { Scene } from "phaser"
import { WORLD_MAP_CONFIG } from "../config/worldMapConfig"

const TRANSPORT_DEPTH = 5
const TRANSPORT_ICON_SIZE = 64
const TRANSPORT_OFFSET_FROM_NUBI = 120
const REDUCED_MOTION_CUE_DURATION = 600
const CUE_DURATION = 1200

const BIOME_TRANSPORT_ICON: Record<string, string> = {
    meadow: '🎈',
    farm: '🚜',
    woods: '🍄',
    beach: '⛵',
    space: '🚀',
    prehistory: '🦕'
}

const BIOME_TRANSPORT_COLOR: Record<string, number> = {
    meadow: 0xff7043,
    farm: 0x8d6e63,
    woods: 0x66bb6a,
    beach: 0x42a5f5,
    space: 0x7e57c2,
    prehistory: 0xef6c00
}

const BIOME_TRANSPORT_SHAPE: Record<string, 'balloon' | 'tractor' | 'mushroom' | 'boat' | 'rocket' | 'dino'> = {
    meadow: 'balloon',
    farm: 'tractor',
    woods: 'mushroom',
    beach: 'boat',
    space: 'rocket',
    prehistory: 'dino'
}

export const TRANSPORT_TOUCHED_EVENT = 'transport-touched'

export class TransportLayer {
    private scene: Scene
    private container?: Phaser.GameObjects.Container
    private cueTween?: Phaser.Tweens.Tween

    constructor(scene: Scene) {
        this.scene = scene
    }

    create(biome: string, groundTopY: number, nubiStartX: number): Phaser.GameObjects.Container {
        this.destroy()

        this.container = this.scene.add.container(0, 0)
        this.container.setDepth(TRANSPORT_DEPTH)

        const x = nubiStartX + TRANSPORT_OFFSET_FROM_NUBI
        const y = groundTopY - TRANSPORT_ICON_SIZE

        const hitAreaSize = Math.max(WORLD_MAP_CONFIG.minHitAreaSize, TRANSPORT_ICON_SIZE + 16)
        const zone = this.scene.add.zone(x, y, hitAreaSize, hitAreaSize)
        zone.setInteractive({ useHandCursor: true })
        zone.on('pointerdown', () => {
            this.scene.events.emit(TRANSPORT_TOUCHED_EVENT)
        })

        const visual = this.createTransportVisual(biome, x, y)
        this.applyPassiveCue(visual)

        this.container.add([zone, visual])

        return this.container
    }

    private createTransportVisual(biome: string, x: number, y: number): Phaser.GameObjects.Container {
        const elementContainer = this.scene.add.container(x, y)

        const assetKey = `transport-${biome}`
        if (this.scene.textures.exists(assetKey)) {
            const img = this.scene.add.image(0, 0, assetKey)
            elementContainer.add(img)
            return elementContainer
        }

        const shape = BIOME_TRANSPORT_SHAPE[biome] ?? 'balloon'
        const color = BIOME_TRANSPORT_COLOR[biome] ?? 0xcccccc
        const icon = BIOME_TRANSPORT_ICON[biome] ?? '?'

        const bg = this.createShapeForTransport(shape, color)
        elementContainer.add(bg)

        const label = this.scene.add.text(0, 0, icon, {
            fontSize: '32px',
            color: '#ffffff',
            fontStyle: 'bold'
        })
        label.setOrigin(0.5)
        elementContainer.add(label)

        return elementContainer
    }

    private createShapeForTransport(shape: string, color: number): Phaser.GameObjects.GameObject {
        const half = TRANSPORT_ICON_SIZE / 2

        switch (shape) {
            case 'balloon': {
                const body = this.scene.add.circle(0, 8, half * 0.7, color)
                const basket = this.scene.add.rectangle(0, half + 4, half * 0.5, half * 0.3, 0x795548)
                const rope = this.scene.add.rectangle(0, half * 0.5, 2, half * 0.5, 0x795548)
                return this.scene.add.container(0, 0, [body, rope, basket])
            }
            case 'tractor': {
                const bodyRect = this.scene.add.rectangle(-4, 0, TRANSPORT_ICON_SIZE * 0.6, TRANSPORT_ICON_SIZE * 0.4, color)
                const wheel1 = this.scene.add.circle(-half * 0.4, half * 0.35, half * 0.3, 0x333333)
                const wheel2 = this.scene.add.circle(half * 0.3, half * 0.35, half * 0.2, 0x333333)
                const chimney = this.scene.add.rectangle(-half * 0.3, -half * 0.35, 6, half * 0.4, 0x555555)
                return this.scene.add.container(0, 0, [bodyRect, chimney, wheel1, wheel2])
            }
            case 'mushroom': {
                const cap = this.scene.add.circle(0, -4, half * 0.7, color)
                const stem = this.scene.add.rectangle(0, half * 0.4, half * 0.35, half * 0.5, 0xfff9c4)
                const spot1 = this.scene.add.circle(-8, -10, 5, 0xffffff)
                const spot2 = this.scene.add.circle(8, -4, 4, 0xffffff)
                return this.scene.add.container(0, 0, [stem, cap, spot1, spot2])
            }
            case 'boat': {
                const hull = this.scene.add.triangle(
                    0, 8,
                    -half * 0.7, 0,
                    half * 0.7, 0,
                    0, half * 0.5,
                    color
                )
                const sail = this.scene.add.triangle(
                    0, -4,
                    0, -half * 0.6,
                    half * 0.4, half * 0.15,
                    -2, half * 0.15,
                    0xffffff
                )
                const mast = this.scene.add.rectangle(0, -4, 2, half * 0.7, 0x795548)
                return this.scene.add.container(0, 0, [hull, mast, sail])
            }
            case 'rocket': {
                const bodyR = this.scene.add.rectangle(0, 4, half * 0.45, TRANSPORT_ICON_SIZE * 0.6, color)
                const nose = this.scene.add.triangle(
                    0, -half * 0.55,
                    0, -half * 0.35,
                    half * 0.22, 0,
                    -half * 0.22, 0,
                    0xef5350
                )
                const fin1 = this.scene.add.triangle(
                    -half * 0.25, half * 0.5,
                    0, -half * 0.15,
                    0, half * 0.15,
                    -half * 0.2, half * 0.15,
                    0xef5350
                )
                const fin2 = this.scene.add.triangle(
                    half * 0.25, half * 0.5,
                    0, -half * 0.15,
                    half * 0.2, half * 0.15,
                    0, half * 0.15,
                    0xef5350
                )
                const windowR = this.scene.add.circle(0, -2, 6, 0xbbdefb)
                return this.scene.add.container(0, 0, [bodyR, nose, fin1, fin2, windowR])
            }
            case 'dino': {
                const bodyD = this.scene.add.circle(0, 4, half * 0.5, color)
                const head = this.scene.add.circle(half * 0.35, -half * 0.2, half * 0.3, color)
                const eye = this.scene.add.circle(half * 0.42, -half * 0.28, 3, 0xffffff)
                const tail = this.scene.add.triangle(
                    -half * 0.5, 4,
                    0, -half * 0.1,
                    0, half * 0.1,
                    -half * 0.4, 0,
                    color
                )
                const leg1 = this.scene.add.rectangle(-8, half * 0.5, 6, half * 0.25, color)
                const leg2 = this.scene.add.rectangle(8, half * 0.5, 6, half * 0.25, color)
                return this.scene.add.container(0, 0, [tail, bodyD, head, eye, leg1, leg2])
            }
            default:
                return this.scene.add.circle(0, 0, half, color)
        }
    }

    private applyPassiveCue(visual: Phaser.GameObjects.Container) {
        const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches

        if (reducedMotion) {
            this.cueTween = this.scene.tweens.add({
                targets: visual,
                alpha: 0.8,
                duration: REDUCED_MOTION_CUE_DURATION,
                yoyo: true,
                repeat: -1,
                ease: 'Sine.easeInOut'
            })
            return
        }

        this.cueTween = this.scene.tweens.add({
            targets: visual,
            y: visual.y - 6,
            scaleX: 1.05,
            scaleY: 1.05,
            duration: CUE_DURATION,
            yoyo: true,
            repeat: -1,
            ease: 'Sine.easeInOut'
        })
    }

    destroy() {
        this.cueTween?.stop()
        this.cueTween = undefined
        this.container?.destroy()
        this.container = undefined
    }
}
