import type { Scene } from "phaser"

/**
 * Prolonga el mosaico de fondo del minijuego por fuera del canvas.
 *
 * El minijuego usa Scale.FIT: el canvas 16:9 cabe entero en la ventana y, si esta no es 16:9,
 * quedan franjas alrededor que el canvas no puede pintar. Este fondo pinta el mismo mosaico como
 * `background` CSS del contenedor del canvas, a la misma escala y centrado igual que el TileSprite
 * (ver `tilePositionFor`), de modo que el patron continua sin costuras y la ventana queda cubierta.
 */
export class ViewportBackdrop {
    private scene: Scene
    private container?: HTMLElement
    private textureKey = ''
    private tileSize = 0
    private blobUrl?: string
    private applyToken = 0

    constructor(scene: Scene) {
        this.scene = scene
    }

    /**
     * Posicion del patron para el TileSprite del canvas: un mosaico centrado en el centro del
     * canvas, igual que `background-position: center` en el contenedor.
     */
    static tilePositionFor(canvasSize: number, tileSize: number): number {
        const offset = (canvasSize - tileSize) / 2
        return ((-offset % tileSize) + tileSize) % tileSize
    }

    /** Pone el contenedor en negro (las franjas de FIT) mientras dura la transicion, hasta `apply`. */
    hold(): void {
        const container = this.scene.game.canvas?.parentElement
        if (!container) return
        this.container = container
        container.style.backgroundColor = '#000'
    }

    /**
     * Usa el mosaico `textureKey` como fondo del contenedor. Sin textura o sin contenedor no hace nada.
     * Phaser carga las imagenes como blob y revoca su URL al terminar, asi que el mosaico se
     * re-codifica desde la textura ya cargada (sin nueva peticion de red).
     */
    apply(textureKey: string): void {
        const container = this.scene.game.canvas?.parentElement
        if (!container || !this.scene.textures.exists(textureKey)) return

        const image = this.scene.textures.get(textureKey).getSourceImage() as HTMLImageElement
        if (!image?.width || !image?.height) return

        const canvas = document.createElement('canvas')
        canvas.width = image.width
        canvas.height = image.height
        canvas.getContext('2d')?.drawImage(image, 0, 0)

        const token = ++this.applyToken
        canvas.toBlob(blob => {
            // clear() o un nuevo apply() mientras se codificaba: descartar
            if (!blob || token !== this.applyToken) return
            if (this.blobUrl) URL.revokeObjectURL(this.blobUrl)
            this.blobUrl = URL.createObjectURL(blob)
            this.container = container
            this.textureKey = textureKey
            this.tileSize = image.width

            container.style.backgroundImage = `url("${this.blobUrl}")`
            container.style.backgroundRepeat = 'repeat'
            container.style.backgroundPosition = 'center center'
            this.update()
        })
    }

    /** Reajusta el tamano del mosaico a la escala actual del canvas (cambia al redimensionar la ventana). */
    update(): void {
        if (!this.container || !this.tileSize) return
        const displayScale = this.scene.scale.displayScale.x || 1
        const cssTile = this.tileSize / displayScale
        this.container.style.backgroundSize = `${cssTile}px ${cssTile}px`
    }

    /** Quita el fondo y deja el del contenedor como estaba. */
    clear(): void {
        this.applyToken++
        if (this.blobUrl) {
            URL.revokeObjectURL(this.blobUrl)
            this.blobUrl = undefined
        }
        if (!this.container) return
        this.container.style.backgroundColor = ''
        this.container.style.backgroundImage = ''
        this.container.style.backgroundRepeat = ''
        this.container.style.backgroundPosition = ''
        this.container.style.backgroundSize = ''
        this.container = undefined
        this.tileSize = 0
    }

    get active(): boolean {
        return !!this.container
    }

    get key(): string {
        return this.textureKey
    }
}
