import { Scene } from "phaser"
import { biomeYOffset } from "../config/worldMapConfig"

const PORTAL_DEPTH = 10
export const PORTAL_SIZE = 128
const PORTAL_ROTATION_SPEED = 4000
const PORTAL_PULSE_SCALE_TO = 1.08
const PORTAL_PULSE_DURATION = 1800
const REDUCED_MOTION_PULSE_SCALE_TO = 1.03
const REDUCED_MOTION_PULSE_DURATION = 2400
// Distancia MÁS ALLÁ de worldWidth (no antes) a la que vive el portal — así
// nunca coincide con un elemento de descubrimiento (positionX*worldWidth
// nunca supera worldWidth) ni con el rango normal de paseo de Nubi
// (WorldMapScene.clampToWorldWidth lo recorta a [0, worldWidth]); solo el
// gesto explícito de tocar el portal manda a Nubi más allá de ese límite
// (ver WorldMapScene.handlePortalTouched). Exportado porque
// WorldMapScene.computeMaxScrollOffset necesita el mismo valor para que la
// cámara pueda revelar el portal en vez de dejarlo fuera de la zona visible.
export const PORTAL_MARGIN = 120

const BIOME_PORTAL_TINT: Record<string, number> = {
    meadow: 0x4caf50,
    farm: 0x795548,
    woods: 0x9c27b0,
    beach: 0x42a5f5,
    space: 0x3f51b5,
    prehistory: 0xf44336
}

const EXIT_ASSET_KEY = 'exit-portal'
const PORTAL_HIT_AREA_SIZE = 96

export const PORTAL_TOUCHED_EVENT = 'portal-touched'

export class ExitPortalLayer {
    private scene: Scene
    private container?: Phaser.GameObjects.Container
    private portalTweens: Phaser.Tweens.Tween[] = []

    constructor(scene: Scene) {
        this.scene = scene
    }

    create(biome: string, worldWidth: number, groundTopY: number): Phaser.GameObjects.Container | undefined {
        this.destroy()

        if (biome === 'prehistory') return undefined

        this.container = this.scene.add.container(0, 0)
        this.container.setDepth(PORTAL_DEPTH)

        const x = worldWidth + PORTAL_MARGIN
        const y = groundTopY - PORTAL_SIZE / 2 + biomeYOffset(biome, 'exitPortal')
        const tint = BIOME_PORTAL_TINT[biome] ?? 0xcccccc

        const portal = this.createPortalVisual(x, y, tint)
        this.container.add(portal)

        const zone = this.scene.add.zone(x, y, PORTAL_HIT_AREA_SIZE, PORTAL_HIT_AREA_SIZE)
        zone.setInteractive({ useHandCursor: true })
        zone.on('pointerdown', () => {
            this.scene.events.emit(PORTAL_TOUCHED_EVENT)
        })
        this.container.add(zone)

        this.applyPortalAnimation(portal)

        return this.container
    }

    private createPortalVisual(x: number, y: number, tint: number): Phaser.GameObjects.Container {
        const portalContainer = this.scene.add.container(x, y)

        if (this.scene.textures.exists(EXIT_ASSET_KEY)) {
            const img = this.scene.add.image(0, 0, EXIT_ASSET_KEY)
            const scale = Math.min(1, PORTAL_SIZE / Math.max(img.width, img.height))
            img.setDisplaySize(img.width * scale, img.height * scale)
            img.setTint(tint)
            portalContainer.add(img)
            return portalContainer
        }

        // Fallback: geometric portal with rings and swirls
        const outerRing = this.scene.add.circle(0, 0, PORTAL_SIZE / 2, tint, 0.3)
        const midRing = this.scene.add.circle(0, 0, PORTAL_SIZE / 3, tint, 0.5)
        const innerRing = this.scene.add.circle(0, 0, PORTAL_SIZE / 5, tint, 0.7)
        const core = this.scene.add.circle(0, 0, PORTAL_SIZE / 8, 0xffffff, 0.9)

        const swirl1 = this.scene.add.ellipse(0, 0, PORTAL_SIZE * 0.6, PORTAL_SIZE * 0.25, tint, 0.4)
        swirl1.setAngle(30)
        const swirl2 = this.scene.add.ellipse(0, 0, PORTAL_SIZE * 0.6, PORTAL_SIZE * 0.25, tint, 0.4)
        swirl2.setAngle(-30)

        portalContainer.add([outerRing, swirl1, swirl2, midRing, innerRing, core])

        return portalContainer
    }

    private applyPortalAnimation(portal: Phaser.GameObjects.Container) {
        const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches

        if (reducedMotion) {
            this.portalTweens.push(this.scene.tweens.add({
                targets: portal,
                scaleX: REDUCED_MOTION_PULSE_SCALE_TO,
                scaleY: REDUCED_MOTION_PULSE_SCALE_TO,
                duration: REDUCED_MOTION_PULSE_DURATION,
                yoyo: true,
                repeat: -1,
                ease: 'Sine.easeInOut'
            }))
            return
        }

        this.portalTweens.push(this.scene.tweens.add({
            targets: portal,
            angle: 360,
            duration: PORTAL_ROTATION_SPEED,
            repeat: -1,
            ease: 'Linear'
        }))

        this.portalTweens.push(this.scene.tweens.add({
            targets: portal,
            scaleX: PORTAL_PULSE_SCALE_TO,
            scaleY: PORTAL_PULSE_SCALE_TO,
            duration: PORTAL_PULSE_DURATION,
            yoyo: true,
            repeat: -1,
            ease: 'Sine.easeInOut'
        }))
    }

    destroy() {
        this.portalTweens.forEach(tween => tween.stop())
        this.portalTweens = []
        this.container?.destroy()
        this.container = undefined
    }
}
