import { Scene, GameObjects, Scale, Time } from "phaser"
import {
    AvatarEvent,
    GameAbandonEvent,
    GameMemoryActionEvent,
    GameReadyEvent,
    GameStartEvent,
    ServerGameEvent,
    type MemoryCard,
    type MemoryState,
    type RecognitionElement
} from "./GameEvent"
import { RoundProgressBar } from "./ui/RoundProgressBar"
import { ExitButton } from "./ui/ExitButton"
import { ViewportBackdrop } from "./ui/ViewportBackdrop"
import { FireworksCelebration } from "./ui/FireworksCelebration"
import { DynamicAssetLoader, MEMORY_CARD_COVER_KEY, MEMORY_CARD_REVERSE_KEY } from "./utils/DynamicAssetLoader"
import { ResponsiveLayout, type LayoutSizes, type MemoryGrid } from "./utils/ResponsiveLayout"
import { DEVICE_PROFILE_REGISTRY_KEY, detectDeviceProfile, type DeviceProfile } from "./utils/DeviceProfile"
import { RoundAudioCache } from "./RoundAudioCache"
import { MessageRouter } from "@/services/MessageRouter"
import type { AudioService } from "@/services/AudioService"
import type { AudioCache } from "@/services/AudioCache"
import { MinigameNubiLayer } from "./worldmap/layers/MinigameNubiLayer"

const FADE_DURATION = 400
// If the board never gets drawn (error, no elements) do not leave the screen black forever.
const INTRO_MAX_WAIT_MS = 10000
const KEEP_RECENT_ROUNDS = 3
// Flip: the card narrows to nothing, swaps side and widens again (scaleX 1 -> 0 -> 1).
const FLIP_HALF_DURATION = 140
const MATCH_POP_SCALE = 1.08
const MATCH_POP_DURATION = 160
const CARD_DEPTH = 1
const CELEBRATION_DEPTH = 50
// The element takes this share of the face of the card.
const ELEMENT_WIDTH_RATIO = 0.78
const ELEMENT_HEIGHT_RATIO = 0.66
const PLACEHOLDER_COVER_COLOR = 0xEF7A92
const PLACEHOLDER_FACE_COLOR = 0xDDDDDD
// Wooden board (9-slice), same as the recognition minigames.
const BOARD_TEXTURE_KEY = 'tablero-minigame'
const BOARD_SLICE_LEFT = 40
const BOARD_SLICE_RIGHT = 40
const BOARD_SLICE_TOP = 100
const BOARD_SLICE_BOTTOM = 104
const BOARD_BORDER_SCALE = 0.4
const BOARD_DEPTH = -0.5

const BIOME_GRADIENTS: Record<string, [number, number]> = {
    meadow:    [0xc8e6c9, 0x81c784],
    farm:      [0xfff9c4, 0xffd54f],
    woods:     [0xa5d6a7, 0x388e3c],
    beach:     [0xb3e5fc, 0x4fc3f7],
    space:     [0x1a237e, 0x4a148c],
    prehistory:[0xd7ccc8, 0x8d6e63]
}

/** What the scene keeps of each card of the board. */
interface CardView {
    cardId: string
    row: number
    column: number
    container: GameObjects.Container
    cover: GameObjects.Image | GameObjects.Rectangle
    face: GameObjects.Image | GameObjects.Rectangle
    /** Element drawn on the face; only exists while the card is face up. */
    content?: GameObjects.Image | GameObjects.Text
    elementId: string | null
    /** Face up on screen (or turning that way). */
    shown: boolean
    matched: boolean
}

/**
 * Memory game (ADR-030): the child turns cards two by two looking for pairs.
 *
 * The server decides everything; the scene only draws its state. A card that does not match its partner is
 * turned back on its own after `flipBackDelayMs`, with no sound, no shake and no failure counter; matched cards
 * stay face up until the game ends. Touching another card while a pair is on show turns that pair back at once,
 * so no touch is lost. It can be played without sound, reading or colour as the only signal.
 */
