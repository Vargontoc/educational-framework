import { Scene } from "phaser"
import { WorldDiscoveryElements } from "../../GameEvent"
import { WORLD_MAP_CONFIG } from "../config/worldMapConfig"
import * as InteractiveElementAnimations from "../reactions/InteractiveElementAnimations"
import type { InteractiveVisual } from "../reactions/InteractiveElementAnimations"

const INTERACTIVE_DEPTH = 3
const LAYOUT_MARGIN = 200
const PASSIVE_CUE_DURATION = 1400
/** Margen (px lógicos) que se añade al tamaño visible del elemento para calcular su zona táctil. */
const HIT_AREA_MARGIN = 30

export type { InteractiveVisual }
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
        InteractiveElementAnimations.ensureAnimations(this.scene)
        return this.container
    }

    setOnTouch(handler: ElementTouchHandler) {
        this.onTouch = handler
    }

    /** Si `assetKey` tiene una animación propia (de las 14 del bioma meadow), incluidas las "solo imagen". */
    hasCustomTapBehavior(assetKey: string): boolean {
        return InteractiveElementAnimations.hasBehavior(assetKey)
    }

    /** Reproduce la animación de toque propia de `element.visualAssetKey` (no-op para las "solo imagen"). */
    playTapAnimation(element: WorldDiscoveryElements, visual: InteractiveVisual): void {
        InteractiveElementAnimations.playTap(this.scene, visual, element.visualAssetKey)
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
            this.createElement(element, x, y)
        })
    }

    private createElement(element: WorldDiscoveryElements, x: number, y: number) {
        if (!this.container) return

        const visual = this.createVisual(element)
        if (!visual) return // sin textura: no se renderiza ningún fallback (ver createVisual)

        const elementContainer = this.scene.add.container(x, y)
        elementContainer.setData('assetKey', element.visualAssetKey)

        const hitAreaSize = this.hitAreaSizeFor(element.visualAssetKey)
        const zone = this.scene.add.zone(0, 0, hitAreaSize, hitAreaSize)
        zone.setInteractive({ useHandCursor: true })

        elementContainer.add([zone, visual])

        if (element.interactionCueType) {
            this.applyPassiveCue(visual)
        }
        InteractiveElementAnimations.attachIdle(this.scene, visual, element.visualAssetKey)

        zone.on('pointerdown', (pointer: Phaser.Input.Pointer) => this.onTouch?.(element, visual, pointer))

        this.container.add(elementContainer)
    }

    /**
     * Zona táctil del elemento: el tamaño al que se dibuja (ver `InteractiveElementAnimations`) más un margen,
     * nunca por debajo del mínimo general (`minHitAreaSize`). Mismo criterio que `TransportLayer`/`ExitPortalLayer`.
     * Una zona más grande que el mínimo fijo de antes también hace que, al tocar cerca del elemento, el toque
     * gane a "caminar hasta el punto tocado" (`GradualScroller` no arranca el arrastre si el puntero ya está
     * sobre una zona interactiva).
     */
    private hitAreaSizeFor(assetKey: string): number {
        const targetSize = InteractiveElementAnimations.targetSizeFor(assetKey)
        if (!targetSize) return WORLD_MAP_CONFIG.minHitAreaSize
        return Math.max(WORLD_MAP_CONFIG.minHitAreaSize, targetSize + HIT_AREA_MARGIN)
    }

    /**
     * Crea la imagen del elemento a partir de `visualAssetKey`. Si la textura no está cargada no se dibuja
     * ningún placeholder: se registra en consola y el elemento entero se omite (sin zona interactiva).
     * Los assetKey con animación propia (ver InteractiveElementAnimations) necesitan un Sprite para poder
     * reproducir sus animaciones; el resto son imágenes estáticas.
     */
    private createVisual(element: WorldDiscoveryElements): InteractiveVisual | undefined {
        const assetKey = element.visualAssetKey
        if (!assetKey || !this.scene.textures.exists(assetKey)) {
            console.log(`InteractiveLayer: no existe el assetKey "${assetKey}", se omite el elemento`)
            return undefined
        }

        const visual = InteractiveElementAnimations.needsSprite(assetKey)
            ? this.scene.add.sprite(0, 0, assetKey)
            : this.scene.add.image(0, 0, assetKey)
        InteractiveElementAnimations.applyTargetSize(visual, assetKey)
        return visual
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
