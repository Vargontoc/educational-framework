import { Scene } from "phaser"
import { WORLD_MAP_CONFIG, biomeYOffset } from "../config/worldMapConfig"

const PARALLAX_DEPTH = 1
const GROUND_SEAM_OVERLAP = 4
const CLOUD_COLOR = 0xffffff
const MOUNTAIN_COLOR = 0x6b8e6b
const CLOUD_X_POSITIONS = [200, 700, 1300, 1900, 2400]
const MOUNTAIN_X_POSITIONS = [400, 1000, 1600, 2200]
const DEFAULT_BIOME = 'meadow'

export class ParallaxLayer {
    private scene: Scene
    private container?: Phaser.GameObjects.Container
    private lastBiome?: string
    private activeWorldWidth: number = WORLD_MAP_CONFIG.worldWidth

    constructor(scene: Scene) {
        this.scene = scene
    }

    create(groundBandHeight = 0, biome?: string, worldWidth?: number): Phaser.GameObjects.Container {
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
        const hillsKey = `background-${effectiveBiome}`
        const container = this.scene.add.container(0, 0)
        container.setDepth(PARALLAX_DEPTH)

        if (this.scene.textures.exists(hillsKey)) {
            const source = this.scene.textures.get(hillsKey).getSourceImage()
            const height = source.height || viewportHeight
            const bottomY = viewportHeight - groundBandHeight + GROUND_SEAM_OVERLAP + biomeYOffset(effectiveBiome, 'hills')
            const tile = this.scene.add.tileSprite(0, bottomY, width, height, hillsKey)
            tile.setOrigin(0, 1)
            container.add(tile)
        } else {
            CLOUD_X_POSITIONS.forEach((x, index) => {
                const cloud = this.scene.add.circle(x, 100 + (index % 2) * 40, 36, CLOUD_COLOR, 0.85)
                container.add(cloud)
            })

            MOUNTAIN_X_POSITIONS.forEach((x) => {
                const mountain = this.scene.add.triangle(
                    x, viewportHeight - 60,
                    0, 80,
                    80, 0,
                    160, 80,
                    MOUNTAIN_COLOR
                )
                container.add(mountain)
            })
        }

        this.container = container
        return container
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