export class MemoryGameScene extends Scene {
    websocket?: WebSocket
    activityId?: number
    biome?: string

    startingGame: boolean = true
    blockActions: boolean = true
    dateClick: number = Date.now()

    private cards = new Map<string, CardView>()
    private elements = new Map<string, RecognitionElement>()
    private grid?: MemoryGrid
    private boardState?: MemoryState
    private pendingFlipBack?: { cardIds: string[], timer: Time.TimerEvent }
    private progressBar?: RoundProgressBar
    private nubiLayer?: MinigameNubiLayer
    private exitButton?: ExitButton
    private roundAudio = new RoundAudioCache()
    private assetLoader?: DynamicAssetLoader
    private loadToken = 0
    private pendingAudioListener?: { id: string, handler: (audioId: string) => void }
    private reducedMotion: boolean = false
    private exitInProgress: boolean = false
    private completing: boolean = false
    private sessionId?: number
    private childId?: number
    private layout!: ResponsiveLayout
    private sizes!: LayoutSizes
    private previousScaleMode?: Scale.ScaleModeType
    private previousAutoCenter?: number
    private fitActive = false
    private backdrop!: ViewportBackdrop
    private backgroundTextureKey: string = ''
    private board?: GameObjects.NineSlice
    private introOverlay?: GameObjects.Rectangle
    private introTimeout?: Time.TimerEvent
    private introStartedAt = 0

    constructor() { super({ key: 'memory-game', active: false }) }

    /** Textura del fondo del bioma en uso; vacia si se usa el degradado de respaldo. */
    get backgroundKey(): string {
        return this.backgroundTextureKey
    }

    init(data: {
        websocket: WebSocket,
        activityId: number,
        biome?: string,
        sessionId?: number,
        childId?: number
    }) {
        this.websocket = data.websocket
        this.activityId = data.activityId
        this.biome = data.biome
        this.sessionId = data.sessionId
        this.childId = data.childId
        this.startingGame = true
        this.blockActions = true
        this.completing = false
        this.cards = new Map()
        this.elements = new Map()
        this.grid = undefined
        this.boardState = undefined
        this.pendingFlipBack = undefined
        this.roundAudio = new RoundAudioCache(this.registry.get('audioCache') as AudioCache | undefined)
        this.assetLoader = new DynamicAssetLoader(this)
        // The manifest is requested now, in parallel with game_start / game_ready.
        DynamicAssetLoader.warmUp()
        this.loadToken++

        let profile = this.registry.get(DEVICE_PROFILE_REGISTRY_KEY) as DeviceProfile | undefined
        if (!profile) {
            profile = detectDeviceProfile()
            this.registry.set(DEVICE_PROFILE_REGISTRY_KEY, profile)
        }
        this.layout = new ResponsiveLayout(profile)
        this.enterFitScaling()
        this.refreshSizes()
    }

    create() {
        this.reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
        this.exitInProgress = false

        this.backdrop = new ViewportBackdrop(this)
        this.createBiomeBackground()
        this.createBoard()

        this.refreshSizes()
        this.progressBar = new RoundProgressBar(this, this.sizes)

        const npcEnabled = this.registry.get('npcEnabled') as boolean ?? false
        this.nubiLayer = new MinigameNubiLayer(this, this.sizes)
        this.nubiLayer.create(npcEnabled)

        this.exitButton = new ExitButton(this, this.sizes)
        this.scale.on('resize', this.onScaleResize)
        // The celebration spritesheet loads while the child plays.
        FireworksCelebration.preload(this)

        this.events.on('exit-button-double-tap', this.onExitDoubleTap)
        this.events.on('minigame-nubi-tap', this.onNubiTap)
        this.events.on('pause', this.onScenePause)
        this.events.on('resume', this.onSceneResume)

        this.holdIntroOverlay()

        this.events.once('shutdown', () => this.cleanup())
    }

    preload() {
        if (this.websocket) {
            this.manageWs(this.websocket)
        }
    }

    // --- Scaling, background and board (same as the recognition minigames) ---------------------------------

