import type { Scene } from "phaser"

/**
 * Fondo del minijuego a nivel de viewport del dispositivo.
 *
 * El minijuego usa Scale.FIT: el canvas 16:9 cabe entero en la ventana y a su alrededor quedan
 * franjas. En vez de dibujar el fondo dentro del canvas (y depender de su tamano o del contenedor),
 * la imagen se pone en una capa fija que ocupa todo el viewport, detras del canvas, y se estira a
 * el (`100% 100%`, sin repetir ni recortar). No depende del tamano del canvas ni del mundo de juego:
 * el canvas es transparente (`transparent: true` en la config del juego) y deja ver la capa.
 */
export class ViewportBackdrop {
    private scene: Scene
    private container?: HTMLElement
    private canvas?: HTMLCanvasElement
    private layer?: HTMLDivElement
    private blobUrl?: string
    private previousCanvasStyle?: { position: string, zIndex: string }
    private textureKey = ''
    private applyToken = 0

    constructor(scene: Scene) {
        this.scene = scene
    }

    /** Pone el contenedor en negro (las franjas de FIT) mientras dura la transicion, hasta `apply`. */
    hold(): void {
        const container = this.scene.game.canvas?.parentElement
        if (!container) return
        this.container = container
        container.style.backgroundColor = '#000'
    }

    /**
     * Muestra la textura `textureKey` como fondo del viewport. Sin textura o sin contenedor no hace nada.
     * Phaser carga las imagenes como blob y revoca su URL al terminar, asi que la imagen se re-codifica
     * desde la textura ya cargada (sin nueva peticion de red).
     */
    apply(textureKey: string): void {
        const canvas = this.scene.game.canvas
        const container = canvas?.parentElement
        if (!canvas || !container || !this.scene.textures.exists(textureKey)) return

        const image = this.scene.textures.get(textureKey).getSourceImage() as HTMLImageElement
        if (!image?.width || !image?.height) return

        const source = document.createElement('canvas')
        source.width = image.width
        source.height = image.height
        source.getContext('2d')?.drawImage(image, 0, 0)

        const token = ++this.applyToken
        source.toBlob(blob => {
            // clear() o un nuevo apply() mientras se codificaba: descartar
            if (!blob || token !== this.applyToken) return
            this.removeLayer()

            this.container = container
            this.canvas = canvas
            this.textureKey = textureKey
            this.blobUrl = URL.createObjectURL(blob)

            const layer = document.createElement('div')
            layer.setAttribute('data-minigame-backdrop', textureKey)
            Object.assign(layer.style, {
                position: 'fixed',
                inset: '0',
                zIndex: '0',
                pointerEvents: 'none',
                backgroundImage: `url("${this.blobUrl}")`,
                backgroundSize: '100% 100%',
                backgroundRepeat: 'no-repeat',
                backgroundPosition: 'center',
                opacity: '0',
                transition: 'opacity 400ms ease-out'
            })
            container.insertBefore(layer, container.firstChild)
            this.layer = layer

            // El canvas queda por encima de la capa
            this.previousCanvasStyle = { position: canvas.style.position, zIndex: canvas.style.zIndex }
            canvas.style.position = 'relative'
            canvas.style.zIndex = '1'

            requestAnimationFrame(() => { layer.style.opacity = '1' })
        })
    }

    /** Quita el fondo y deja el contenedor y el canvas como estaban. */
    clear(): void {
        this.applyToken++
        this.removeLayer()
        if (this.container) {
            this.container.style.backgroundColor = ''
            this.container = undefined
        }
    }

    private removeLayer(): void {
        this.layer?.remove()
        this.layer = undefined
        if (this.blobUrl) {
            URL.revokeObjectURL(this.blobUrl)
            this.blobUrl = undefined
        }
        if (this.canvas && this.previousCanvasStyle) {
            this.canvas.style.position = this.previousCanvasStyle.position
            this.canvas.style.zIndex = this.previousCanvasStyle.zIndex
        }
        this.canvas = undefined
        this.previousCanvasStyle = undefined
        this.textureKey = ''
    }

    get active(): boolean {
        return !!this.layer
    }

    get key(): string {
        return this.textureKey
    }
}
