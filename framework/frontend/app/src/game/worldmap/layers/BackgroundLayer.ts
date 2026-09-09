import { Scene } from "phaser"
import { WORLD_MAP_CONFIG } from "../config/worldMapConfig"

const BACKGROUND_DEPTH = 0
const SKY_COLOR = 0x87ceeb
const GROUND_COLOR = 0x8bc34a

export class BackgroundLayer {
    private scene: Scene
    private background?: Phaser.GameObjects.GameObject

    constructor(scene: Scene) {
        this.scene = scene
    }

    buildFrom(biome?: string) {
        this.destroy()

        const { viewportWidth, viewportHeight } = WORLD_MAP_CONFIG
        const assetKey = biome ? `biome-${biome.toLowerCase()}-bg` : undefined

        if (assetKey && this.scene.textures.exists(assetKey)) {
            const image = this.scene.add.image(viewportWidth / 2, viewportHeight / 2, assetKey)
            image.setDepth(BACKGROUND_DEPTH)
            this.background = image
            return
        }

        const graphics = this.scene.add.graphics()
        graphics.setDepth(BACKGROUND_DEPTH)
        graphics.fillGradientStyle(SKY_COLOR, SKY_COLOR, GROUND_COLOR, GROUND_COLOR, 1)
        graphics.fillRect(0, 0, viewportWidth, viewportHeight)
        this.background = graphics
    }

    destroy() {
        this.background?.destroy()
        this.background = undefined
    }
}
