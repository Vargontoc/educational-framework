import { Scene } from "phaser"
import { WorldDiscoveryElements } from "../../GameEvent"
import { WORLD_MAP_CONFIG } from "../config/worldMapConfig"

const INTERACTIVE_DEPTH = 3
const SHAPE_SIZE = 56
const LAYOUT_MARGIN = 200
const PASSIVE_CUE_DURATION = 1400

type ShapeKind = 'circle' | 'square' | 'triangle' | 'star'

const SHAPE_ORDER: ShapeKind[] = ['circle', 'square', 'triangle', 'star']
const COLOR_PALETTE = [0xff8a65, 0x64b5f6, 0xaed581, 0xffd54f, 0xba68c8, 0x4db6ac]

function hashString(value: string): number {
    let hash = 0
    for (let i = 0; i < value.length; i++) {
        hash = (hash * 31 + value.charCodeAt(i)) >>> 0
    }
    return hash
}

function shapeForType(elementType: string): ShapeKind {
    return SHAPE_ORDER[hashString(elementType || 'default') % SHAPE_ORDER.length]
}

function colorForType(elementType: string): number {
    return COLOR_PALETTE[hashString(elementType || 'default') % COLOR_PALETTE.length]
}

export type InteractiveVisual = Phaser.GameObjects.Shape | Phaser.GameObjects.Image
export type ElementTouchHandler = (element: WorldDiscoveryElements, visual: InteractiveVisual, pointer: Phaser.Input.Pointer) => void

export class InteractiveLayer {
    private scene: Scene
    private container?: Phaser.GameObjects.Container
    private onTouch?: ElementTouchHandler
    private cueTweens: Phaser.Tweens.Tween[] = []

    constructor(scene: Scene) {
        this.scene = scene
    }

    create(): Phaser.GameObjects.Container {
        // Sin este destroy(), cada rebuildLayersForBiome() (una vez por
        // transición) dejaba el contenedor anterior huérfano en pantalla
        // —seguía visible e interactivo con los elementos del bioma
        // anterior— en vez de eliminarlo al crear el nuevo. Mismo patrón que
        // ya usan GroundLayer/ParallaxLayer/TransportLayer/ExitPortalLayer.
        this.destroy()

        this.container = this.scene.add.container(0, 0)
        this.container.setDepth(INTERACTIVE_DEPTH)
        return this.container
    }

    setOnTouch(handler: ElementTouchHandler) {
        this.onTouch = handler
    }

    // El contrato solo trae ancho de mundo (world_width), no una altura de
    // mundo equivalente: la posición vertical no tiene una "franja" propia a
    // la que referirse, así que positionY se mapea directamente sobre la
    // altura del viewport/dispositivo. Nota: esto acopla la posición vertical
    // al alto de pantalla actual (1280x720 fijo hoy) — pendiente de revisar
    // cuando se aborde el control responsive a distintos tamaños de dispositivo.
    render(elements: WorldDiscoveryElements[], activeWorldWidth?: number) {
        if (!this.container) return
        this.clearCueTweens()
        this.container.removeAll(true)

        const worldWidth = activeWorldWidth ?? WORLD_MAP_CONFIG.worldWidth
        const usableWidth = worldWidth - LAYOUT_MARGIN * 2
        const minStep = WORLD_MAP_CONFIG.minHitAreaSize + WORLD_MAP_CONFIG.interactiveMinSeparation

        const unauthoredElements = elements.filter(e => e.positionX == null)
        const unauthoredStep = unauthoredElements.length > 1
            ? Math.max(minStep, usableWidth / (unauthoredElements.length - 1))
            : 0
        let unauthoredIndex = 0

        const defaultY = WORLD_MAP_CONFIG.viewportHeight - 160

        elements.forEach((element) => {
            let x: number
            let y: number

            if (element.positionX != null) {
                // Posición autorada por backend: se mapea 1:1 sobre el ancho de
                // mundo activo, sin el margen de layout (ese margen solo tiene
                // sentido para repartir la disposición sintética de abajo).
                x = element.positionX * worldWidth
            } else {
                x = unauthoredElements.length > 1
                    ? LAYOUT_MARGIN + unauthoredIndex * unauthoredStep
                    : LAYOUT_MARGIN + usableWidth / 2
                unauthoredIndex++
            }

            if (element.positionY != null) {
                y = element.positionY * WORLD_MAP_CONFIG.viewportHeight
            } else {
                y = defaultY
            }

            console.log(`Element: [${element.positionX}:${element.positionY}] -> World position: [${x}:${y}]`)

            this.createElement(element, x, y)
        })
    }

    private createElement(element: WorldDiscoveryElements, x: number, y: number) {
        if (!this.container) return

        const elementContainer = this.scene.add.container(x, y)

        const hitAreaSize = WORLD_MAP_CONFIG.minHitAreaSize
        const zone = this.scene.add.zone(0, 0, hitAreaSize, hitAreaSize)
        zone.setInteractive({ useHandCursor: true })

        const visual = this.createVisual(element)
        elementContainer.add([zone, visual])

        if (element.interactionCueType) {
            this.applyPassiveCue(visual)
        }

        zone.on('pointerdown', (pointer: Phaser.Input.Pointer) => this.onTouch?.(element, visual, pointer))

        this.container.add(elementContainer)
    }

    private createVisual(element: WorldDiscoveryElements): InteractiveVisual {
        const assetKey = element.visualAssetKey
        if (assetKey && this.scene.textures.exists(assetKey)) {
            return this.scene.add.image(0, 0, assetKey)
        }

        if (assetKey) {
            console.debug(`InteractiveLayer: visualAssetKey "${assetKey}" no encontrado en caché de texturas, usando placeholder geométrico`)
        }

        return this.createShape(element.elementType)
    }

    private createShape(elementType: string): Phaser.GameObjects.Shape {
        const kind = shapeForType(elementType)
        const color = colorForType(elementType)

        switch (kind) {
            case 'square':
                return this.scene.add.rectangle(0, 0, SHAPE_SIZE, SHAPE_SIZE, color)
            case 'triangle':
                return this.scene.add.triangle(
                    0, 0,
                    0, -SHAPE_SIZE / 2,
                    SHAPE_SIZE / 2, SHAPE_SIZE / 2,
                    -SHAPE_SIZE / 2, SHAPE_SIZE / 2,
                    color
                )
            case 'star':
                return this.scene.add.star(0, 0, 5, SHAPE_SIZE / 4, SHAPE_SIZE / 2, color)
            case 'circle':
            default:
                return this.scene.add.circle(0, 0, SHAPE_SIZE / 2, color)
        }
    }

    private applyPassiveCue(visual: InteractiveVisual) {
        const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
        if (reducedMotion) return

        const tween = this.scene.tweens.add({
            targets: visual,
            alpha: 0.75,
            scaleX: 1.04,
            scaleY: 1.04,
            duration: PASSIVE_CUE_DURATION,
            yoyo: true,
            repeat: -1,
            ease: 'Sine.easeInOut'
        })
        this.cueTweens.push(tween)
    }

    private clearCueTweens() {
        this.cueTweens.forEach(tween => tween.stop())
        this.cueTweens = []
    }

    destroy() {
        this.clearCueTweens()
        this.container?.destroy()
        this.container = undefined
    }
}
