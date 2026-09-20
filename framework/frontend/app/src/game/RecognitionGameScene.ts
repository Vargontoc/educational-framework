import { Scene, GameObjects, Scale } from "phaser"
import {
    AvatarEvent,
    GAME_RESULT_TYPE,
    GameAbandonEvent,
    GameReadyEvent,
    GameRecognitionActionEvent,
    GameStartEvent,
    RECOGNITION_TYPE,
    RecognitionElement,
    RecognitionEnginePayload,
    ServerGameEvent
} from "./GameEvent"
import { RoundProgressBar } from "./ui/RoundProgressBar"
import { ExitButton } from "./ui/ExitButton"
import { createSplashCompound, layoutSplashCompound } from "./ui/SplashCompound"
import { DynamicAssetLoader } from "./utils/DynamicAssetLoader"
import { RecognitionColorizer } from "./utils/RecognitionColorizer"
import { ResponsiveLayout, type LayoutSizes, type OptionSlot } from "./utils/ResponsiveLayout"
import { DEVICE_PROFILE_REGISTRY_KEY, detectDeviceProfile, type DeviceProfile } from "./utils/DeviceProfile"
import { RoundAudioCache } from "./RoundAudioCache"
import { MessageRouter } from "@/services/MessageRouter"
import type { AudioService } from "@/services/AudioService"
import type { AudioCache } from "@/services/AudioCache"
import { MinigameNubiLayer } from "./worldmap/layers/MinigameNubiLayer"
import {
    generateColorTexture,
    type ColorShape
} from "../utils/colorTextureGenerator"

type RoundObject =
    Phaser.GameObjects.Image
    | Phaser.GameObjects.Rectangle
    | Phaser.GameObjects.Text
    | Phaser.GameObjects.Container

const FADE_DURATION = 400
const FEEDBACK_DELAY = 500
const FEEDBACK_WOBBLE_PX = 8
const FEEDBACK_WOBBLE_DURATION = 300
const FEEDBACK_WOBBLE_CYCLES = 2
const FEEDBACK_SCALE_TO = 1.15
const FEEDBACK_SCALE_DURATION = 200
const CORRECT_TINT = 0x4CAF50
const CORRECT_TINT_ALPHA = 0.3
const PARTICLE_DURATION = 400
const HINT_BORDER_COLOR = 0xFFC107
const HINT_BORDER_THICKNESS = 4
const HINT_BORDER_PADDING = 8
const HINT_PULSE_DURATION = 1500
const HINT_PULSE_ALPHA_MIN = 0.4
const HINT_PULSE_ALPHA_MAX = 1.0
const HINT_STATIC_ALPHA = 0.7
const CELEBRATION_STAR_COUNT_MIN = 3
const CELEBRATION_STAR_COUNT_MAX = 5
const CELEBRATION_STAR_POINTS = 5
const CELEBRATION_STAR_INNER_RADIUS = 12
const CELEBRATION_STAR_OUTER_RADIUS = 30
const CELEBRATION_STAR_SCALE_UP_DURATION = 300
const CELEBRATION_STAR_WAIT_DURATION = 1000
const CELEBRATION_STAR_FADE_DURATION = 500
const CELEBRATION_TOTAL_DURATION = 1500
const CELEBRATION_STAR_COLORS = [0xFFC107, 0x42A5F5, 0x66BB6A]
const HINT_DEPTH = 4
const CELEBRATION_DEPTH = 50
const STIMULUS_CARD_ALPHA = 0.15
const STIMULUS_CARD_CORNER_RATIO = 0.1
const STIMULUS_LABEL_RATIO = 0.4
const OPTION_LABEL_RATIO = 0.45
const KEEP_RECENT_ROUNDS = 3
const GUIDE_CHROM_COLOR = 0xFFFFFF
const GUIDE_CHROM_ALPHA_BASE = 0.2
const GUIDE_CHROM_ALPHA_MAX = 0.25
const GUIDE_CHROM_PULSE_DURATION = 2000
const GUIDE_CHROM_PADDING = 20
const GUIDE_CHROM_DEPTH = 1
const PATTERN_COLOR = 0x000000
const PATTERN_ALPHA = 0.4
const PATTERN_LINE_THICKNESS = 3
const PATTERN_DEPTH = 2
const PATTERN_TYPES = ['diagonal', 'dots', 'grid', 'waves', 'cross'] as const
type PatternType = typeof PATTERN_TYPES[number]

const BIOME_GRADIENTS: Record<string, [number, number]> = {
    meadow:    [0xc8e6c9, 0x81c784],
    farm:      [0xfff9c4, 0xffd54f],
    woods:     [0xa5d6a7, 0x388e3c],
    beach:     [0xb3e5fc, 0x4fc3f7],
    space:     [0x1a237e, 0x4a148c],
    prehistory:[0xd7ccc8, 0x8d6e63]
}

export class RecognitionGameScene extends Scene {
    websocket?: WebSocket
    activityId?: number
    biome?: string

    startingGame: boolean = true
    blockActions: boolean = false
    dateClick: number = Date.now()