    private onScaleResize = () => {
        this.layoutBoard()
        this.introOverlay?.setPosition(this.scale.width / 2, this.scale.height / 2).setSize(this.scale.width, this.scale.height)
        this.relayout()
    }

    // Paused = portrait: OrientationRequiredScene takes over and needs the normal (cover) scaling to stay legible.
    private onScenePause = () => {
        this.blockActions = true
        this.backdrop?.clear()
        this.restoreScaling()
    }

    private onSceneResume = () => {
        this.blockActions = !this.boardState || this.completing
        this.enterFitScaling()
        if (this.backgroundTextureKey && !this.introOverlay) this.backdrop?.apply(this.backgroundTextureKey)
    }

    /** While the minigame lasts the canvas fits (FIT) so all the content is visible on any aspect ratio. */
    private enterFitScaling(): void {
        if (this.fitActive) return
        this.fitActive = true
        this.previousScaleMode = this.scale.scaleMode
        this.scale.scaleMode = Scale.FIT
        this.scale.displaySize.setAspectMode(Scale.FIT)
        this.disableAutoCenterInFlexParent()
        this.scale.refresh()
    }

    /** The flex parent already centres the canvas: with FIT `autoCenter` would add its margins on top. */
    private disableAutoCenterInFlexParent(): void {
        const parent = this.scale.canvas?.parentElement
        if (!parent || getComputedStyle(parent).display !== 'flex') return
        this.previousAutoCenter = this.scale.autoCenter
        this.scale.autoCenter = Scale.NO_CENTER
        this.scale.canvas.style.marginLeft = ''
        this.scale.canvas.style.marginTop = ''
    }

    private restoreScaling(): void {
        if (!this.fitActive || this.previousScaleMode === undefined) return
        this.fitActive = false
        this.scale.scaleMode = this.previousScaleMode
        this.scale.displaySize.setAspectMode(this.previousScaleMode)
        this.previousScaleMode = undefined
        if (this.previousAutoCenter !== undefined) {
            this.scale.autoCenter = this.previousAutoCenter
            this.previousAutoCenter = undefined
        }
        this.scale.refresh()
    }

    private refreshSizes(): void {
        this.sizes = this.layout.calculateSizes({
            viewportWidth: this.scale.width,
            viewportHeight: this.scale.height,
            displayScale: this.scale.displayScale.x
        })
    }

    private relayout(): void {
        if (!this.layout || this.exitInProgress) return

        this.refreshSizes()
        this.progressBar?.resize(this.sizes)
        this.exitButton?.resize(this.sizes)
        this.nubiLayer?.resize(this.sizes)
        this.layoutCards()
    }

    /** Biome background: an image behind the transparent canvas (see ViewportBackdrop) or a gradient fallback. */
    private createBiomeBackground(): void {
        const normalizedBiome = (this.biome ?? 'meadow').toLowerCase()

        const imageKey = `minigame-background-${normalizedBiome}`
        if (this.textures.exists(imageKey)) {
            this.backgroundTextureKey = imageKey
            return
        }

        this.backgroundTextureKey = ''
        const [colorTop, colorBottom] = BIOME_GRADIENTS[normalizedBiome] ?? BIOME_GRADIENTS['meadow']

        const graphics = this.add.graphics()
        graphics.setDepth(-1)
        const steps = 8
        const stepHeight = this.scale.height / steps
        for (let i = 0; i < steps; i++) {
            const ratio = i / (steps - 1)
            const r = ((colorTop >> 16) & 0xff) * (1 - ratio) + ((colorBottom >> 16) & 0xff) * ratio
            const g = ((colorTop >> 8) & 0xff) * (1 - ratio) + ((colorBottom >> 8) & 0xff) * ratio
            const b = (colorTop & 0xff) * (1 - ratio) + (colorBottom & 0xff) * ratio
            graphics.fillStyle((Math.round(r) << 16) | (Math.round(g) << 8) | Math.round(b), 1)
            graphics.fillRect(0, i * stepHeight, this.scale.width, stepHeight + 1)
        }
    }

