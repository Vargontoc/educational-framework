import { Scene } from "phaser"
import type { LayoutSizes } from "../../utils/ResponsiveLayout"

const BUBBLE_FADE_DURATION = 200
const BUBBLE_VISIBLE_DURATION = 3000
const BUBBLE_BG_COLOR = 0xFFFFFF
const BUBBLE_BORDER_COLOR = 0xCCCCCC
const BUBBLE_BORDER_RADIUS = 12
const BUBBLE_POINTER_SIZE = 10
const BUBBLE_PADDING = 12
const BUBBLE_FONT_SIZE = 16
const BUBBLE_MAX_WIDTH = 200
const BUBBLE_TEXT_COLOR = '#333333'
const NUBI_DEPTH = 20
const BUBBLE_DEPTH = 21
const SLEEPING_TINT = 0x8888CC
const ZZZ_FONT_SIZE = 20
const WAVE_COUNT = 3
const WAVE_DURATION = 350
const WAVE_STAGGER = 75
const WAVE_END_SCALE = 1.8
const WAVE_COLOR = 0xFFFFFF
const WAVE_ALPHA = 0.6
const WAVE_LINE_WIDTH = 4
const REDUCED_MOTION_WAVE_DURATION = 200

type PhraseMoment = 'welcome' | 'hint' | 'celebration'

const PHRASE_POOLS: Record<PhraseMoment, string[]> = {
    welcome: [
        '¡Vamos a jugar!',
        '¿Listo?',
        '¡A ver qué encuentras!',
        '¡Empezamos!'
    ],
    hint: [
        'Mira bien las opciones',
        'Fíjate en la forma',
        'Tómate tu tiempo',
        'Observa con atención'
    ],
    celebration: [
        '¡Lo lograste!',
        '¡Muy bien!',
        '¡Buen trabajo!',
        '¡Fantástico!'
    ]
}

export class MinigameNubiLayer {
    private scene: Scene
    private container: Phaser.GameObjects.Container
    private nubiSprite?: Phaser.GameObjects.Sprite
    private bubbleContainer?: Phaser.GameObjects.Container
    private npcEnabled = false
    private reducedMotion = false
    private usedPhrases = new Set<string>()
    private bubbleVisible = false
    private bubbleTimer?: Phaser.Time.TimerEvent
    private waveTweens: Phaser.Tweens.Tween[] = []
    private sizes: LayoutSizes
    private onNpcStateChanged = (enabled: boolean) => this.setNpcEnabled(enabled)

    constructor(scene: Scene, sizes: LayoutSizes) {
        this.scene = scene
        this.sizes = sizes
        this.container = scene.add.container(0, 0)
        this.container.setDepth(NUBI_DEPTH)
        this.container.setScrollFactor(0)
    }

    private get nubiSize(): number {
        return this.sizes.nubiSize
    }

    /** Escala del texto del globo: nunca por debajo del tamaño base ni de la fuente minima legible. */
    private get textScale(): number {
        return Math.max(1, this.sizes.minFontSize / BUBBLE_FONT_SIZE)
    }

    create(npcEnabled: boolean): void {
        this.npcEnabled = npcEnabled
        this.reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
        this.usedPhrases.clear()

        if (this.scene.textures.exists('nubi-greetings')) {
            this.buildNubiSprite()
            this.applyState()
        } else {
            this.scene.load.spritesheet('nubi-greetings', '/assets/animations/nubi/greetings.png', {
                frameWidth: 400,
                frameHeight: 400
            })
            this.scene.load.once('complete', () => {
                this.buildNubiSprite()
                this.applyState()
            })
            this.scene.load.start()
        }

        this.scene.registry.events.on('npc-state-changed', this.onNpcStateChanged)
        this.scene.events.once('shutdown', () => this.destroy())
        this.scene.events.once('destroy', () => this.destroy())
    }

    private buildNubiSprite(): void {
        if (this.nubiSprite) return

        this.nubiSprite = this.scene.add.sprite(0, 0, 'nubi-greetings')

        if (!this.scene.anims.exists('minigame-nubi-idle')) {
            this.scene.anims.create({
                key: 'minigame-nubi-idle',
                frames: this.scene.anims.generateFrameNumbers('nubi-greetings', { start: 0, end: 29 }),
                frameRate: 15,
                repeat: -1
            })
        }

        this.nubiSprite.setInteractive({ useHandCursor: false })
        this.nubiSprite.on('pointerdown', () => this.handleTap())

        this.container.add([this.nubiSprite])
        this.applyLayout()
    }

