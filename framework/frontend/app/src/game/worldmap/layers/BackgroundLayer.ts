import Phaser, { Scene } from "phaser"
import { WORLD_MAP_CONFIG } from "../config/worldMapConfig"

const BACKGROUND_DEPTH = 0
const SKYBOX_KEY = 'skybox'
const SKY_COLOR = 0x87ceeb

export class BackgroundLayer {
    private scene: Scene
    private container?: Phaser.GameObjects.Container
    private skybox?: Phaser.GameObjects.TileSprite | Phaser.GameObjects.Graphics

    private skyboxScrollX: number = 0
    constructor(scene: Scene) {
        this.scene = scene
    }

    // El contenedor se crea una única vez y se reutiliza entre llamadas
    // (incluida la resincronización por biome en WORLD_STATE_SYNC) para que
    // cualquier referencia externa siga siendo válida entre reconstrucciones.
    buildFrom(_biome?: string): Phaser.GameObjects.Container {
        if (!this.container) {
            this.container = this.scene.add.container(0, 0)
            this.container.setDepth(BACKGROUND_DEPTH)
        }

        if (this.skybox) {
            // WORLD_STATE_SYNC llega ~1 vez/segundo (respuesta al heartbeat) y
            // WorldMapScene llama a buildFrom() en cada uno. Sistema de biomas aún
            // no implementado (no hay hoy ningún caso real de cambio de bioma en
            // caliente), así que no hace falta reconstruir el skybox cada vez:
            // destruir y recrear la tileSprite reiniciaría tilePositionX a 0 en
            // cada resincronización, cortando en seco la deriva continua de update().
            return this.container
        }

        const { viewportWidth, viewportHeight } = WORLD_MAP_CONFIG

        if (this.scene.textures.exists(SKYBOX_KEY)) {
            const actualWidth = this.scene.scale.gameSize.width;
            const actualHeight = this.scene.scale.gameSize.height;

            // El skybox no se registra en GradualScroller: a diferencia de las
            // demás capas (que solo se mueven cuando el jugador arrastra), el cielo
            // deriva solo, de forma continua, vía tilePositionX en update() — ver
            // más abajo. Por eso basta con el ancho del viewport, no el del mundo.
            const tile = this.scene.add.tileSprite(0, 0, actualWidth, actualHeight, SKYBOX_KEY)
            tile.setOrigin(0, 0)
            this.container.add(tile)
            this.skybox = tile
            tile.tileScaleX = 1.001; 
            const texture = this.scene.textures.get(SKYBOX_KEY)
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

    update(deltaMs: number) {
        // Duck-typing para distinguir la tileSprite real del fallback Graphics
        // (ninguno de los dos expone una propiedad común más directa que comprobar).
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
    }
}