    private createBoard(): void {
        if (!this.textures.exists(BOARD_TEXTURE_KEY)) return

        this.board = this.add.nineslice(
            0, 0, BOARD_TEXTURE_KEY, undefined, 0, 0,
            BOARD_SLICE_LEFT, BOARD_SLICE_RIGHT, BOARD_SLICE_TOP, BOARD_SLICE_BOTTOM
        )
        this.board.setOrigin(0, 0).setDepth(BOARD_DEPTH).setScale(BOARD_BORDER_SCALE)
        this.layoutBoard()
    }

    private layoutBoard(): void {
        this.board?.setPosition(0, 0).setSize(this.scale.width / BOARD_BORDER_SCALE, this.scale.height / BOARD_BORDER_SCALE)
    }

    /** The transition from the map ends in black; it is held until the board is drawn (`revealIntro`). */
    private holdIntroOverlay(): void {
        this.backdrop.hold()
        this.introOverlay = this.add.rectangle(
            this.scale.width / 2, this.scale.height / 2,
            this.scale.width, this.scale.height, 0x000000, 1
        )
        this.introOverlay.setDepth(100)
        this.introOverlay.setScrollFactor(0)
        this.introStartedAt = performance.now()
        this.introTimeout = this.time.delayedCall(INTRO_MAX_WAIT_MS, () => this.revealIntro())
    }

    private revealIntro(): void {
        const overlay = this.introOverlay
        if (!overlay) return
        this.introOverlay = undefined
        this.introTimeout?.remove(false)
        this.introTimeout = undefined
        console.info(`[MemoryGameScene] tablero listo ${Math.round(performance.now() - this.introStartedAt)} ms tras entrar en la escena`)

        if (this.backgroundTextureKey) this.backdrop.apply(this.backgroundTextureKey)

        this.tweens.add({
            targets: overlay,
            alpha: 0,
            duration: this.reducedMotion ? 200 : FADE_DURATION,
            ease: 'Sine.easeOut',
            onComplete: () => overlay.destroy()
        })
    }

    // --- Exit ---------------------------------------------------------------------------------------------

    private onExitDoubleTap = () => this.sendAbandonAndExit()
    private onNubiTap = () => this.replayRoundAudio()

    private sendAbandonAndExit(): void {
        if (this.exitInProgress) return
        this.exitInProgress = true
        this.blockActions = true

        this.cancelPendingRoundAudio()
        this.getAudioService()?.stop()

        if (this.websocket && this.websocket.readyState === WebSocket.OPEN) {
            this.websocket.send(JSON.stringify(new GameAbandonEvent()))
        }

        this.fadeToBlackAndExit()
    }

    private fadeToBlackAndExit(): void {
        this.exitInProgress = true

        const overlay = this.add.rectangle(
            this.scale.width / 2, this.scale.height / 2,
            this.scale.width, this.scale.height, 0x000000, 0
        )
        overlay.setDepth(100)
        overlay.setScrollFactor(0)

        this.tweens.add({
            targets: overlay,
            alpha: 1,
            duration: this.reducedMotion ? 200 : FADE_DURATION,
            ease: 'Sine.easeIn',
            onComplete: () => {
                this.scene.start('world-map', {
                    websocket: this.websocket,
                    sessionId: this.sessionId,
                    childId: this.childId
                })
            }
        })
    }

    // --- WebSocket ----------------------------------------------------------------------------------------

    manageWs(ws: WebSocket) {
        ws.onmessage = (msg) => {
            MessageRouter.route(
                msg.data,
                (jsonData) => this.readEvent(jsonData as ServerGameEvent | AvatarEvent),
                (binaryData) => { void this.getAudioService()?.handleBinaryFrame(binaryData) }
            )
        }

        if (this.activityId && this.startingGame === true) {
            this.startingGame = false
            const startEvent = new GameStartEvent()
            startEvent.activityId = this.activityId
            if (ws.readyState === WebSocket.OPEN) {
                ws.send(JSON.stringify(startEvent))
            } else {
                console.warn('WebSocket not open, cannot send game_start. ReadyState:', ws.readyState)
            }
        }
    }

