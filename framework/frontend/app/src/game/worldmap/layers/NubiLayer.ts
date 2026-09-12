import { Scene } from "phaser"
import { WORLD_MAP_CONFIG } from "../config/worldMapConfig"

const NUBI_DEPTH = 4
const NUBI_Y = 550

// Placeholder único mientras no existan las animaciones reales walk-left/
// walk-right/idle: se usa 'greetings' tanto para caminar como para el reposo
// al llegar. Sustituir por las claves reales cuando estén disponibles.
const WALK_ANIMATION_KEY = 'greetings'
const IDLE_ANIMATION_KEY = 'greetings'

export class NubiLayer {
    private scene: Scene
    private nubi?: Phaser.GameObjects.Sprite
    private npcEnabled = false
    // Posición de Nubi en coordenadas de mundo (mismo espacio que el ground/
    // interactive layer, factor 1), no de pantalla. La posición de pantalla se
    // deriva cada frame como worldX - offset (ver syncScreenPosition), igual
    // que cualquier otro objeto "plantado" en el suelo: si el jugador arrastra
    // el mundo manualmente, Nubi se desplaza en pantalla con él.
    private worldX = 0
    private targetWorldX?: number
    private onNpcStateChanged = (enabled: boolean) => this.setNpcEnabled(enabled)

    constructor(scene: Scene) {
        this.scene = scene
    }

    setPosition(x: number, y: number) {
        this.nubi?.setPosition(x, y)
    }

    runAnimation(animation: string) {
        this.nubi?.play(animation)
    }

    setScale(scale: number) {
        this.nubi?.setScale(scale)
    }

    preload() {
        this.scene.load.spritesheet('nubi-greetings', '/assets/animations/nubi/greetings.png', {
            frameWidth: 400,
            frameHeight: 400
        })
    }

    // groundTopY: borde superior de la franja de GroundLayer (viewportHeight -
    // GroundLayer.getBandHeight()). El sprite mantiene su origin por defecto
    // (0.5, 0.5, centrado) para no alterar el significado de setPosition() en
    // otros usos (p. ej. LoadingScene la trata como centro); el ajuste al
    // borde del suelo se hace aquí restando la mitad de su alto real.
    create(npcEnabled: boolean, groundTopY: number = NUBI_Y) {
        const { viewportWidth } = WORLD_MAP_CONFIG
        const x = viewportWidth / 6
        this.worldX = x

        if (!this.scene.anims.exists('greetings')) {
            this.scene.anims.create({
                key: 'greetings',
                frames: this.scene.anims.generateFrameNumbers('nubi-greetings', { start: 0, end: 29 }),
                frameRate: 15,
                repeat: -1
            })
        }

        this.nubi = this.scene.add.sprite(x, groundTopY, 'nubi-greetings')
        this.nubi.setDepth(NUBI_DEPTH)
        this.nubi.y = groundTopY - this.nubi.displayHeight / 2
        this.nubi.play(IDLE_ANIMATION_KEY)

        this.setNpcEnabled(npcEnabled)

        this.scene.events.on('npc-state-changed', this.onNpcStateChanged)
        this.scene.events.once('shutdown', () => this.destroy())
        this.scene.events.once('destroy', () => this.destroy())
    }

    // Dirige a Nubi hacia targetWorldX (coordenada de mundo, solo eje X) a pie,
    // en vez del antiguo desplazamiento por arrastre del paisaje. Se llama al
    // tocar el fondo o un elemento interactivo (ver WorldMapScene), que convierte
    // la coordenada de pantalla tocada a mundo sumándole el offset actual.
    walkTo(targetWorldX: number) {
        if (!this.nubi || !this.npcEnabled) return

        const wasWalking = this.targetWorldX !== undefined
        this.targetWorldX = targetWorldX
        this.nubi.setFlipX(targetWorldX < this.worldX)

        if (!wasWalking) {
            this.runAnimation(WALK_ANIMATION_KEY)
        }
    }

    // Avanza worldX hacia el objetivo y devuelve cuánto se movió este frame (0
    // si Nubi no está caminando). WorldMapScene usa ese delta para pedirle a
    // GradualScroller que la cámara siga a Nubi (GradualScroller.followStep),
    // en vez de mover directamente la posición de pantalla aquí.
    update(deltaMs: number): number {
        if (!this.nubi || this.targetWorldX === undefined) return 0

        const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
        const diff = this.targetWorldX - this.worldX
        const maxStep = WORLD_MAP_CONFIG.nubiWalkSpeed * (deltaMs / 1000)

        if (reducedMotion || Math.abs(diff) <= maxStep) {
            const step = diff
            this.worldX = this.targetWorldX
            this.targetWorldX = undefined
            this.runAnimation(IDLE_ANIMATION_KEY)
            return step
        }

        const step = Math.sign(diff) * maxStep
        this.worldX += step
        return step
    }

    // Sincroniza la posición de pantalla con worldX y el offset de cámara
    // actuales. Debe llamarse cada frame (caminando o no) para que Nubi
    // acompañe también los arrastres manuales del jugador, igual que el
    // ground/interactive layer (factor 1). Se recorta con la mitad del ancho
    // real de su sprite para que Nubi nunca sobrepase visualmente el borde del
    // viewport, aunque worldX - offset caiga justo en el límite.
    syncScreenPosition(offset: number) {
        if (!this.nubi) return

        const halfWidth = this.nubi.displayWidth / 2
        const rawX = this.worldX - offset
        this.nubi.x = Math.min(Math.max(rawX, halfWidth), WORLD_MAP_CONFIG.viewportWidth - halfWidth)
    }

    setNpcEnabled(enabled: boolean) {
        this.npcEnabled = enabled
        this.nubi?.setVisible(enabled)
    }

    destroy() {
        this.scene.events.off('npc-state-changed', this.onNpcStateChanged)
        this.nubi?.destroy()
        this.nubi = undefined
    }
}
