import { Scene } from "phaser"
import { WORLD_MAP_CONFIG } from "../config/worldMapConfig"

interface ScrollLayer {
    container: Phaser.GameObjects.Container
    factor: number
}

function clamp(value: number, min: number, max: number): number {
    return Math.min(Math.max(value, min), max)
}

// Nombre del evento de escena que anuncia si el paisaje se está desplazando
// ahora mismo (arrastre activo o inercia tras soltar).
export const WORLDMAP_SCROLL_MOVE_EVENT = 'worldmap-scroll-move'

// Emitido en pointerup cuando el gesto fue un toque (desplazamiento total por
// debajo de WORLD_MAP_CONFIG.tapMaxDistance), no un arrastre. Payload: la
// coordenada X de pantalla del toque. WorldMapScene lo usa para dirigir a Nubi
// hacia ese punto.
export const WORLDMAP_TAP_EVENT = 'worldmap-tap'

export class GradualScroller {
    private scene: Scene
    private reducedMotion: boolean

    private offset = 0
    private targetOffset = 0
    private velocity = 0
    private dragging = false
    private pointerStartX = 0
    private offsetStartDrag = 0
    private pointerDownX = 0

    private layers: ScrollLayer[] = []
    private inertiaTween?: Phaser.Tweens.Tween
    private moving = false

    private onPointerDownHandler = (pointer: Phaser.Input.Pointer, currentlyOver: Phaser.GameObjects.GameObject[]) => this.onPointerDown(pointer, currentlyOver)
    private onPointerMoveHandler = (pointer: Phaser.Input.Pointer) => this.onPointerMove(pointer)
    private onPointerUpHandler = (pointer: Phaser.Input.Pointer) => this.onPointerUp(pointer)

    constructor(scene: Scene, reducedMotion: boolean) {
        this.scene = scene
        this.reducedMotion = reducedMotion
    }

    attach() {
        this.scene.input.on('pointerdown', this.onPointerDownHandler)
        this.scene.input.on('pointermove', this.onPointerMoveHandler)
        this.scene.input.on('pointerup', this.onPointerUpHandler)
        this.scene.input.on('pointerupoutside', this.onPointerUpHandler)
    }

    registerLayer(container: Phaser.GameObjects.Container, factor: number) {
        this.layers.push({ container, factor })
        this.applyOffset()
    }

    getOffset(): number {
        return this.offset
    }

    // Avanza el offset por `delta` (mismo signo/magnitud que el paso mundial que
    // acaba de dar Nubi), respetando el mismo límite [0, maxScrollOffset] que el
    // arrastre. No hace nada mientras el jugador está arrastrando manualmente:
    // el gesto manual tiene prioridad sobre el seguimiento automático de Nubi.
    followStep(delta: number) {
        if (this.dragging || delta === 0) return
        this.offset = clamp(this.offset + delta, 0, WORLD_MAP_CONFIG.maxScrollOffset)
        this.applyOffset()
    }

    update(deltaMs: number) {
        if (!this.dragging) return

        const maxStep = WORLD_MAP_CONFIG.scrollMaxSpeed * (deltaMs / 1000)
        const diff = this.targetOffset - this.offset
        const step = clamp(diff, -maxStep, maxStep)

        if (step === 0) {
            this.velocity = 0
            this.setMoving(false)
            return
        }

        this.offset += step
        this.velocity = deltaMs > 0 ? step / (deltaMs / 1000) : 0
        this.applyOffset()
        this.setMoving(true)
    }

    private onPointerDown(pointer: Phaser.Input.Pointer, currentlyOver: Phaser.GameObjects.GameObject[]) {
        if (currentlyOver && currentlyOver.length > 0) return

        this.dragging = true
        this.pointerStartX = pointer.x
        this.pointerDownX = pointer.x
        this.offsetStartDrag = this.offset
        this.targetOffset = this.offset
        this.velocity = 0
        this.inertiaTween?.stop()
    }

    private onPointerMove(pointer: Phaser.Input.Pointer) {
        if (!this.dragging) return
        const delta = pointer.x - this.pointerStartX
        this.targetOffset = clamp(this.offsetStartDrag - delta, 0, WORLD_MAP_CONFIG.maxScrollOffset)
    }

    private onPointerUp(pointer: Phaser.Input.Pointer) {
        if (!this.dragging) return
        this.dragging = false

        // Un toque (desplazamiento total mínimo) no es un arrastre: se trata como
        // petición de "ir aquí" para Nubi, no como scroll del paisaje ni inercia.
        if (Math.abs(pointer.x - this.pointerDownX) < WORLD_MAP_CONFIG.tapMaxDistance) {
            this.velocity = 0
            this.setMoving(false)
            this.scene.events.emit(WORLDMAP_TAP_EVENT, pointer.x)
            return
        }

        if (this.reducedMotion || Math.abs(this.velocity) < 1) {
            this.velocity = 0
            this.setMoving(false)
            return
        }

        const distance = this.velocity * (WORLD_MAP_CONFIG.inertiaDuration / 1000) * 0.5
        const target = clamp(this.offset + distance, 0, WORLD_MAP_CONFIG.maxScrollOffset)
        const proxy = { offset: this.offset }

        this.inertiaTween = this.scene.tweens.add({
            targets: proxy,
            offset: target,
            duration: WORLD_MAP_CONFIG.inertiaDuration,
            ease: WORLD_MAP_CONFIG.inertiaEase,
            onUpdate: () => {
                this.offset = proxy.offset
                this.applyOffset()
            },
            onComplete: () => {
                this.setMoving(false)
            }
        })
    }

    private setMoving(moving: boolean) {
        if (this.moving === moving) return
        this.moving = moving
        this.scene.events.emit(WORLDMAP_SCROLL_MOVE_EVENT, moving)
    }

    private applyOffset() {
        this.layers.forEach(({ container, factor }) => {
            container.x = -this.offset * factor
        })
    }

    destroy() {
        this.inertiaTween?.stop()
        this.scene.input.off('pointerdown', this.onPointerDownHandler)
        this.scene.input.off('pointermove', this.onPointerMoveHandler)
        this.scene.input.off('pointerup', this.onPointerUpHandler)
        this.scene.input.off('pointerupoutside', this.onPointerUpHandler)
        this.layers = []
    }
}