    readEvent(event: ServerGameEvent | AvatarEvent) {
        if (!this.websocket) return
        if (!event) return
        if (event.event === 'GAME_AVATAR_EVENT') {
            if (event.eventType === 'ROUND_PROMPT') {
                this.handleRoundPrompt(event)
            }
            return
        }
        switch (event.event) {
            case 'GAME_STARTED':
                if (event.payload.engine === 'MEMORY') {
                    this.websocket.send(JSON.stringify(new GameReadyEvent()))
                }
                break
            case 'GAME_READY':
                if (event.payload.engine === 'MEMORY' && event.payload.memoryState) {
                    this.startBoard(event.payload.memoryState)
                }
                break
            case 'GAME_ACTION_RESULT': {
                const state = event.payload.updatedState
                if (state && state.engine === 'MEMORY' && state.memoryState) {
                    this.applyResult(state.memoryState, event.payload.gameCompleted)
                }
                break
            }
        }
    }

    // --- Board --------------------------------------------------------------------------------------------

    /** Loads the images of the board (only the missing ones) and draws it once they are ready. */
    private startBoard(state: MemoryState): void {
        if (!this.assetLoader) return

        const token = ++this.loadToken
        this.mergeElements(state.elements)
        this.assetLoader.loadRoundAssets(state.elements, 'MEMORY').then(() => {
            if (token !== this.loadToken) return

            this.renderBoard(state)
            this.assetLoader?.cleanupOldTextures(KEEP_RECENT_ROUNDS)
            this.progressBar?.updateProgress(state.matchedPairs, state.totalPairs)
            this.dateClick = Date.now()
            this.blockActions = false
            this.revealIntro()
            this.nubiLayer?.showPhrase('welcome')
        })
    }

    private mergeElements(elements: RecognitionElement[] | undefined): void {
        elements?.forEach(element => this.elements.set(element.id, element))
    }

    private renderBoard(state: MemoryState): void {
        this.clearBoard()
        this.boardState = state
        this.grid = this.layout.getMemoryGrid(state.rows, state.columns)

        state.cards.forEach(card => {
            const view = this.createCardView(card)
            this.cards.set(card.cardId, view)
            if (card.faceUp || card.matched) {
                view.shown = true
                view.matched = card.matched
                this.setFace(view, true, card.elementId ?? null)
            }
        })
        this.layoutCards()
    }

    private clearBoard(): void {
        this.cancelPendingFlipBack()
        this.cards.forEach(view => view.container.destroy(true))
        this.cards.clear()
    }

    private createCardView(card: MemoryCard): CardView {
        const container = this.add.container(0, 0)
        container.setDepth(CARD_DEPTH)
        container.setData('cardId', card.cardId)

        const face = this.textures.exists(MEMORY_CARD_REVERSE_KEY)
            ? this.add.image(0, 0, MEMORY_CARD_REVERSE_KEY)
            : this.add.rectangle(0, 0, 10, 10, PLACEHOLDER_FACE_COLOR)
        const cover = this.textures.exists(MEMORY_CARD_COVER_KEY)
            ? this.add.image(0, 0, MEMORY_CARD_COVER_KEY)
            : this.add.rectangle(0, 0, 10, 10, PLACEHOLDER_COVER_COLOR)
        face.setVisible(false)
        container.add([face, cover])

        const view: CardView = {
            cardId: card.cardId,
            row: card.row,
            column: card.column,
            container,
            cover,
            face,
            elementId: null,
            shown: false,
            matched: false
        }

        container.setSize(10, 10)
        container.setInteractive({ useHandCursor: true })
        container.on('pointerdown', () => this.onCardTap(card.cardId))
        return view
    }

