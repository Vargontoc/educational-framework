import { Scene } from "phaser"
import { WORLD_MAP_CONFIG } from "../config/worldMapConfig"

const GROUND_DEPTH = 2
const GROUND_COLOR = 0x8bc34a
const GROUND_FALLBACK_HEIGHT_RATIO = 0.25
const DEFAULT_BIOME = 'meadow'

export class GroundLayer {
    private scene: Scene
    private container?: Phaser.GameObjects.Container
    private bandHeight = 0
    private lastBiome?: string
    private activeWorldWidth: number = WORLD_MAP_CONFIG.worldWidth

    constructor(scene: Scene) {
        this.scene = scene
    }

    create(biome?: string, worldWidth?: number): Phaser.GameObjects.Container {
        const effectiveBiome = biome ?? DEFAULT_BIOME
        const effectiveWorldWidth = worldWidth ?? WORLD_MAP_CONFIG.worldWidth

        if (this.container && this.lastBiome === effectiveBiome && this.activeWorldWidth === effectiveWorldWidth) {
            return this.container
        }

        this.destroy()

        this.lastBiome = effectiveBiome
        this.activeWorldWidth = effectiveWorldWidth

        const { viewportHeight } = WORLD_MAP_CONFIG
        const width = effectiveWorldWidth + WORLD_MAP_CONFIG.viewportWidth
        const groundKey = `ground-${effectiveBiome}`
        const container = this.scene.add.container(0, 0)
        container.setDepth(GROUND_DEPTH)

        if (this.scene.textures.exists(groundKey)) {
            const source = this.scene.textures.get(groundKey).getSourceImage()
            this.bandHeight = source.height || viewportHeight * GROUND_FALLBACK_HEIGHT_RATIO
            const tile = this.scene.add.tileSprite(0, viewportHeight, width, this.bandHeight, groundKey)
            tile.setOrigin(0, 1)
            container.add(tile)
        } else {
            this.bandHeight = viewportHeight * GROUND_FALLBACK_HEIGHT_RATIO
            const graphics = this.scene.add.graphics()
            graphics.fillStyle(GROUND_COLOR, 1)
            graphics.fillRect(0, viewportHeight - this.bandHeight, width, this.bandHeight)
            container.add(graphics)
        }

        this.container = container
        return container
    }

    getBandHeight(): number {
        return this.bandHeight
    }

    getLastBiome(): string | undefined {
        return this.lastBiome
    }

    getActiveWorldWidth(): number {
        return this.activeWorldWidth
    }

    destroy() {
        this.container?.destroy()
        this.container = undefined
    }
}
