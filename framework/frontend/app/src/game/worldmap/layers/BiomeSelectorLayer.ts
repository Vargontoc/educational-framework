import { Scene } from "phaser"
import { WORLD_MAP_CONFIG } from "../config/worldMapConfig"

const SELECTOR_DEPTH = 100
const OVERLAY_BG_COLOR = 0x000000
const OVERLAY_BG_ALPHA = 0.55
const STICKER_SIZE = 90
const STICKER_INNER_SIZE = 72
const CLOSE_BUTTON_SIZE = 48
const CLOSE_BUTTON_MARGIN = 24
const MAP_AREA_WIDTH_RATIO = 0.85
const MAP_AREA_HEIGHT_RATIO = 0.7
const REDUCED_MOTION_FADE_DURATION = 100
const FADE_DURATION = 250

const STICKER_DEFINITIONS: Array<{
    biome: string
    icon: string
    color: number
    shape: 'circle' | 'roundedRect' | 'hexagon' | 'diamond' | 'star' | 'triangle'
}> = [
    { biome: 'MEADOW', icon: '🌿', color: 0x81c784, shape: 'circle' },
    { biome: 'FARM', icon: '🌾', color: 0xa1887f, shape: 'roundedRect' },
    { biome: 'WOODS', icon: '🌲', color: 0x66bb6a, shape: 'hexagon' },
    { biome: 'BEACH', icon: '🏖', color: 0x4fc3f7, shape: 'diamond' },
    { biome: 'SPACE', icon: '⭐', color: 0x9575cd, shape: 'star' },
    { biome: 'PREHISTORY', icon: '🦴', color: 0xff8a65, shape: 'triangle' }
]

export const DESTINATION_SELECTED_EVENT = 'destination-selected'
export const SELECTOR_CLOSED_EVENT = 'selector-closed'

export interface BiomeHostInfo {
    biome: string
    sequenceOrder: number | null
}

export class BiomeSelectorLayer {
    private scene: Scene
    private container?: Phaser.GameObjects.Container
    private backdropZone?: Phaser.GameObjects.Zone
    private visible = false

    constructor(scene: Scene) {
        this.scene = scene
    }

    open(hosts: BiomeHostInfo[]) {
        if (this.visible) return
        this.visible = true

        this.container = this.scene.add.container(0, 0)
        this.container.setDepth(SELECTOR_DEPTH)

        const { viewportWidth, viewportHeight } = WORLD_MAP_CONFIG

        const backdrop = this.scene.add.rectangle(
            viewportWidth / 2, viewportHeight / 2,
            viewportWidth, viewportHeight,
            OVERLAY_BG_COLOR, OVERLAY_BG_ALPHA
        )
        backdrop.setScrollFactor(0)

        this.backdropZone = this.scene.add.zone(
            viewportWidth / 2, viewportHeight / 2,
            viewportWidth, viewportHeight
        )
        this.backdropZone.setInteractive({ useHandCursor: false })
        this.backdropZone.on('pointerdown', (_pointer: Phaser.Input.Pointer) => {
            this.close()
        })

        this.container.add([backdrop, this.backdropZone])

        this.buildMapArea(hosts)
        this.buildCloseButton()
        this.buildNubiPlaceholder()

        const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
        if (!reducedMotion) {
            this.container.setAlpha(0)
            this.scene.tweens.add({
                targets: this.container,
                alpha: 1,
                duration: FADE_DURATION,
                ease: 'Sine.easeOut'
            })
        }
    }

    close() {
        if (!this.visible || !this.container) return

        const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches

        if (reducedMotion) {
            this.cleanup()
            return
        }

        this.scene.tweens.add({
            targets: this.container,
            alpha: 0,
            duration: REDUCED_MOTION_FADE_DURATION,
            ease: 'Sine.easeIn',
            onComplete: () => {
                this.cleanup()
            }
        })
    }