    images: RoundObject[] = []
    private progressBar?: RoundProgressBar
    private nubiLayer?: MinigameNubiLayer
    private exitButton?: ExitButton
    private roundAudio = new RoundAudioCache()
    private assetLoader?: DynamicAssetLoader
    private colorizer = new RecognitionColorizer()
    private roundLoadToken = 0
    /** COLOR: item (`item_N`) elegido para la ronda, el mismo en todas las opciones y en el estimulo. */
    private selectedColorItem: string = ''
    private showIcon: boolean = true
    private pendingAudioListener?: { id: string, handler: (audioId: string) => void }
    private reducedMotion: boolean = false
    private exitInProgress: boolean = false
    private feedbackTweenGroup: Phaser.Tweens.Tween[] = []
    private selectedOptionId: string = ''
    private sessionId?: number
    private childId?: number
    private previousHintActive: boolean = false
    private hintBorderGraphics?: Phaser.GameObjects.Graphics
    private hintPulseTween?: Phaser.Tweens.Tween
    private layout!: ResponsiveLayout
    private sizes!: LayoutSizes
    private previousScaleMode?: Scale.ScaleModeType
    private hintTargetId: string = ''
    private fitActive = false
    private onScaleResize = () => this.relayout()
    // Paused = portrait: OrientationRequiredScene takes over and needs the normal (cover) scaling to stay legible.
    private onScenePause = () => {
        this.blockActions = true
        this.restoreScaling()
    }
    private onSceneResume = () => {
        this.blockActions = false
        this.enterFitScaling()
    }
    private stimulusCardGraphics?: Phaser.GameObjects.Graphics
    private _nonChromaticKeyRequired: boolean = false
    private _recognitionCategory: string = ''
    private nonChromaticPatternGraphics: Phaser.GameObjects.Graphics[] = []
    private guideChromGraphics?: Phaser.GameObjects.Graphics
    private guideChromPulseTween?: Phaser.Tweens.Tween
    private touchEnableTimer?: Phaser.Time.TimerEvent

    constructor() { super({ key: 'recognition-game', active: false }) }

    /** Tamaño táctil de las opciones (unidades lógicas), calculado por ResponsiveLayout. */
    get minElementHitSize(): number {
        return this.sizes?.hitSize ?? 0
    }

    get nonChromaticKeyRequired(): boolean {
        return this._nonChromaticKeyRequired
    }

    get recognitionCategory(): string {
        return this._recognitionCategory
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
        this.blockActions = false
        this.selectedOptionId = ''
        this.previousHintActive = false
        this.roundAudio = new RoundAudioCache(this.registry.get('audioCache') as AudioCache | undefined)
        this.assetLoader = new DynamicAssetLoader(this)
        this.roundLoadToken++

        let profile = this.registry.get(DEVICE_PROFILE_REGISTRY_KEY) as DeviceProfile | undefined
        if (!profile) {
            profile = detectDeviceProfile()
            this.registry.set(DEVICE_PROFILE_REGISTRY_KEY, profile)
        }
        this.layout = new ResponsiveLayout(profile)
        this.enterFitScaling()
        this.refreshSizes()
    }

    /**
     * Mientras dura el minijuego el canvas se ajusta con FIT (todo el contenido visible,
     * sin recortes en pantallas que no son 16:9). El resto de escenas siguen con su modo.
     */
    private enterFitScaling(): void {
        if (this.fitActive) return
        this.fitActive = true
        this.previousScaleMode = this.scale.scaleMode
        this.scale.scaleMode = Scale.FIT
        this.scale.displaySize.setAspectMode(Scale.FIT)
        this.scale.refresh()
    }

    private restoreScaling(): void {
        if (!this.fitActive || this.previousScaleMode === undefined) return
        this.fitActive = false
        this.scale.scaleMode = this.previousScaleMode
        this.scale.displaySize.setAspectMode(this.previousScaleMode)
        this.previousScaleMode = undefined
        this.scale.refresh()
    }

    private refreshSizes(): void {
        this.sizes = this.layout.calculateSizes({
            viewportWidth: this.scale.width,
            viewportHeight: this.scale.height,
            displayScale: this.scale.displayScale.x
        })
    }

    /** El tamaño del canvas en pantalla cambió: recalcula tamaños y recoloca todo. */
    private relayout(): void {
        if (!this.layout || this.exitInProgress) return

        this.refreshSizes()
        this.progressBar?.resize(this.sizes)
        this.exitButton?.resize(this.sizes)
        this.nubiLayer?.resize(this.sizes)
        this.layoutRound()
    }

    create() {
        this.reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
        this.exitInProgress = false
        this.previousHintActive = false

        this.createBiomeBackground()

        this.refreshSizes()
        this.progressBar = new RoundProgressBar(this, this.sizes)

        const npcEnabled = this.registry.get('npcEnabled') as boolean ?? false
        this.nubiLayer = new MinigameNubiLayer(this, this.sizes)
        this.nubiLayer.create(npcEnabled)

        this.exitButton = new ExitButton(this, this.sizes)
        this.scale.on('resize', this.onScaleResize)

        this.events.on('exit-button-double-tap', this.onExitDoubleTap)
        this.events.on('minigame-nubi-tap', this.onNubiTap)

        this.fadeFromBlack()

        this.events.on('pause', this.onScenePause)
        this.events.on('resume', this.onSceneResume)

        this.events.once('shutdown', () => this.cleanup())
    }

    preload() {
        if (this.websocket) {
            this.manageWs(this.websocket)
        }
    }