    /** Tamaño y posicion de Nubi (esquina inferior derecha) segun los tamaños actuales. */
    private applyLayout(): void {
        const size = this.nubiSize
        this.container.setPosition(
            this.sizes.viewportWidth - this.sizes.nubiMargin - size / 2,
            this.sizes.viewportHeight - this.sizes.nubiMargin - size / 2
        )

        if (!this.nubiSprite) return
        this.nubiSprite.setScale(size / Math.max(this.nubiSprite.width, this.nubiSprite.height))

        const zzz = this.container.getByName('zzz-text') as Phaser.GameObjects.Text | null
        if (zzz) {
            zzz.setFontSize(ZZZ_FONT_SIZE * this.textScale)
            zzz.setPosition(this.nubiSprite.displayWidth / 2 + 5, -this.nubiSprite.displayHeight / 2 - 5)
        }
    }

    resize(sizes: LayoutSizes): void {
        this.sizes = sizes
        this.hideBubble()
        this.applyLayout()
    }

    private handleTap(): void {
        if (!this.npcEnabled) return
        this.scene.events.emit('minigame-nubi-tap')
    }

    playSoundWave(): void {
        if (!this.nubiSprite) return

        const count = this.reducedMotion ? 1 : WAVE_COUNT
        for (let i = 0; i < count; i++) {
            const wave = this.scene.add.circle(0, 0, this.nubiSize / 2)
            wave.setStrokeStyle(WAVE_LINE_WIDTH * this.textScale, WAVE_COLOR, WAVE_ALPHA)
            wave.setFillStyle(WAVE_COLOR, 0)
            this.container.add(wave)

            const tween = this.scene.tweens.add({
                targets: wave,
                scaleX: this.reducedMotion ? 1 : WAVE_END_SCALE,
                scaleY: this.reducedMotion ? 1 : WAVE_END_SCALE,
                alpha: 0,
                delay: i * WAVE_STAGGER,
                duration: this.reducedMotion ? REDUCED_MOTION_WAVE_DURATION : WAVE_DURATION,
                ease: 'Sine.easeOut',
                onComplete: () => {
                    this.waveTweens = this.waveTweens.filter(t => t !== tween)
                    wave.destroy()
                }
            })
            this.waveTweens.push(tween)
        }
    }

    private applyState(): void {
        if (!this.nubiSprite) return

        if (this.npcEnabled) {
            this.nubiSprite.clearTint()
            if (!this.reducedMotion) {
                this.nubiSprite.play('minigame-nubi-idle')
            } else {
                this.nubiSprite.stop()
                this.nubiSprite.setFrame(0)
            }
        } else {
            this.nubiSprite.stop()
            this.nubiSprite.setFrame(0)
            this.nubiSprite.setTint(SLEEPING_TINT)
            this.showZzz()
        }
    }

    private showZzz(): void {
        if (!this.nubiSprite) return

        const existing = this.container.getByName('zzz-text')
        if (existing) return

        const zzz = this.scene.add.text(
            this.nubiSprite.displayWidth / 2 + 5,
            -this.nubiSprite.displayHeight / 2 - 5,
            'Z Z Z',
            {
                fontSize: `${ZZZ_FONT_SIZE * this.textScale}px`,
                color: '#6666AA',
                fontStyle: 'bold'
            }
        )
        zzz.setName('zzz-text')
        zzz.setOrigin(0, 1)
        this.container.add(zzz)
    }

    private removeZzz(): void {
        const existing = this.container.getByName('zzz-text') as Phaser.GameObjects.Text | undefined
        if (existing) existing.destroy()
    }

    setNpcEnabled(enabled: boolean): void {
        this.npcEnabled = enabled
        if (enabled) {
            this.removeZzz()
        }
        this.applyState()
    }

    showPhrase(moment: PhraseMoment): void {
        if (!this.npcEnabled) return

        const phrase = this.getRandomPhrase(moment)
        if (!phrase) return

        this.displayBubble(phrase)
    }