    private cleanup() {
        this.scene.events.emit(SELECTOR_CLOSED_EVENT)
        this.container?.destroy()
        this.container = undefined
        this.backdropZone = undefined
        this.visible = false
    }

    private buildNubiPlaceholder() {
        if (!this.container) return

        const { viewportWidth, viewportHeight } = WORLD_MAP_CONFIG
        const nubiX = viewportWidth / 2
        const nubiY = viewportHeight * 0.15

        const nubiCircle = this.scene.add.circle(nubiX, nubiY, 36, 0xb3e5fc, 0.9)
        const nubiLabel = this.scene.add.text(nubiX, nubiY, '🗺️', {
            fontSize: '28px'
        })
        nubiLabel.setOrigin(0.5)

        this.container.add([nubiCircle, nubiLabel])
    }

    private buildMapArea(hosts: BiomeHostInfo[]) {
        if (!this.container) return

        const { viewportWidth, viewportHeight } = WORLD_MAP_CONFIG
        const areaWidth = viewportWidth * MAP_AREA_WIDTH_RATIO
        const areaHeight = viewportHeight * MAP_AREA_HEIGHT_RATIO
        const areaX = (viewportWidth - areaWidth) / 2
        const areaY = viewportHeight * 0.25

        const mapBg = this.scene.add.rectangle(
            viewportWidth / 2, areaY + areaHeight / 2,
            areaWidth, areaHeight,
            0xfff8e1, 0.9
        )
        mapBg.setStrokeStyle(3, 0x8d6e63)
        this.container.add(mapBg)

        const mapTitle = this.scene.add.text(viewportWidth / 2, areaY + 16, 'Where to go?', {
            fontSize: '20px',
            color: '#5d4037',
            fontStyle: 'bold'
        })
        mapTitle.setOrigin(0.5, 0)
        this.container.add(mapTitle)

        const sorted = this.sortHostsBySequence(hosts)
        this.layoutStickers(sorted, areaX, areaY + 50, areaWidth, areaHeight - 60)
    }

    private sortHostsBySequence(hosts: BiomeHostInfo[]): BiomeHostInfo[] {
        const withOrder = hosts.filter(h => h.sequenceOrder !== null)
        const withoutOrder = hosts.filter(h => h.sequenceOrder === null)

        withOrder.sort((a, b) => (a.sequenceOrder ?? 0) - (b.sequenceOrder ?? 0))

        const knownBiomes = new Set(withOrder.map(h => h.biome))
        const remaining = withoutOrder.filter(h => !knownBiomes.has(h.biome))

        return [...withOrder, ...remaining]
    }

    private layoutStickers(hosts: BiomeHostInfo[], areaX: number, areaY: number, areaWidth: number, areaHeight: number) {
        if (!this.container) return

        const count = hosts.length
        if (count === 0) return

        const cols = Math.min(count, 3)
        const rows = Math.ceil(count / cols)
        const cellW = areaWidth / cols
        const cellH = areaHeight / rows

        hosts.forEach((host, index) => {
            const col = index % cols
            const row = Math.floor(index / cols)
            const cx = areaX + cellW * col + cellW / 2
            const cy = areaY + cellH * row + cellH / 2

            this.createSticker(host.biome, cx, cy)
        })
    }