    private createBiomeBackground(): void {
        const normalizedBiome = (this.biome ?? 'meadow').toLowerCase()
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
            const color = (Math.round(r) << 16) | (Math.round(g) << 8) | Math.round(b)
            graphics.fillStyle(color, 1)
            graphics.fillRect(0, i * stepHeight, this.scale.width, stepHeight + 1)
        }
    }

    private onExitDoubleTap = () => this.sendAbandonAndExit()
    private onNubiTap = () => this.replayRoundAudio()

    private sendAbandonAndExit(): void {
        if (this.exitInProgress) return
        this.exitInProgress = true

        this.cancelPendingRoundAudio()
        this.getAudioService()?.stop()

        if (this.websocket && this.websocket.readyState === WebSocket.OPEN) {
            this.websocket.send(JSON.stringify(new GameAbandonEvent()))
        }

        this.fadeToBlackAndExit()
    }

    private fadeFromBlack(): void {
        const overlay = this.add.rectangle(
            this.scale.width / 2, this.scale.height / 2,
            this.scale.width, this.scale.height, 0x000000, 1
        )
        overlay.setDepth(100)
        overlay.setScrollFactor(0)

        const duration = this.reducedMotion ? 200 : FADE_DURATION
        this.tweens.add({
            targets: overlay,
            alpha: 0,
            duration,
            ease: 'Sine.easeOut',
            onComplete: () => overlay.destroy()
        })
    }

    private fadeToBlackAndExit(): void {
        console.log('[RecognitionGameScene] fadeToBlackAndExit called, WebSocket readyState:', this.websocket?.readyState)
        this.cleanupFeedbackTweens()

        const overlay = this.add.rectangle(
            this.scale.width / 2, this.scale.height / 2,
            this.scale.width, this.scale.height, 0x000000, 0
        )
        overlay.setDepth(100)
        overlay.setScrollFactor(0)

        const duration = this.reducedMotion ? 200 : FADE_DURATION
        this.tweens.add({
            targets: overlay,
            alpha: 1,
            duration,
            ease: 'Sine.easeIn',
            onComplete: () => {
                console.log('[RecognitionGameScene] Transitioning to world-map, WebSocket readyState:', this.websocket?.readyState)
                this.scene.start('world-map', {
                    websocket: this.websocket,
                    sessionId: this.sessionId,
                    childId: this.childId
                })
            }
        })
    }

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

    /**
     * Resolve the texture key for an element, generating color textures dynamically if needed
     */
    private resolveTextureKey(element: RecognitionElement, size: number = this.minElementHitSize): string | null {
        const category = this._recognitionCategory as RECOGNITION_TYPE

        if (category === 'COLOR') {
            // Profiles with a colour vision preference keep the accessible colour + shape resolved
            // server-side (SPRINT-075) as the splash; the rest use the block's splash image.
            const accessible = element.accessibleColor
            const generate = () => generateColorTexture(
                this, accessible!.value, (accessible!.shapeIcon || 'circle') as ColorShape, size)

            if (accessible && this._nonChromaticKeyRequired) return generate()

            const splashKey = DynamicAssetLoader.textureKey(element, category)
            if (splashKey && this.textures.exists(splashKey)) return splashKey
            return accessible ? generate() : splashKey
        }

        return DynamicAssetLoader.textureKey(element, category)
    }

    /** Key of the selected item texture in the colour block of `element`, if it is loaded. */
    private colorItemKey(element: RecognitionElement): string | null {
        const block = DynamicAssetLoader.colorBlock(element)
        if (!block || !this.selectedColorItem) return null
        const key = DynamicAssetLoader.colorItemKey(block, this.selectedColorItem)
        return this.textures.exists(key) ? key : null
    }

    private placeOption(
        option: Phaser.GameObjects.Image | Phaser.GameObjects.Rectangle | Phaser.GameObjects.Container,
        label: Phaser.GameObjects.Text | undefined,
        slot: OptionSlot
    ): void {
        option.setPosition(slot.x, slot.y)
        if (option instanceof GameObjects.Image) {
            option.setScale(slot.size / Math.max(option.width, option.height))
        } else if (option instanceof GameObjects.Container) {
            layoutSplashCompound(option, slot.size)
        } else {
            option.setSize(slot.size, slot.size)
            option.input?.hitArea?.setSize(slot.size, slot.size)
        }
        label?.setPosition(slot.x, slot.y).setFontSize(slot.size * OPTION_LABEL_RATIO)
    }

    renderElements(items: RecognitionElement[], targetElementId: string, colors?: number[]) {
        this.images = []
        this.cleanupStimulusCard()

        const targetElement = items.find(e => e.id === targetElementId)
        const optionElements = items
        if (targetElement) {
            this.renderTargetElement(targetElement, colors ? this.colorizer.pickStimulusColor(colors) : undefined)
        }

        const slots = this.layout.getOptionSlots(optionElements.length)
        optionElements.forEach((e, i) => {
            const slot = slots[i]
            const imageKey = this.resolveTextureKey(e, Math.round(slot.size))
            let optionElement: Phaser.GameObjects.Image | Phaser.GameObjects.Rectangle | Phaser.GameObjects.Container
            let labelElement: Phaser.GameObjects.Text | undefined

            if (imageKey && this.textures.exists(imageKey) && this._recognitionCategory === 'COLOR') {
                optionElement = createSplashCompound(this, imageKey, this.colorItemKey(e))
            } else if (imageKey && this.textures.exists(imageKey)) {
                const img = this.add.image(slot.x, slot.y, imageKey)

                if (colors && colors[i] !== undefined) {
                    this.colorizer.applyTint(img, colors[i])
                }

                optionElement = img
            } else {
                if (imageKey) {
                    console.warn('Texture not in cache, using placeholder for option:', imageKey)
                }
                const rect = this.add.rectangle(slot.x, slot.y, slot.size, slot.size, 0x90A4AE, 0.6)
                const label = this.add.text(slot.x, slot.y, e.displayValue, {
                    fontSize: `${slot.size * OPTION_LABEL_RATIO}px`,
                    color: '#ffffff',
                    fontStyle: 'bold'
                }).setOrigin(0.5, 0.5)
                optionElement = rect
                labelElement = label
            }

            optionElement.setData('elementId', e.id)
            optionElement.setData('optionIndex', i)
            if (labelElement) {
                labelElement.setData('elementId', e.id)
                labelElement.setData('optionIndex', i)
            }
            // Size first: a Container takes its hit area from its size.
            this.placeOption(optionElement, labelElement, slot)
            optionElement.setInteractive({ useHandCursor: false })

            optionElement.on('pointerdown', () => {
                if (this.startingGame) return
                if (this.blockActions) return
                this.blockActions = true
                this.selectedOptionId = e.id
                this.removeVisualHint()
                if (this.websocket) {
                    const ev = new GameRecognitionActionEvent()
                    const diff = Date.now() - this.dateClick
                    ev.setAction(e.id, diff)
                    this.websocket.send(JSON.stringify(ev))
                    this.dateClick = Date.now()
                }
            })

            this.images.push(optionElement)
            if (labelElement) {
                this.images.push(labelElement)
            }
        })
    }

    private renderTargetElement(element: RecognitionElement, tint?: number): void {
        const { x, y } = this.sizes.stimulusCenter
        this.drawStimulusCard()

        const imageKey = this.resolveTextureKey(element, Math.round(this.sizes.stimulusSize))
        let stimulus: Phaser.GameObjects.Image | Phaser.GameObjects.Text | Phaser.GameObjects.Container
        if (imageKey && this.textures.exists(imageKey) && this._recognitionCategory === 'COLOR') {
            // The target's item is the hint (EASY/MEDIUM): the option showing the same item is the right one.
            // In HARD (showIcon=false) the target is the bare splash.
            stimulus = createSplashCompound(this, imageKey, this.showIcon ? this.colorItemKey(element) : null)
        } else if (imageKey && this.textures.exists(imageKey)) {
            const img = this.add.image(x, y, imageKey)

            if (tint !== undefined) {
                this.colorizer.applyTint(img, tint)
            }
            stimulus = img
        } else {
            stimulus = this.add.text(x, y, element.displayValue, {
                fontSize: `${this.sizes.stimulusSize * STIMULUS_LABEL_RATIO}px`,
                color: '#ffffff',
                fontStyle: 'bold'
            }).setOrigin(0.5, 0.5)
        }

        stimulus.setData('elementId', element.id)
        stimulus.setData('isStimulus', true)
        this.images.push(stimulus)
        this.layoutStimulus()
    }

    private drawStimulusCard(): void {
        const { x, y } = this.sizes.stimulusCenter
        const size = this.sizes.stimulusSize

        const card = this.stimulusCardGraphics ?? this.add.graphics()
        card.clear()
        card.fillStyle(0xFFFFFF, STIMULUS_CARD_ALPHA)
        card.fillRoundedRect(x - size / 2, y - size / 2, size, size, size * STIMULUS_CARD_CORNER_RATIO)
        this.stimulusCardGraphics = card
    }

    private layoutStimulus(): void {
        const stimulus = this.images.find(o => o.getData('isStimulus'))
        if (!stimulus) return

        const { x, y } = this.sizes.stimulusCenter
        const size = this.sizes.stimulusSize
        this.drawStimulusCard()
        stimulus.setPosition(x, y)
        if (stimulus instanceof GameObjects.Image) {
            stimulus.setScale(size / Math.max(stimulus.width, stimulus.height))
        } else if (stimulus instanceof GameObjects.Container) {
            layoutSplashCompound(stimulus, size)
        } else if (stimulus instanceof GameObjects.Text) {
            stimulus.setFontSize(size * STIMULUS_LABEL_RATIO)
        }
    }

    /** Recoloca lo ya renderizado con los tamaños actuales y redibuja los overlays. */
    private layoutRound(): void {
        const options = this.getOptionImages()
        const bases = options.filter(
            (o): o is Phaser.GameObjects.Image | Phaser.GameObjects.Rectangle | Phaser.GameObjects.Container =>
                !(o instanceof GameObjects.Text)
        )
        const slots = this.layout.getOptionSlots(bases.length)

        bases.forEach((base, i) => {
            const index = base.getData('optionIndex') as number ?? i
            const label = options.find(
                (o): o is Phaser.GameObjects.Text => o instanceof GameObjects.Text && o.getData('optionIndex') === index
            )
            this.placeOption(base, label, slots[index] ?? slots[i])
        })
        this.layoutStimulus()

        if (this.hintBorderGraphics && this.hintTargetId) this.showVisualHint(this.hintTargetId)
        if (this.guideChromGraphics) this.renderGuideChrom()
        if (this.nonChromaticPatternGraphics.length > 0) this.renderNonChromaticPatterns(this.getOptionImages())
    }

    private cleanupStimulusCard(): void {
        if (this.stimulusCardGraphics) {
            this.stimulusCardGraphics.destroy()
            this.stimulusCardGraphics = undefined
        }
    }

    private applyTouchEnableDelay(delayMs: number, optionImages: RoundObject[]): void {
        if (this.touchEnableTimer) {
            this.touchEnableTimer.remove(false)
            this.touchEnableTimer = undefined
        }

        const isTappable = (obj: GameObjects.GameObject): obj is GameObjects.Image | GameObjects.Rectangle | GameObjects.Container =>
            !(obj instanceof GameObjects.Text)

        if (delayMs <= 0) {
            optionImages.forEach(img => {
                img.setAlpha(1.0)
                if (isTappable(img)) {
                    img.setInteractive({ useHandCursor: false })
                }
            })
            this.blockActions = false
            return
        }

        optionImages.forEach(img => {
            img.setAlpha(0.5)
            if (isTappable(img)) {
                img.disableInteractive()
            }
        })
        this.blockActions = true

        if (this.reducedMotion) {
            this.touchEnableTimer = this.time.delayedCall(delayMs, () => {
                optionImages.forEach(img => {
                    img.setAlpha(1.0)
                    if (isTappable(img)) {
                        img.setInteractive({ useHandCursor: false })
                    }
                })
                this.blockActions = false
                this.touchEnableTimer = undefined
            })
        } else {
            this.touchEnableTimer = this.time.delayedCall(delayMs, () => {
                const tappableImages = optionImages.filter(isTappable)
                this.tweens.add({
                    targets: optionImages,
                    alpha: 1.0,
                    duration: 200,
                    ease: 'Sine.easeOut',
                    onComplete: () => {
                        tappableImages.forEach(img => {
                            img.setInteractive({ useHandCursor: false })
                        })
                        this.blockActions = false
                        this.touchEnableTimer = undefined
                    }
                })
            })
        }
    }

    private renderGuideChrom(): void {
        this.destroyGuideChrom()

        const optionImages = this.getOptionImages()
        if (optionImages.length === 0) return

        let minX = Infinity, maxX = -Infinity, minY = Infinity, maxY = -Infinity
        optionImages.forEach(img => {
            const bounds = img.getBounds()
            if (bounds.x < minX) minX = bounds.x
            if (bounds.x + bounds.width > maxX) maxX = bounds.x + bounds.width
            if (bounds.y < minY) minY = bounds.y
            if (bounds.y + bounds.height > maxY) maxY = bounds.y + bounds.height
        })

        const cx = (minX + maxX) / 2
        const cy = (minY + maxY) / 2
        const rx = (maxX - minX) / 2 + GUIDE_CHROM_PADDING
        const ry = (maxY - minY) / 2 + GUIDE_CHROM_PADDING

        const graphics = this.add.graphics()
        graphics.setDepth(GUIDE_CHROM_DEPTH)
        graphics.fillStyle(GUIDE_CHROM_COLOR, GUIDE_CHROM_ALPHA_BASE)
        graphics.fillEllipse(cx, cy, rx * 2, ry * 2)

        this.guideChromGraphics = graphics

        if (!this.reducedMotion) {
            this.guideChromPulseTween = this.tweens.add({
                targets: graphics,
                alpha: GUIDE_CHROM_ALPHA_MAX,
                duration: GUIDE_CHROM_PULSE_DURATION / 2,
                ease: 'Sine.inOut',
                yoyo: true,
                repeat: -1
            })
        }
    }

    private destroyGuideChrom(): void {
        if (this.guideChromPulseTween) {
            this.guideChromPulseTween.stop()
            this.guideChromPulseTween = undefined
        }
        if (this.guideChromGraphics) {
            this.guideChromGraphics.destroy()
            this.guideChromGraphics = undefined
        }
    }

    private createPatternGraphics(patternType: PatternType, width: number, height: number): Phaser.GameObjects.Graphics {
        const graphics = this.add.graphics()
        graphics.lineStyle(PATTERN_LINE_THICKNESS, PATTERN_COLOR, PATTERN_ALPHA)

        switch (patternType) {
            case 'diagonal': {
                const lineSpacing = width / 4
                for (let i = 1; i <= 3; i++) {
                    const startX = i * lineSpacing - width / 4
                    const startY = 0
                    const endX = startX + height
                    const endY = height
                    graphics.moveTo(startX, startY)
                    graphics.lineTo(endX, endY)
                }
                break
            }
            case 'dots': {
                const radius = Math.min(width, height) * 0.06
                const offsetX = width * 0.3
                const offsetY = height * 0.3
                const centerX = width / 2
                const centerY = height / 2
                const dotPositions = [
                    { x: centerX - offsetX, y: centerY - offsetY },
                    { x: centerX + offsetX, y: centerY - offsetY },
                    { x: centerX - offsetX, y: centerY + offsetY },
                    { x: centerX + offsetX, y: centerY + offsetY }
                ]
                graphics.fillStyle(PATTERN_COLOR, PATTERN_ALPHA)
                dotPositions.forEach(pos => {
                    graphics.fillCircle(pos.x, pos.y, radius)
                })
                break
            }
            case 'grid': {
                const gridDivisions = 3
                for (let i = 1; i < gridDivisions; i++) {
                    const gx = (width / gridDivisions) * i
                    graphics.moveTo(gx, 0)
                    graphics.lineTo(gx, height)
                    const gy = (height / gridDivisions) * i
                    graphics.moveTo(0, gy)
                    graphics.lineTo(width, gy)
                }
                break
            }
            case 'waves': {
                const waveAmplitude = height * 0.1
                const waveCount = 2
                for (let w = 0; w < waveCount; w++) {
                    const baseY = height * (0.35 + w * 0.3)
                    const steps = 20
                    for (let i = 0; i <= steps; i++) {
                        const x = (i / steps) * width
                        const y = baseY + Math.sin((i / steps) * Math.PI * 2) * waveAmplitude
                        if (i === 0) {
                            graphics.moveTo(x, y)
                        } else {
                            graphics.lineTo(x, y)
                        }
                    }
                }
                break
            }
            case 'cross': {
                const midX = width / 2
                const midY = height / 2
                graphics.moveTo(midX, 0)
                graphics.lineTo(midX, height)
                graphics.moveTo(0, midY)
                graphics.lineTo(width, midY)
                break
            }
        }

        graphics.strokePath()
        return graphics
    }

    private renderNonChromaticPatterns(optionImages: RoundObject[]): void {
        this.destroyNonChromaticPatterns()

        if (!this._nonChromaticKeyRequired || this._recognitionCategory !== 'COLOR') return

        optionImages.forEach((img, index) => {
            const bounds = img.getBounds()
            const patternType = PATTERN_TYPES[index % PATTERN_TYPES.length]
            const graphics = this.createPatternGraphics(patternType, bounds.width, bounds.height)
            graphics.setPosition(bounds.x, bounds.y)
            graphics.setDepth(PATTERN_DEPTH)
            this.nonChromaticPatternGraphics.push(graphics)
        })
    }

    private destroyNonChromaticPatterns(): void {
        this.nonChromaticPatternGraphics.forEach(g => g.destroy())
        this.nonChromaticPatternGraphics = []
    }

    private getOptionImages(): RoundObject[] {
        return this.images.filter(img => !img.getData('isStimulus'))
    }

    /**
     * Carga solo las texturas de la ronda (las que falten) y renderiza cuando estan listas.
     * Si mientras tanto llega otra ronda o se cierra la escena, el resultado se descarta.
     */
    loadResources(type: RECOGNITION_TYPE | null, items: RecognitionElement[], targetElementId: string, onReady?: () => void) {
        if (!type) return
        if (!items || items.length <= 0) return
        if (!this.assetLoader) return

        const token = ++this.roundLoadToken
        // COLOR also picks the item shared by every option of the round; other categories have none.
        const loading = type === 'COLOR'
            ? this.assetLoader.loadColorRound(items)
            : this.assetLoader.loadRoundAssets(items, type).then(() => null)

        loading.then(colorItem => {
            if (token !== this.roundLoadToken) return

            this.selectedColorItem = colorItem ?? ''
            this.clearRoundVisuals()
            const colors = RecognitionColorizer.appliesTo(type) ? this.colorizer.assignColors(items.length) : undefined
            this.renderElements(items, targetElementId, colors)
            this.startingGame = false
            this.assetLoader?.cleanupOldTextures(KEEP_RECENT_ROUNDS)
            onReady?.()
        })
    }

    private clearRoundVisuals(): void {
        this.images.forEach(i => i.destroy(true))
        this.images = []
        this.cleanupStimulusCard()
        this.destroyGuideChrom()
        this.destroyNonChromaticPatterns()
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
                if (event.payload.engine === "RECOGNITION") {
                    this.websocket.send(JSON.stringify(new GameReadyEvent()))
                }
                break
            case 'GAME_ACTION_RESULT':
                if (event.payload.resultType && event.payload.updatedState) {
                    this.applyActionToResultType(
                        event.payload.resultType,
                        event.payload.gameCompleted,
                        event.payload.updatedState
                    )
                }
                break
            case 'GAME_READY':
                if (event.payload.engine === "RECOGNITION" && event.payload.recognitionState?.recognitionCategory) {
                    const rs = event.payload.recognitionState
                    this._nonChromaticKeyRequired = rs.nonChromaticKeyRequired ?? false
                    this._recognitionCategory = rs.recognitionCategory ?? ''
                    this.showIcon = rs.showIcon ?? true
                    if (this.progressBar && rs.totalRounds > 0) {
                        this.progressBar.updateProgress(rs.roundIndex, rs.totalRounds)
                    }
                    this.loadResources(rs.recognitionCategory ?? null, rs.elements, rs.targetElementId ?? '', () => {
                        this.nubiLayer?.showPhrase('welcome')
                        if (rs.hintActive) {
                            this.showVisualHint(rs.targetElementId)
                        }
                        if (rs.guideChromEnabled) {
                            this.renderGuideChrom()
                        }
                        const optionImages = this.getOptionImages()
                        this.renderNonChromaticPatterns(optionImages)
                        this.applyTouchEnableDelay(rs.touchEnableDelayMs ?? 0, optionImages)
                    })
                    if (rs.hintActive) {
                        this.nubiLayer?.showPhrase('hint')
                    }
                    this.previousHintActive = rs.hintActive
                }
                break
        }
    }

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

        // Queued so the success/error jingle of the previous answer is not cut off.
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

    applyActionToResultType(result: GAME_RESULT_TYPE, complete: boolean, state: RecognitionEnginePayload) {
        if (state.recognitionState) {
            this._nonChromaticKeyRequired = state.recognitionState.nonChromaticKeyRequired ?? false
            this._recognitionCategory = state.recognitionState.recognitionCategory ?? ''
            this.showIcon = state.recognitionState.showIcon ?? true
        }

        if (state.recognitionState && this.progressBar) {
            this.progressBar.updateProgress(
                state.recognitionState.roundIndex,
                state.recognitionState.totalRounds
            )
        }

        const currentHintActive = state.recognitionState?.hintActive ?? false
        if (!this.previousHintActive && currentHintActive) {
            this.nubiLayer?.showPhrase('hint')
            this.showVisualHint(state.recognitionState?.targetElementId ?? '')
        }
        this.previousHintActive = currentHintActive

        if (complete) {
            this.nubiLayer?.showPhrase('celebration')
        }

        const selectedImage = this.images.find(img => img.getData('elementId') === this.selectedOptionId && !img.getData('isStimulus'))

        if (complete) {
            this.removeVisualHint()
            this.playFeedbackAnimation(result, selectedImage, () => {
                this.playCelebration()
            })
            return
        }

        const nextState = state.recognitionState
        const nextCategory = nextState?.recognitionCategory
        const advancesRound = result === 'CORRECT' && !!nextState && !!nextCategory && nextState.elements.length > 0

        // The next round is already known: warm its textures while the feedback plays.
        if (advancesRound && nextState && nextCategory) {
            void this.assetLoader?.preloadNextRound(nextState.elements, nextCategory)
        }

        this.playFeedbackAnimation(result, selectedImage, () => {
            if (advancesRound && nextState && nextCategory) {
                // blockActions stays on until the new round is rendered (applyTouchEnableDelay releases it).
                this.loadResources(nextCategory, nextState.elements, nextState.targetElementId ?? '', () => {
                    if (currentHintActive && nextState.targetElementId) {
                        this.showVisualHint(nextState.targetElementId)
                    }
                    if (nextState.guideChromEnabled) {
                        this.renderGuideChrom()
                    }
                    const optionImages = this.getOptionImages()
                    this.renderNonChromaticPatterns(optionImages)
                    this.applyTouchEnableDelay(nextState.touchEnableDelayMs ?? 0, optionImages)
                })
                return
            }

            this.time.delayedCall(FEEDBACK_DELAY, () => {
                if (!this.touchEnableTimer) {
                    this.blockActions = false
                }
            })
        })
    }

    private playFeedbackAnimation(result: GAME_RESULT_TYPE, targetImage: RoundObject | undefined, onComplete: () => void): void {
        this.playFeedbackSound(result)

        if (result === 'CORRECT') {
            this.playCorrectAnimation(targetImage, onComplete)
        } else if (result === 'INCORRECT' || result === 'TIMEOUT') {
            this.playIncorrectAnimation(targetImage, onComplete)
        } else {
            onComplete()
        }
    }

    private playCorrectAnimation(targetImage: RoundObject | undefined, onComplete: () => void): void {
        const target = targetImage ?? this.images[0]
        if (!target) {
            onComplete()
            return
        }

        if (this.reducedMotion) {
            this.tweens.add({
                targets: target,
                alpha: 0.5,
                duration: 200,
                yoyo: true,
                onComplete
            })
            return
        }

        this.tweens.add({
            targets: target,
            scaleX: target.scaleX * FEEDBACK_SCALE_TO,
            scaleY: target.scaleY * FEEDBACK_SCALE_TO,
            duration: FEEDBACK_SCALE_DURATION / 2,
            ease: 'Cubic.out',
            yoyo: true,
            onComplete: () => {
                target.setScale(target.scaleX / FEEDBACK_SCALE_TO, target.scaleY / FEEDBACK_SCALE_TO)
            }
        })

        if ('setTint' in target) {
            (target as Phaser.GameObjects.Image).setTint(CORRECT_TINT)
        }
        target.setAlpha(1 - CORRECT_TINT_ALPHA)
        this.time.delayedCall(FEEDBACK_SCALE_DURATION, () => {
            const tint = this.colorizer.getTint(target)
            if (tint !== undefined) {
                (target as Phaser.GameObjects.Image).setTint(tint)
            } else if ('clearTint' in target) {
                (target as Phaser.GameObjects.Image).clearTint()
            }
            target.setAlpha(1)
        })

        const particle = this.add.circle(target.x, target.y, 10, CORRECT_TINT, 0.6)
        particle.setDepth(5)
        this.tweens.add({
            targets: particle,
            radius: 60,
            alpha: 0,
            scaleX: 4,
            scaleY: 4,
            duration: PARTICLE_DURATION,
            ease: 'Cubic.out',
            onComplete: () => particle.destroy()
        })

        this.time.delayedCall(Math.max(FEEDBACK_SCALE_DURATION, PARTICLE_DURATION), onComplete)
    }

    private playIncorrectAnimation(targetImage: RoundObject | undefined, onComplete: () => void): void {
        const target = targetImage ?? this.images[0]
        if (!target) {
            onComplete()
            return
        }

        const originalX = target.x

        if (this.reducedMotion) {
            this.tweens.add({
                targets: target,
                alpha: 0.5,
                duration: 200,
                yoyo: true,
                onComplete
            })
            return
        }

        const wobbleDuration = FEEDBACK_WOBBLE_DURATION

        this.tweens.add({
            targets: target,
            x: originalX + FEEDBACK_WOBBLE_PX,
            duration: wobbleDuration / 2,
            ease: 'Sine.inOut',
            yoyo: true,
            repeat: FEEDBACK_WOBBLE_CYCLES * 2 - 1,
            onComplete: () => {
                target.x = originalX
                onComplete()
            }
        })
    }

    private playFeedbackSound(result: GAME_RESULT_TYPE): void {
        const ttsEnabled = this.registry.get('ttsEnabled') as boolean ?? false
        if (!ttsEnabled) return

        const audioService = this.registry.get('audioService') as { playStatic?: (key: string) => void } | undefined
        if (!audioService?.playStatic) return

        if (result === 'CORRECT') {
            audioService.playStatic('success')
        } else if (result === 'INCORRECT' || result === 'TIMEOUT') {
            audioService.playStatic('error-soft')
        }
    }

    private showVisualHint(targetElementId: string): void {
        this.removeVisualHint()

        if (!targetElementId) return
        this.hintTargetId = targetElementId

        const targetImage = this.images.find(img => img.getData('elementId') === targetElementId && !img.getData('isStimulus'))
        if (!targetImage) return

        const graphics = this.add.graphics()
        graphics.setDepth(HINT_DEPTH)

        const bounds = targetImage.getBounds()
        const x = bounds.x - HINT_BORDER_PADDING
        const y = bounds.y - HINT_BORDER_PADDING
        const width = bounds.width + HINT_BORDER_PADDING * 2
        const height = bounds.height + HINT_BORDER_PADDING * 2

        graphics.lineStyle(HINT_BORDER_THICKNESS, HINT_BORDER_COLOR, 1)
        graphics.strokeRoundedRect(x, y, width, height, 8)

        this.hintBorderGraphics = graphics

        if (this.reducedMotion) {
            graphics.setAlpha(HINT_STATIC_ALPHA)
        } else {
            graphics.setAlpha(HINT_PULSE_ALPHA_MIN)
            this.hintPulseTween = this.tweens.add({
                targets: graphics,
                alpha: HINT_PULSE_ALPHA_MAX,
                duration: HINT_PULSE_DURATION / 2,
                ease: 'Sine.inOut',
                yoyo: true,
                repeat: -1
            })
        }
    }

    private removeVisualHint(): void {
        this.hintTargetId = ''
        if (this.hintPulseTween) {
            this.hintPulseTween.stop()
            this.hintPulseTween = undefined
        }
        if (this.hintBorderGraphics) {
            this.hintBorderGraphics.destroy()
            this.hintBorderGraphics = undefined
        }
    }

    private playCelebration(): void {
        this.playCelebrationSound()

        const starCount = CELEBRATION_STAR_COUNT_MIN + Math.floor(
            Math.random() * (CELEBRATION_STAR_COUNT_MAX - CELEBRATION_STAR_COUNT_MIN + 1)
        )

        const stars: Phaser.GameObjects.Star[] = []
        for (let i = 0; i < starCount; i++) {
            const x = this.scale.width * (0.2 + Math.random() * 0.6)
            const y = this.scale.height * (0.2 + Math.random() * 0.4)
            const color = CELEBRATION_STAR_COLORS[i % CELEBRATION_STAR_COLORS.length]

            const star = this.add.star(x, y, CELEBRATION_STAR_POINTS, CELEBRATION_STAR_INNER_RADIUS, CELEBRATION_STAR_OUTER_RADIUS, color)
            star.setDepth(CELEBRATION_DEPTH)
            star.setScrollFactor(0)
            stars.push(star)
        }

        if (this.reducedMotion) {
            stars.forEach(star => star.setAlpha(1))
            this.time.delayedCall(CELEBRATION_TOTAL_DURATION, () => {
                this.fadeToBlackAndExit()
            })
            return
        }

        stars.forEach(star => {
            star.setScale(0)
            this.tweens.add({
                targets: star,
                scaleX: 1,
                scaleY: 1,
                duration: CELEBRATION_STAR_SCALE_UP_DURATION,
                ease: 'Back.out',
                onComplete: () => {
                    this.time.delayedCall(CELEBRATION_STAR_WAIT_DURATION, () => {
                        this.tweens.add({
                            targets: star,
                            alpha: 0,
                            duration: CELEBRATION_STAR_FADE_DURATION,
                            ease: 'Sine.in'
                        })
                    })
                }
            })
        })

        this.time.delayedCall(CELEBRATION_TOTAL_DURATION, () => {
            this.fadeToBlackAndExit()
        })
    }

    // TECH-DEBT: playCelebrationSound gates by audioGeneralEnabled && ttsEnabled,
    // while playFeedbackSound (SPRINT-070) only checks ttsEnabled.
    // This inconsistency follows SPRINT-072 spec; align in a future sprint.
    private playCelebrationSound(): void {
        const audioGeneralEnabled = this.registry.get('audioGeneralEnabled') as boolean ?? false
        const ttsEnabled = this.registry.get('ttsEnabled') as boolean ?? false
        if (!audioGeneralEnabled || !ttsEnabled) return

        const audioService = this.registry.get('audioService') as { playStatic?: (key: string) => void } | undefined
        if (!audioService?.playStatic) return

        audioService.playStatic('celebration')
    }

    private cleanupFeedbackTweens(): void {
        this.feedbackTweenGroup.forEach(t => t.stop())
        this.feedbackTweenGroup = []
    }

    private cleanup(): void {
        console.log('[RecognitionGameScene] Cleanup called, WebSocket readyState:', this.websocket?.readyState)
        
        this.scale.off('resize', this.onScaleResize)
        this.events.off('pause', this.onScenePause)
        this.events.off('resume', this.onSceneResume)
        this.restoreScaling()
        this.events.off('exit-button-double-tap', this.onExitDoubleTap)
        this.events.off('minigame-nubi-tap', this.onNubiTap)
        this.cancelPendingRoundAudio()
        this.roundAudio.clear()
        this.exitButton?.destroy()
        this.exitButton = undefined
        this.progressBar?.destroy()
        this.progressBar = undefined
        this.nubiLayer?.destroy()
        this.nubiLayer = undefined
        this.roundLoadToken++
        this.images.forEach(i => {
            if (i instanceof GameObjects.Image) this.colorizer.clearTint(i)
            i.destroy(true)
        })
        this.images = []
        this.cleanupStimulusCard()
        this.assetLoader?.cleanupOldTextures(0)
        this.assetLoader = undefined
        this.cleanupFeedbackTweens()
        this.removeVisualHint()
        this.destroyGuideChrom()
        this.destroyNonChromaticPatterns()
        if (this.touchEnableTimer) {
            this.touchEnableTimer.remove(false)
            this.touchEnableTimer = undefined
        }
        
        // Clean up WebSocket handlers but don't close the connection
        if (this.websocket) {
            this.websocket.onmessage = null
            this.websocket.onclose = null
            this.websocket.onerror = null
        }
    }
}