    private getRandomPhrase(moment: PhraseMoment): string | null {
        const pool = PHRASE_POOLS[moment]
        const available = pool.filter(p => !this.usedPhrases.has(p))

        if (available.length === 0) return null

        const chosen = available[Math.floor(Math.random() * available.length)]
        this.usedPhrases.add(chosen)
        return chosen
    }

    private displayBubble(text: string): void {
        this.hideBubble()

        const textObj = this.scene.add.text(0, 0, text, {
            fontSize: `${BUBBLE_FONT_SIZE}px`,
            color: BUBBLE_TEXT_COLOR,
            wordWrap: { width: BUBBLE_MAX_WIDTH - BUBBLE_PADDING * 2, useAdvancedWrap: true },
            align: 'center'
        })
        textObj.setOrigin(0.5, 0.5)

        const textBounds = textObj.getBounds()
        const bgWidth = textBounds.width + BUBBLE_PADDING * 2
        const bgHeight = textBounds.height + BUBBLE_PADDING * 2

        const bg = this.scene.add.graphics()
        bg.fillStyle(BUBBLE_BG_COLOR, 1)
        bg.lineStyle(2, BUBBLE_BORDER_COLOR, 1)
        bg.fillRoundedRect(-bgWidth / 2, -bgHeight / 2, bgWidth, bgHeight, BUBBLE_BORDER_RADIUS)
        bg.strokeRoundedRect(-bgWidth / 2, -bgHeight / 2, bgWidth, bgHeight, BUBBLE_BORDER_RADIUS)

        bg.fillTriangle(
            -BUBBLE_POINTER_SIZE, bgHeight / 2,
            BUBBLE_POINTER_SIZE, bgHeight / 2,
            0, bgHeight / 2 + BUBBLE_POINTER_SIZE
        )
        bg.lineStyle(2, BUBBLE_BORDER_COLOR, 1)
        bg.beginPath()
        bg.moveTo(-BUBBLE_POINTER_SIZE, bgHeight / 2)
        bg.lineTo(0, bgHeight / 2 + BUBBLE_POINTER_SIZE)
        bg.lineTo(BUBBLE_POINTER_SIZE, bgHeight / 2)
        bg.strokePath()

        const bubble = this.scene.add.container(0, 0)
        bubble.add([bg, textObj])
        bubble.setDepth(BUBBLE_DEPTH)

        // Se dibuja a tamaño base y se escala entero para que el texto sea legible en pantallas pequeñas.
        const scale = this.textScale
        bubble.setScale(scale)

        const nubiWorldX = this.container.x
        const nubiWorldY = this.container.y
        const scaledHalfHeight = (bgHeight / 2) * scale
        const bubbleY = nubiWorldY - this.nubiSize / 2 - scaledHalfHeight - (BUBBLE_POINTER_SIZE + 5) * scale
        const clampedBubbleX = Math.min(nubiWorldX, this.scene.scale.width - (bgWidth / 2) * scale - 10)
        bubble.setPosition(clampedBubbleX, bubbleY)

        this.bubbleContainer = bubble
        this.bubbleVisible = true

        if (this.reducedMotion) {
            bubble.setAlpha(1)
        } else {
            bubble.setAlpha(0)
            this.scene.tweens.add({
                targets: bubble,
                alpha: 1,
                duration: BUBBLE_FADE_DURATION,
                ease: 'Sine.easeOut'
            })
        }

        this.bubbleTimer = this.scene.time.delayedCall(BUBBLE_VISIBLE_DURATION, () => {
            this.hideBubble()
        })
    }

    private hideBubble(): void {
        if (this.bubbleTimer) {
            this.bubbleTimer.remove(false)
            this.bubbleTimer = undefined
        }

        if (!this.bubbleContainer || !this.bubbleVisible) return
        this.bubbleVisible = false

        const target = this.bubbleContainer
        this.bubbleContainer = undefined

        if (this.reducedMotion) {
            target.destroy()
            return
        }

        this.scene.tweens.add({
            targets: target,
            alpha: 0,
            duration: BUBBLE_FADE_DURATION,
            ease: 'Sine.easeIn',
            onComplete: () => target.destroy()
        })
    }

    destroy(): void {
        this.scene.registry.events.off('npc-state-changed', this.onNpcStateChanged)
        this.hideBubble()
        this.waveTweens.forEach(t => t.stop())
        this.waveTweens = []
        this.container.destroy()
        this.usedPhrases.clear()
    }
}
