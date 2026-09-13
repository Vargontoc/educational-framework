import Phaser, { Scene } from "phaser"
import { WORLD_MAP_CONFIG } from "../config/worldMapConfig"

const BACKGROUND_DEPTH = 0
const SKY_COLOR = 0x87ceeb
const DEFAULT_BIOME = 'meadow'

export class BackgroundLayer {
    private scene: Scene
    private container?: Phaser.GameObjects.Container
    private skybox?: Phaser.GameObjects.TileSprite | Phaser.GameObjects.Graphics
    private lastBiome?: string

    private skyboxScrollX: number = 0
    constructor(scene: Scene) {
        this.scene = scene
    }

    buildFrom(biome?: string): Phaser.GameObjects.Container {
        if (!this.container) {
            this.container = this.scene.add.container(0, 0)
            this.container.setDepth(BACKGROUND_DEPTH)
        }

        const effectiveBiome = biome ?? DEFAULT_BIOME

        if (this.skybox && this.lastBiome === effectiveBiome) {
            return this.container
        }

        this.lastBiome = effectiveBiome

        if (this.skybox) {
            this.skybox.destroy()
            this.skybox = undefined
        }

        const skyboxKey = `skybox-${effectiveBiome}`
        const { viewportWidth, viewportHeight } = WORLD_MAP_CONFIG

        if (this.scene.textures.exists(skyboxKey)) {
            const actualWidth = this.scene.scale.gameSize.width;
            const actualHeight = this.scene.scale.gameSize.height;

            const tile = this.scene.add.tileSprite(0, 0, actualWidth, actualHeight, skyboxKey)
            tile.setOrigin(0, 0)
            this.container.add(tile)
            this.skybox = tile
            tile.tileScaleX = 1.001; 
            const texture = this.scene.textures.get(skyboxKey)
            texture.setFilter(Phaser.Textures.FilterMode.LINEAR);

            if(texture.source && texture.source[0]){
                texture.source[0].setWrap(Phaser.Textures.WrapMode.REPEAT, Phaser.Textures.WrapMode.REPEAT);
                texture.source[0].update();
            }
            
        } else {
            const graphics = this.scene.add.graphics()
            graphics.fillStyle(SKY_COLOR, 1)
            graphics.fillRect(0, 0, viewportWidth, viewportHeight)
            this.container.add(graphics)
            this.skybox = graphics
        }

        return this.container
    }

    getLastBiome(): string | undefined {
        return this.lastBiome
    }

    update(deltaMs: number) {
        if (!this.skybox || !('tilePositionX' in this.skybox)) return

        const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
        if (reducedMotion) return

        this.skyboxScrollX +=  (WORLD_MAP_CONFIG.skyboxAutoScrollSpeed * (deltaMs / 1000))
        this.skybox.tilePositionX = Math.round(this.skyboxScrollX)

    }

    destroy() {
        this.container?.destroy()
        this.container = undefined
        this.skybox = undefined
        this.lastBiome = undefined
    }
}