    /** Places and sizes every card (also on resize): the whole card is the touch area. */
    private layoutCards(): void {
        if (!this.boardState) return
        this.grid = this.layout.getMemoryGrid(this.boardState.rows, this.boardState.columns)
        const grid = this.grid

        this.cards.forEach(view => {
            const center = ResponsiveLayout.memoryCardCenter(grid, view.row, view.column)
            view.container.setPosition(center.x, center.y)
            view.cover.setDisplaySize(grid.cardWidth, grid.cardHeight)
            view.face.setDisplaySize(grid.cardWidth, grid.cardHeight)
            view.container.setSize(grid.cardWidth, grid.cardHeight)
            const hitArea = view.container.input?.hitArea as Phaser.Geom.Rectangle | undefined
            hitArea?.setTo(0, 0, grid.cardWidth, grid.cardHeight)
            this.fitContent(view)
        })
    }

    /** Draws the element on the face of the card, fitted inside it keeping its proportions. */
    private fitContent(view: CardView): void {
        const grid = this.grid
        const content = view.content
        if (!grid || !content) return

        if (content instanceof GameObjects.Image) {
            const scale = Math.min(
                grid.cardWidth * ELEMENT_WIDTH_RATIO / content.width,
                grid.cardHeight * ELEMENT_HEIGHT_RATIO / content.height
            )
            content.setScale(scale)
        } else {
            content.setFontSize(Math.max(this.sizes.minFontSize, grid.cardWidth * 0.22))
        }
    }

    /** Shows the card face up (with its element) or face down, instantly. */
    private setFace(view: CardView, up: boolean, elementId: string | null): void {
        view.cover.setVisible(!up)
        view.face.setVisible(up)
        view.content?.destroy()
        view.content = undefined
        view.elementId = up ? elementId : null

        if (up && elementId) {
            view.content = this.createContent(elementId)
            view.container.add(view.content)
        }
        this.fitContent(view)
    }

    private createContent(elementId: string): GameObjects.Image | GameObjects.Text {
        const element = this.elements.get(elementId)
        const key = element ? DynamicAssetLoader.textureKey(element, 'MEMORY') : null
        if (key && this.textures.exists(key)) {
            return this.add.image(0, 0, key)
        }
        // Placeholder while the image is missing: the game stays playable.
        return this.add.text(0, 0, element?.displayValue ?? '?', {
            fontFamily: 'sans-serif',
            color: '#333333',
            align: 'center'
        }).setOrigin(0.5)
    }

    /** Horizontal flip: the card narrows to nothing, changes side and widens again. */
    private flip(view: CardView, up: boolean, elementId: string | null, onDone?: () => void): void {
        if (this.reducedMotion) {
            this.setFace(view, up, elementId)
            onDone?.()
            return
        }

        this.tweens.add({
            targets: view.container,
            scaleX: 0,
            duration: FLIP_HALF_DURATION,
            ease: 'Sine.easeIn',
            onComplete: () => {
                this.setFace(view, up, elementId)
                this.tweens.add({
                    targets: view.container,
                    scaleX: 1,
                    duration: FLIP_HALF_DURATION,
                    ease: 'Sine.easeOut',
                    onComplete: () => onDone?.()
                })
            }
        })
    }

    // --- Playing ------------------------------------------------------------------------------------------

    private onCardTap(cardId: string): void {
        if (this.blockActions || this.exitInProgress || this.introOverlay) return
        if (!this.websocket || this.websocket.readyState !== WebSocket.OPEN) return

        const view = this.cards.get(cardId)
        // A card already face up (the pair on show included) or matched does nothing.
        if (!view || view.matched || view.shown) return

        // The pair on show turns back right away, as the server does when another card is touched.
        this.flushFlipBack()

        this.blockActions = true
        const action = new GameMemoryActionEvent()
        action.setAction(cardId, Date.now() - this.dateClick)
        this.websocket.send(JSON.stringify(action))
    }

