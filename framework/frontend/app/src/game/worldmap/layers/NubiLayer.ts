import { Scene } from "phaser"
import { WORLD_MAP_CONFIG } from "../config/worldMapConfig"

const NUBI_DEPTH = 3
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
    // Oculta a Nubi mientras el selector de biomas (overlay a pantalla
    // completa) está abierto, independientemente de npcEnabled — se compone
    // con él en applyVisibility() en vez de pisarlo, para que reabrir el
    // selector con el NPC desactivado no "reactive" a Nubi por error.
    private selectorOpen = false
    // Posición de Nubi en coordenadas de mundo (mismo espacio que el ground/
    // interactive layer, factor 1), no de pantalla. La posición de pantalla se
    // deriva cada frame como worldX - offset (ver syncScreenPosition), igual
    // que cualquier otro objeto "plantado" en el suelo: si el jugador arrastra
    // el mundo manualmente, Nubi se desplaza en pantalla con él.
    private worldX = 0
    private targetWorldX?: number
    // Invocado cuando Nubi llega al target actual (ver update()). Lo usa
    // WorldMapScene para encadenar "camina hasta el portal, luego transiciona"
    // (bug de naturalidad al pulsar el portal) sin acoplar NubiLayer a la
    // lógica de transición de biomas.
    private pendingArriveCallback?: () => void
    private onNpcStateChanged = (enabled: boolean) => this.setNpcEnabled(enabled)

    constructor(scene: Scene) {
        this.scene = scene
    }

    setPosition(x: number, y: number) {
        this.nubi?.setPosition(x, y)
    }

    /** `yOffset`: ajuste vertical del bioma (positivo = hacia abajo), ver BIOME_Y_OFFSETS. */
    adjustToGroundTopY(groundTopY: number, yOffset: number = 0) {
        if (!this.nubi) return
        this.nubi.y = groundTopY - this.nubi.displayHeight / 2 + yOffset
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
    create(npcEnabled: boolean, groundTopY: number = NUBI_Y, yOffset: number = 0) {
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
        // La escala va ANTES de calcular la Y: displayHeight depende de ella, y con la escala aplicada despues
        // Nubi quedaba flotando (Y calculada con el sprite a tamano completo) hasta que un ajuste posterior la corregia.
        this.nubi.setScale(.4)
        this.nubi.y = groundTopY - this.nubi.displayHeight / 2 + yOffset
        this.nubi.play(IDLE_ANIMATION_KEY)
        this.nubi.setInteractive({ useHandCursor: false })
        let lastTapTime = 0
        this.nubi.on('pointerdown', () => {
            const now = Date.now()
            if (now - lastTapTime < 2000) {
                this.scene.events.emit('nubi-double-tap')
                lastTapTime = 0
            } else {
                lastTapTime = now
            }
        })

        this.setNpcEnabled(npcEnabled)

        this.scene.registry.events.on('npc-state-changed', this.onNpcStateChanged)
        this.scene.events.once('shutdown', () => this.destroy())
        this.scene.events.once('destroy', () => this.destroy())
    }

    // Dirige a Nubi hacia targetWorldX (coordenada de mundo, solo eje X) a pie,
    // en vez del antiguo desplazamiento por arrastre del paisaje. Se llama al
    // tocar el fondo o un elemento interactivo (ver WorldMapScene), que convierte
    // la coordenada de pantalla tocada a mundo sumándole el offset actual.
    // No aplica ningún clamp de rango — quien llama decide si el target debe
    // recortarse al ancho de mundo activo (paseo normal) o puede superarlo a
    // propósito (p. ej. caminar hasta el portal de salida, que vive fuera de
    // ese rango — ver WorldMapScene.handlePortalTouched).
    //
    // onArrive: callback opcional invocado cuando Nubi llega al target (ver
    // update()). Si el NPC está desactivado, Nubi no camina visualmente pero
    // el callback se invoca igualmente de inmediato — quien llama no debe
    // depender de ver a Nubi moverse para que su transición ocurra.
    walkTo(targetWorldX: number, onArrive?: () => void) {
        if (!this.nubi || !this.npcEnabled) {
            onArrive?.()
            return
        }

        const wasWalking = this.targetWorldX !== undefined
        this.targetWorldX = targetWorldX
        this.pendingArriveCallback = onArrive
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
            const onArrive = this.pendingArriveCallback
            this.pendingArriveCallback = undefined
            onArrive?.()
            return step
        }

        const step = Math.sign(diff) * maxStep
        this.worldX += step
        return step
    }

    // Reposiciona a Nubi en el punto de inicio del bioma activo, sin
    // animación de caminata — usado al completar una transición real de
    // bioma (bajo el fundido a negro, invisible para el niño), para que no
    // aparezca en el sitio donde estaba en el bioma anterior (p. ej. junto
    // al portal de salida, fuera del ancho del nuevo mundo).
    resetToStart() {
        this.setWorldX(WORLD_MAP_CONFIG.viewportWidth / 6)
    }

    /** Posicion actual de Nubi en coordenadas de mundo (la que tiene ahora, aunque este caminando hacia otro punto). */
    getWorldX(): number {
        return this.worldX
    }

    // Coloca a Nubi en una coordenada de mundo concreta sin caminar — usado
    // también al reanudar una sesión persistida, para alinear a Nubi con el
    // offset de cámara restaurado en vez de dejarlo en el spawn point fijo.
    setWorldX(worldX: number) {
        this.worldX = worldX
        this.targetWorldX = undefined
        this.pendingArriveCallback = undefined
        this.nubi?.setFlipX(false)
        this.runAnimation(IDLE_ANIMATION_KEY)
    }

    // Oculta/muestra a Nubi mientras el selector de biomas está abierto (ver
    // comentario de `selectorOpen`). Se compone con npcEnabled en
    // applyVisibility() en vez de forzar la visibilidad directamente.
    setSelectorOpen(open: boolean) {
        this.selectorOpen = open
        this.applyVisibility()
    }

    private applyVisibility() {
        this.nubi?.setVisible(this.npcEnabled && !this.selectorOpen)
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
        this.applyVisibility()
    }

    destroy() {
        this.scene.registry.events.off('npc-state-changed', this.onNpcStateChanged)
        this.nubi?.destroy()
        this.nubi = undefined
    }
}
