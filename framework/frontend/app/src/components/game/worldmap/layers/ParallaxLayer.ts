import { Scene } from "phaser"
import { WORLD_MAP_CONFIG } from "../config/worldMapConfig"

const PARALLAX_DEPTH = 1
const CLOUD_COLOR = 0xffffff
const MOUNTAIN_COLOR = 0x6b8e6b
const CLOUD_X_POSITIONS = [200, 700, 1300, 1900, 2400]
const MOUNTAIN_X_POSITIONS = [400, 1000, 1600, 2200]

export class ParallaxLayer {
    private scene: Scene
    private container?: Phaser.GameObjects.Container

    constructor(scene: Scene) {
        this.scene = scene
    }

    create(): Phaser.GameObjects.Container {
        this.destroy()

        const { viewportHeight } = WORLD_MAP_CONFIG
        const container = this.scene.add.container(0, 0)
        container.setDepth(PARALLAX_DEPTH)

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

        this.container = container
        return container
    }

    destroy() {
        this.container?.destroy()
        this.container = undefined
    }
}