    /**
     * Draws the state the server answered with: the card just turned goes face up, matched cards stay up and a
     * non-matching pair is turned back after `flipBackDelayMs`. No sound or shake for a non-matching pair.
     */
    private applyResult(state: MemoryState, complete: boolean): void {
        this.mergeElements(state.elements)
        this.boardState = state
        this.progressBar?.updateProgress(state.matchedPairs, state.totalPairs)

        let pending = 0
        const newlyMatched: CardView[] = []
        const afterFlips = () => {
            if (this.exitInProgress) return
            newlyMatched.forEach(view => this.popMatched(view))
            if (complete) {
                this.completing = true
                this.blockActions = true
                this.nubiLayer?.showPhrase('celebration')
                this.time.delayedCall(this.reducedMotion ? 0 : MATCH_POP_DURATION * 2, () => this.playCelebration())
                return
            }
            if (state.waitingForFlipBack) {
                this.scheduleFlipBack(state.flipBackCardIds, state.flipBackDelayMs)
            }
            this.dateClick = Date.now()
            this.blockActions = false
        }
        const flipDone = () => {
            pending--
            if (pending === 0) afterFlips()
        }

        state.cards.forEach(card => {
            const view = this.cards.get(card.cardId)
            if (!view) return
            const up = card.faceUp || card.matched

            if (card.matched && !view.matched) {
                view.matched = true
                newlyMatched.push(view)
            }
            if (up && !view.shown) {
                view.shown = true
                pending++
                this.flip(view, true, card.elementId ?? null, flipDone)
            } else if (!up && view.shown) {
                // The server turned it back (the pair on show when another card was touched).
                view.shown = false
                pending++
                this.flip(view, false, null, flipDone)
            }
        })

        if (pending === 0) afterFlips()
    }

    private popMatched(view: CardView): void {
        if (this.reducedMotion) return
        this.tweens.add({
            targets: view.container,
            scaleX: MATCH_POP_SCALE,
            scaleY: MATCH_POP_SCALE,
            duration: MATCH_POP_DURATION,
            yoyo: true,
            ease: 'Sine.easeInOut'
        })
    }

    private scheduleFlipBack(cardIds: string[], delayMs: number): void {
        this.cancelPendingFlipBack()
        const ids = cardIds.filter(id => this.cards.has(id))
        if (ids.length === 0) return
        this.pendingFlipBack = {
            cardIds: ids,
            timer: this.time.delayedCall(Math.max(delayMs, 0), () => this.flipBackNow())
        }
    }

    private flipBackNow(): void {
        const pending = this.pendingFlipBack
        if (!pending) return
        this.pendingFlipBack = undefined
        pending.timer.remove(false)

        pending.cardIds.forEach(id => {
            const view = this.cards.get(id)
            if (!view || view.matched || !view.shown) return
            view.shown = false
            this.flip(view, false, null)
        })
    }

    /** Turns the pair on show back now (another card was touched before its time was up). */
    private flushFlipBack(): void {
        if (this.pendingFlipBack) this.flipBackNow()
    }

    private cancelPendingFlipBack(): void {
        this.pendingFlipBack?.timer.remove(false)
        this.pendingFlipBack = undefined
    }

    // --- Nubi audio (single prompt per game) --------------------------------------------------------------

    private getAudioService(): AudioService | undefined {
        return this.registry.get('audioService') as AudioService | undefined
    }

    private isRoundAudioEnabled(): boolean {
        const audioGeneralEnabled = this.registry.get('audioGeneralEnabled') as boolean ?? false
        const ttsEnabled = this.registry.get('ttsEnabled') as boolean ?? false
        return audioGeneralEnabled && ttsEnabled
    }

    private handleRoundPrompt(event: AvatarEvent): void {
        this.cancelPendingRoundAudio()

        const audioId = event.audioAvailable ? event.audioId : undefined
        this.roundAudio.setCurrent(audioId)
        if (!audioId || !this.isRoundAudioEnabled()) return

        const audioService = this.getAudioService()
        if (!audioService) return

        const play = () => {
            audioService.enqueue('dynamic', audioId)
            audioService.playNext()
        }

        if (this.roundAudio.isCached(audioId)) {
            play()
            return
        }

        // The binary frame follows the JSON event and is decoded asynchronously.
        const handler = (receivedId: string) => {
            if (receivedId !== audioId) return
            this.cancelPendingRoundAudio()
            play()
        }
        this.pendingAudioListener = { id: audioId, handler }
        audioService.on('audio-received', handler)
    }

