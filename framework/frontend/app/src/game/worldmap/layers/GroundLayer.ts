import { Scene } from "phaser"
import { WORLD_MAP_CONFIG } from "../config/worldMapConfig"

const GROUND_DEPTH = 2
const GROUND_KEY = 'ground'
const GROUND_COLOR = 0x8bc34a
const GROUND_FALLBACK_HEIGHT_RATIO = 0.25

function layerWidth(): number {
    return WORLD_MAP_CONFIG.worldWidth + WORLD_MAP_CONFIG.viewportWidth
}

// Suelo del bioma: capa más cercana al jugador, donde se apoyan Nubi y los
// elementos interactuables. Se desplaza al factor 1 (WORLD_MAP_CONFIG.groundFactor),
// igual que InteractiveLayer, para que ambos permanezcan alineados visualmente.
export class GroundLayer {
    private scene: Scene
    private container?: Phaser.GameObjects.Container
    private bandHeight = 0

    constructor(scene: Scene) {
        this.scene = scene
    }

    create(): Phaser.GameObjects.Container {
        this.destroy()

        const { viewportHeight } = WORLD_MAP_CONFIG
        const width = layerWidth()
        const container = this.scene.add.container(0, 0)
        container.setDepth(GROUND_DEPTH)

        if (this.scene.textures.exists(GROUND_KEY)) {
            // ground.png es una franja (bastante menos alta que el viewport), no una
            // imagen a pantalla completa: se ancla a la altura real de la textura y al
            // borde inferior del viewport. Forzar la tileSprite a viewportHeight
            // estiraría/tejería verticalmente la franja (efecto "suelo repetido") y,
            // al ocupar toda la pantalla en el depth más alto de las tres capas de
            // fondo, taparía por completo skybox y background_hills debajo.
            const source = this.scene.textures.get(GROUND_KEY).getSourceImage()
            this.bandHeight = source.height || viewportHeight * GROUND_FALLBACK_HEIGHT_RATIO
            const tile = this.scene.add.tileSprite(0, viewportHeight, width, this.bandHeight, GROUND_KEY)
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

    // Alto real (en px) de la franja de suelo ya construida. WorldMapScene lo usa
    // para apoyar ParallaxLayer justo encima del suelo, en vez de que ambas capas
    // se solapen ancladas al borde inferior del viewport (lo que dejaba tapada por
    // el suelo la parte baja —normalmente la más "sólida"— de las colinas).
    getBandHeight(): number {
        return this.bandHeight
    }

    destroy() {
        this.container?.destroy()
        this.container = undefined
    }
}