    private createSticker(biome: string, cx: number, cy: number) {
        if (!this.container) return

        const definition = STICKER_DEFINITIONS.find(d => d.biome === biome)
        if (!definition) return

        const stickerContainer = this.scene.add.container(cx, cy)

        const assetKey = `sticker-${biome.toLowerCase()}`
        if (this.scene.textures.exists(assetKey)) {
            const img = this.scene.add.image(0, 0, assetKey)
            stickerContainer.add(img)
        } else {
            const shape = this.createStickerShape(definition.shape, definition.color)
            stickerContainer.add(shape)

            const icon = this.scene.add.text(0, -2, definition.icon, {
                fontSize: '28px'
            })
            icon.setOrigin(0.5)
            stickerContainer.add(icon)
        }

        const nameLabel = this.scene.add.text(0, STICKER_SIZE / 2 + 4, this.formatBiomeName(biome), {
            fontSize: '14px',
            color: '#4e342e',
            fontStyle: 'bold'
        })
        nameLabel.setOrigin(0.5, 0)
        stickerContainer.add(nameLabel)

        const hitSize = Math.max(WORLD_MAP_CONFIG.minHitAreaSize, STICKER_SIZE)
        const zone = this.scene.add.zone(0, 0, hitSize, hitSize)
        zone.setInteractive({ useHandCursor: true })
        zone.on('pointerdown', (pointer: Phaser.Input.Pointer) => {
            pointer.event.stopPropagation()
            this.handleStickerTouch(biome)
        })
        stickerContainer.add(zone)

        this.container.add(stickerContainer)
    }

    private createStickerShape(shape: string, color: number): Phaser.GameObjects.GameObject {
        const half = STICKER_INNER_SIZE / 2

        switch (shape) {
            case 'circle':
                return this.scene.add.circle(0, 0, half, color)

            case 'roundedRect':
                return this.scene.add.rectangle(0, 0, STICKER_INNER_SIZE, STICKER_INNER_SIZE * 0.8, color)

            case 'hexagon':
                return this.scene.add.polygon(0, 0, this.hexagonPoints(half), color)

            case 'diamond':
                return this.scene.add.polygon(0, 0, [
                    0, -half,
                    half, 0,
                    0, half,
                    -half, 0
                ], color)

            case 'star':
                return this.scene.add.star(0, 0, 5, half * 0.45, half, color)

            case 'triangle':
                return this.scene.add.triangle(
                    0, 0,
                    0, -half,
                    half, half * 0.8,
                    -half, half * 0.8,
                    color
                )

            default:
                return this.scene.add.circle(0, 0, half, color)
        }
    }

    private hexagonPoints(radius: number): number[] {
        const points: number[] = []
        for (let i = 0; i < 6; i++) {
            const angle = (Math.PI / 3) * i - Math.PI / 2
            points.push(Math.cos(angle) * radius, Math.sin(angle) * radius)
        }
        return points
    }

    private formatBiomeName(biome: string): string {
        return biome.charAt(0).toUpperCase() + biome.slice(1).toLowerCase()
    }

    private buildCloseButton() {
        if (!this.container) return

        const { viewportWidth } = WORLD_MAP_CONFIG
        const x = viewportWidth - CLOSE_BUTTON_MARGIN - CLOSE_BUTTON_SIZE / 2
        const y = CLOSE_BUTTON_MARGIN + CLOSE_BUTTON_SIZE / 2

        const bg = this.scene.add.circle(x, y, CLOSE_BUTTON_SIZE / 2, 0xffffff, 0.85)
        bg.setStrokeStyle(2, 0x757575)

        const cross = this.scene.add.text(x, y, '✕', {
            fontSize: '24px',
            color: '#616161',
            fontStyle: 'bold'
        })
        cross.setOrigin(0.5)

        const zone = this.scene.add.zone(x, y, Math.max(WORLD_MAP_CONFIG.minHitAreaSize, CLOSE_BUTTON_SIZE), Math.max(WORLD_MAP_CONFIG.minHitAreaSize, CLOSE_BUTTON_SIZE))
        zone.setInteractive({ useHandCursor: true })
        zone.on('pointerdown', (pointer: Phaser.Input.Pointer) => {
            pointer.event.stopPropagation()
            this.close()
        })

        this.container.add([bg, cross, zone])
    }

    private handleStickerTouch(biome: string) {
        this.visible = false
        this.scene.events.emit(DESTINATION_SELECTED_EVENT, biome)
        this.cleanup()
    }
}
