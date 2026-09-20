import { Scene, GameObjects } from "phaser"
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
import { MinigameNubiLayer } from "./worldmap/layers/MinigameNubiLayer"
import {
    generateColorTexture,
    type ColorShape
} from "../utils/colorTextureGenerator"

const VIEWPORT_WIDTH = 1280
const VIEWPORT_HEIGHT = 720
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
const STIMULUS_CARD_WIDTH = 120
const STIMULUS_CARD_HEIGHT = 120
const STIMULUS_CARD_ALPHA = 0.15
const STIMULUS_CARD_CORNER_RADIUS = 12
const STIMULUS_ZONE_Y = 0.25
const STIMULUS_ZONE_X = 0.5
const OPTIONS_ZONE_Y = 0.65
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

    images: (Phaser.GameObjects.Image | Phaser.GameObjects.Rectangle | Phaser.GameObjects.Text)[] = []
    private progressBar?: RoundProgressBar
    private nubiLayer?: MinigameNubiLayer
    private reducedMotion: boolean = false
    private exitInProgress: boolean = false
    private feedbackTweenGroup: Phaser.Tweens.Tween[] = []
    private selectedOptionId: string = ''
    private sessionId?: number
    private childId?: number
    private previousHintActive: boolean = false
    private hintBorderGraphics?: Phaser.GameObjects.Graphics
    private hintPulseTween?: Phaser.Tweens.Tween
    private minElementHitSize = 80
    private stimulusCardGraphics?: Phaser.GameObjects.Graphics
    private _nonChromaticKeyRequired: boolean = false
    private _recognitionCategory: string = ''
    private nonChromaticPatternGraphics: Phaser.GameObjects.Graphics[] = []
    private guideChromGraphics?: Phaser.GameObjects.Graphics
    private guideChromPulseTween?: Phaser.Tweens.Tween
    private touchEnableTimer?: Phaser.Time.TimerEvent

    constructor() { super({ key: 'recognition-game', active: false }) }

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
    }

    create() {
        this.reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
        this.exitInProgress = false
        this.previousHintActive = false

        this.createBiomeBackground()

        this.progressBar = new RoundProgressBar(this)

        const npcEnabled = this.registry.get('npcEnabled') as boolean ?? false
        this.nubiLayer = new MinigameNubiLayer(this)
        this.nubiLayer.create(npcEnabled)

        this.events.on('minigame-nubi-double-tap', () => this.sendAbandonAndExit())

        this.fadeFromBlack()

        this.events.on('pause', () => {
            this.blockActions = true
        })

        this.events.on('resume', () => {
            this.blockActions = false
        })

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
        const stepHeight = VIEWPORT_HEIGHT / steps
        for (let i = 0; i < steps; i++) {
            const ratio = i / (steps - 1)
            const r = ((colorTop >> 16) & 0xff) * (1 - ratio) + ((colorBottom >> 16) & 0xff) * ratio
            const g = ((colorTop >> 8) & 0xff) * (1 - ratio) + ((colorBottom >> 8) & 0xff) * ratio
            const b = (colorTop & 0xff) * (1 - ratio) + (colorBottom & 0xff) * ratio
            const color = (Math.round(r) << 16) | (Math.round(g) << 8) | Math.round(b)
            graphics.fillStyle(color, 1)
            graphics.fillRect(0, i * stepHeight, VIEWPORT_WIDTH, stepHeight + 1)
        }
    }

    private sendAbandonAndExit(): void {
        if (this.exitInProgress) return
        this.exitInProgress = true

        if (this.websocket && this.websocket.readyState === WebSocket.OPEN) {
            this.websocket.send(JSON.stringify(new GameAbandonEvent()))
        }

        this.fadeToBlackAndExit()
    }

    private fadeFromBlack(): void {
        const overlay = this.add.rectangle(
            VIEWPORT_WIDTH / 2, VIEWPORT_HEIGHT / 2,
            VIEWPORT_WIDTH, VIEWPORT_HEIGHT, 0x000000, 1
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
            VIEWPORT_WIDTH / 2, VIEWPORT_HEIGHT / 2,
            VIEWPORT_WIDTH, VIEWPORT_HEIGHT, 0x000000, 0
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
            this.readEvent(JSON.parse(msg.data))
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
        // COLOR elements render from the accessible color already resolved server-side
        // (accessible_color / accessible_color_palette) — never derived client-side.
        if (this._recognitionCategory === 'COLOR' && element.accessibleColor) {
            const shape = (element.accessibleColor.shapeIcon || 'circle') as ColorShape
            return generateColorTexture(this, element.accessibleColor.value, shape, size)
        }

        const imageRef = element.resourceRefs?.['image']
        if (!imageRef) return null

        return imageRef
    }

    renderElements(items: RecognitionElement[], targetElementId: string) {
        this.images = []
        this.cleanupStimulusCard()

        const targetElement = items.find(e => e.id === targetElementId)
        const optionElements = items
        console.log(items)
        if (targetElement) {
            this.renderTargetElement(targetElement)
        }

        const count = optionElements.length
        const spacing = count > 0 ? Math.max(this.minElementHitSize, VIEWPORT_WIDTH / (count + 1)) : 0
        optionElements.forEach((e, i) => {
            const x = spacing * (i + 1)
            const y = VIEWPORT_HEIGHT * OPTIONS_ZONE_Y
            const imageKey = this.resolveTextureKey(e)
            let optionElement: Phaser.GameObjects.Image | Phaser.GameObjects.Rectangle
            let labelElement: Phaser.GameObjects.Text | undefined

            if (imageKey && this.textures.exists(imageKey)) {
                const img = this.add.image(x, y, imageKey)

                if (img.displayWidth < this.minElementHitSize || img.displayHeight < this.minElementHitSize) {
                    const scale = this.minElementHitSize / Math.max(img.displayWidth, img.displayHeight)
                    img.setScale(scale, scale)
                }

                optionElement = img
            } else {
                if (imageKey) {
                    console.warn('Texture not in cache, using placeholder for option:', imageKey)
                }
                const rect = this.add.rectangle(x, y, this.minElementHitSize, this.minElementHitSize, 0x90A4AE, 0.6)
                const label = this.add.text(x, y, e.displayValue, {
                    fontSize: '36px',
                    color: '#ffffff',
                    fontStyle: 'bold'
                }).setOrigin(0.5, 0.5)
                optionElement = rect
                labelElement = label
            }

            optionElement.setInteractive({ useHandCursor: false })
            optionElement.setData('elementId', e.id)
            if (labelElement) {
                labelElement.setData('elementId', e.id)
            }

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

    private renderTargetElement(element: RecognitionElement): void {
        const x = VIEWPORT_WIDTH * STIMULUS_ZONE_X
        const y = VIEWPORT_HEIGHT * STIMULUS_ZONE_Y

        const cardGraphics = this.add.graphics()
        cardGraphics.fillStyle(0xFFFFFF, STIMULUS_CARD_ALPHA)
        cardGraphics.fillRoundedRect(
            x - STIMULUS_CARD_WIDTH / 2,
            y - STIMULUS_CARD_HEIGHT / 2,
            STIMULUS_CARD_WIDTH,
            STIMULUS_CARD_HEIGHT,
            STIMULUS_CARD_CORNER_RADIUS
        )
        this.stimulusCardGraphics = cardGraphics

        const imageKey = this.resolveTextureKey(element, STIMULUS_CARD_WIDTH)
        if (imageKey && this.textures.exists(imageKey)) {
            const img = this.add.image(x, y, imageKey)

            if (img.displayWidth < STIMULUS_CARD_WIDTH || img.displayHeight < STIMULUS_CARD_HEIGHT) {
                const scale = Math.min(STIMULUS_CARD_WIDTH, STIMULUS_CARD_HEIGHT) / Math.max(img.displayWidth, img.displayHeight)
                img.setScale(scale, scale)
            }

            img.setData('elementId', element.id)
            img.setData('isStimulus', true)
            this.images.push(img)
        } else {
            const placeholder = this.add.text(x, y, element.displayValue, {
                fontSize: '48px',
                color: '#ffffff',
                fontStyle: 'bold'
            }).setOrigin(0.5, 0.5)
            placeholder.setData('elementId', element.id)
            placeholder.setData('isStimulus', true)
            this.images.push(placeholder)
        }
    }

    private cleanupStimulusCard(): void {
        if (this.stimulusCardGraphics) {
            this.stimulusCardGraphics.destroy()
            this.stimulusCardGraphics = undefined
        }
    }

    private applyTouchEnableDelay(delayMs: number, optionImages: (Phaser.GameObjects.Image | Phaser.GameObjects.Rectangle | Phaser.GameObjects.Text)[]): void {
        if (this.touchEnableTimer) {
            this.touchEnableTimer.remove(false)
            this.touchEnableTimer = undefined
        }

        const isTappable = (obj: GameObjects.GameObject): obj is GameObjects.Image | GameObjects.Rectangle =>
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

    private renderNonChromaticPatterns(optionImages: (Phaser.GameObjects.Image | Phaser.GameObjects.Rectangle | Phaser.GameObjects.Text)[]): void {
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

    private getOptionImages(): (Phaser.GameObjects.Image | Phaser.GameObjects.Rectangle | Phaser.GameObjects.Text)[] {
        return this.images.filter(img => !img.getData('isStimulus'))
    }

    loadResources(type: RECOGNITION_TYPE | null, items: RecognitionElement[], targetElementId: string, onReady?: () => void) {
        if (!type) return
        if (!items || items.length <= 0) return

        const allCached = items.every((e) => !e.resourceRefs?.['image'] || this.textures.exists(e.resourceRefs['image']))
        if (allCached) {
            this.renderElements(items, targetElementId)
            onReady?.()
            return
        }

        this.load.setBaseURL('/')
        const packKeyMap: Record<RECOGNITION_TYPE, string> = {
            'LETTER': 'recognition-letters',
            'NUMBER': 'recognition-numbers',
            'SHAPE': 'recognition-shapes',
            'COLOR': 'recognition-colors',
            'ANIMAL': 'recognition-animals'
        }

        const packKey = packKeyMap[type]
        if (!packKey) {
            this.renderPlaceholderElements(items, targetElementId)
            this.startingGame = false
            onReady?.()
            return
        }

        // COLOR category generates textures dynamically, no need to load assets
        if (type === 'COLOR') {
            this.renderElements(items, targetElementId)
            this.startingGame = false
            onReady?.()
            return
        }

        let packFailed = false
        this.load.pack(`packManifest_${packKey}`, 'assets-manifest.json', packKey)
        this.load.on('loaderror', (_file: Phaser.Loader.File) => {
            packFailed = true
            console.warn('Asset pack not available, using placeholders for:', type)
            this.renderPlaceholderElements(items, targetElementId)
            this.startingGame = false
            onReady?.()
        })
        this.load.once('complete', () => {
            if (!packFailed) {
                this.renderElements(items, targetElementId)
                this.startingGame = false
                onReady?.()
            }
        })
        this.load.start()
    }

    private renderPlaceholderElements(items: RecognitionElement[], targetElementId: string): void {
        this.images = []
        this.cleanupStimulusCard()

        const targetElement = items.find(e => e.id === targetElementId)
        const optionElements = items

        if (targetElement) {
            this.renderTargetElement(targetElement)
        }

        const count = optionElements.length
        const spacing = count > 0 ? Math.max(this.minElementHitSize, VIEWPORT_WIDTH / (count + 1)) : 0

        optionElements.forEach((e, i) => {
            const x = spacing * (i + 1)
            const y = VIEWPORT_HEIGHT * OPTIONS_ZONE_Y

            const rect = this.add.rectangle(x, y, this.minElementHitSize, this.minElementHitSize, 0x90A4AE, 0.6)
                .setInteractive({ useHandCursor: false })
            const label = this.add.text(x, y, e.displayValue, {
                fontSize: '36px',
                color: '#ffffff',
                fontStyle: 'bold'
            }).setOrigin(0.5, 0.5)

            rect.setData('elementId', e.id)
            label.setData('elementId', e.id)

            rect.on('pointerdown', () => {
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

            this.images.push(rect)
            this.images.push(label)
        })
    }

    readEvent(event: ServerGameEvent | AvatarEvent) {
        if (!this.websocket) return
        if (!event) return
        if (event.event === 'GAME_AVATAR_EVENT') {
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

    applyActionToResultType(result: GAME_RESULT_TYPE, complete: boolean, state: RecognitionEnginePayload) {
        if (state.recognitionState) {
            this._nonChromaticKeyRequired = state.recognitionState.nonChromaticKeyRequired ?? false
            this._recognitionCategory = state.recognitionState.recognitionCategory ?? ''
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

        this.playFeedbackAnimation(result, selectedImage, () => {
            switch (result) {
                case 'CORRECT':
                    if (state.recognitionState && state.recognitionState.recognitionCategory) {
                        this.images.forEach((i) => { i.destroy(true) })
                        this.images = []
                        this.cleanupStimulusCard()
                        this.destroyGuideChrom()
                        this.destroyNonChromaticPatterns()
                        this.renderElements(state.recognitionState.elements, state.recognitionState.targetElementId ?? '')
                        if (currentHintActive && state.recognitionState.targetElementId) {
                            this.showVisualHint(state.recognitionState.targetElementId)
                        }
                        if (state.recognitionState.guideChromEnabled) {
                            this.renderGuideChrom()
                        }
                        const optionImages = this.getOptionImages()
                        this.renderNonChromaticPatterns(optionImages)
                        this.applyTouchEnableDelay(state.recognitionState.touchEnableDelayMs ?? 0, optionImages)
                    }
                    break
                case 'INCORRECT':
                    break
                case 'TIMEOUT':
                    break
            }

            this.time.delayedCall(FEEDBACK_DELAY, () => {
                if (!this.touchEnableTimer) {
                    this.blockActions = false
                }
            })
        })
    }

    private playFeedbackAnimation(result: GAME_RESULT_TYPE, targetImage: (Phaser.GameObjects.Image | Phaser.GameObjects.Rectangle | Phaser.GameObjects.Text) | undefined, onComplete: () => void): void {
        this.playFeedbackSound(result)

        if (result === 'CORRECT') {
            this.playCorrectAnimation(targetImage, onComplete)
        } else if (result === 'INCORRECT' || result === 'TIMEOUT') {
            this.playIncorrectAnimation(targetImage, onComplete)
        } else {
            onComplete()
        }
    }

    private playCorrectAnimation(targetImage: (Phaser.GameObjects.Image | Phaser.GameObjects.Rectangle | Phaser.GameObjects.Text) | undefined, onComplete: () => void): void {
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
            if ('clearTint' in target) {
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

    private playIncorrectAnimation(targetImage: (Phaser.GameObjects.Image | Phaser.GameObjects.Rectangle | Phaser.GameObjects.Text) | undefined, onComplete: () => void): void {
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
            const x = VIEWPORT_WIDTH * (0.2 + Math.random() * 0.6)
            const y = VIEWPORT_HEIGHT * (0.2 + Math.random() * 0.4)
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
        
        this.progressBar?.destroy()
        this.progressBar = undefined
        this.nubiLayer?.destroy()
        this.nubiLayer = undefined
        this.images.forEach(i => i.destroy(true))
        this.images = []
        this.cleanupStimulusCard()
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
