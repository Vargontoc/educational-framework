import { Scene } from "phaser"
import { WORLD_MAP_CONFIG } from "../config/worldMapConfig"

const PARALLAX_DEPTH = 1
const HILLS_KEY = 'background'
// Solape deliberado con GroundLayer para evitar la línea/costura visible que
// deja un límite "a tope" entre dos tileSprites (filtrado/escalado de canvas):
// GroundLayer se dibuja encima (mayor depth) y tapa este pequeño solape.
const GROUND_SEAM_OVERLAP = 4
const CLOUD_COLOR = 0xffffff
const MOUNTAIN_COLOR = 0x6b8e6b
const CLOUD_X_POSITIONS = [200, 700, 1300, 1900, 2400]
const MOUNTAIN_X_POSITIONS = [400, 1000, 1600, 2200]

function layerWidth(): number {
    return WORLD_MAP_CONFIG.worldWidth + WORLD_MAP_CONFIG.viewportWidth
}

export class ParallaxLayer {
    private scene: Scene
    private container?: Phaser.GameObjects.Container

    constructor(scene: Scene) {
        this.scene = scene
    }

    // groundBandHeight: alto (px) de la franja de GroundLayer ya construida
    // (GroundLayer.getBandHeight()). Las colinas se apoyan justo encima de esa
    // franja en vez de anclarse al borde inferior del viewport: si se ancladan
    // ambas al mismo borde, el suelo (depth más alto) tapa la parte baja de las
    // colinas, que suele ser la más sólida del dibujo, dejándolas casi invisibles.
    create(groundBandHeight = 0): Phaser.GameObjects.Container {
        this.destroy()

        const { viewportHeight } = WORLD_MAP_CONFIG
        const container = this.scene.add.container(0, 0)
        container.setDepth(PARALLAX_DEPTH)

        if (this.scene.textures.exists(HILLS_KEY)) {
            // background (hill_background.png) es una franja de colinas, no una
            // imagen a pantalla completa: se ancla a su altura real, justo encima
            // del suelo, para no estirarla/tejerla verticalmente ni dejarla tapada.
            const source = this.scene.textures.get(HILLS_KEY).getSourceImage()
            const height = source.height || viewportHeight
            const bottomY = viewportHeight - groundBandHeight + GROUND_SEAM_OVERLAP
            const tile = this.scene.add.tileSprite(0, bottomY, layerWidth(), height, HILLS_KEY)
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

    destroy() {
        this.container?.destroy()
        this.container = undefined
    }
}