    private cancelPendingRoundAudio(): void {
        if (!this.pendingAudioListener) return
        this.getAudioService()?.off('audio-received', this.pendingAudioListener.handler)
        this.pendingAudioListener = undefined
    }

    /** One tap on Nubi repeats the prompt. */
    private replayRoundAudio(): void {
        if (this.exitInProgress) return
        if (!this.isRoundAudioEnabled()) return

        const audioId = this.roundAudio.getCurrentId()
        if (!audioId || !this.roundAudio.isCached(audioId)) return

        const audioService = this.getAudioService()
        if (!audioService) return

        void audioService.playDynamic(audioId)
        this.nubiLayer?.playSoundWave()
    }

    // --- Celebration --------------------------------------------------------------------------------------

    private playCelebration(): void {
        if (this.exitInProgress) return
        this.playCelebrationSound()

        const { count, durationMs } = FireworksCelebration.play(this, { reducedMotion: this.reducedMotion, depth: CELEBRATION_DEPTH })
        this.celebrationBurstCount = count
        this.time.delayedCall(durationMs, () => this.fadeToBlackAndExit())
    }

    /** Fireworks launched by the celebration (0 until the game completes); read by the tests. */
    celebrationBurstCount: number = 0

    private playCelebrationSound(): void {
        const audioGeneralEnabled = this.registry.get('audioGeneralEnabled') as boolean ?? false
        const ttsEnabled = this.registry.get('ttsEnabled') as boolean ?? false
        if (!audioGeneralEnabled || !ttsEnabled) return

        const audioService = this.registry.get('audioService') as { playStatic?: (key: string) => void } | undefined
        audioService?.playStatic?.('celebration')
    }

    // --- Test support -------------------------------------------------------------------------------------

    /** Snapshot of the board for the tests (Cypress hook in GameView). */
    getBoardSnapshot() {
        return {
            rows: this.boardState?.rows ?? 0,
            columns: this.boardState?.columns ?? 0,
            blockActions: this.blockActions,
            flipBackPending: !!this.pendingFlipBack,
            completing: this.completing,
            celebrationBurstCount: this.celebrationBurstCount,
            backgroundKey: this.backgroundTextureKey,
            cards: [...this.cards.values()].map(view => ({
                cardId: view.cardId,
                row: view.row,
                column: view.column,
                x: view.container.x,
                y: view.container.y,
                width: view.container.width,
                height: view.container.height,
                scaleX: view.container.scaleX,
                shown: view.shown,
                matched: view.matched,
                coverVisible: view.cover.visible,
                faceVisible: view.face.visible,
                elementId: view.elementId,
                contentKey: view.content instanceof GameObjects.Image ? view.content.texture.key : null,
                inputEnabled: view.container.input?.enabled ?? false
            }))
        }
    }

    // --- Cleanup ------------------------------------------------------------------------------------------

    private cleanup(): void {
        this.scale.off('resize', this.onScaleResize)
        this.events.off('pause', this.onScenePause)
        this.events.off('resume', this.onSceneResume)
        this.backdrop?.clear()
        this.restoreScaling()
        this.events.off('exit-button-double-tap', this.onExitDoubleTap)
        this.events.off('minigame-nubi-tap', this.onNubiTap)
        this.cancelPendingRoundAudio()
        this.roundAudio.clear()
        this.cancelPendingFlipBack()
        this.exitButton?.destroy()
        this.exitButton = undefined
        this.progressBar?.destroy()
        this.progressBar = undefined
        this.nubiLayer?.destroy()
        this.nubiLayer = undefined
        this.loadToken++
        this.cards.forEach(view => view.container.destroy(true))
        this.cards.clear()
        this.boardState = undefined
        this.assetLoader?.cleanupOldTextures(0)
        this.assetLoader = undefined
        this.introTimeout?.remove(false)
        this.introTimeout = undefined

        // Clean up WebSocket handlers but don't close the connection
        if (this.websocket) {
            this.websocket.onmessage = null
            this.websocket.onclose = null
            this.websocket.onerror = null
        }
    }
}
