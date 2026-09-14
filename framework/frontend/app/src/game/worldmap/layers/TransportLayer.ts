import { Scene } from "phaser"
import { WORLD_MAP_CONFIG } from "../config/worldMapConfig"

const TRANSPORT_DEPTH = 5
const TRANSPORT_ICON_SIZE = 64
// A la IZQUIERDA del spawn de Nubi (más cerca de x=0), no a la derecha como
// antes: los elementos de descubrimiento pueden autorarse con positionX
// arbitrariamente bajo, así que colocar el transporte hacia el interior del
// mundo (como estaba) aumentaba el riesgo de solaparse con uno de ellos.
// Nota: esto NO saca al transporte del rango [0, worldWidth] por el que
// Nubi puede caminar normalmente (a diferencia del portal de salida, que sí
// vive fuera de ese rango — ver ExitPortalLayer/WorldMapScene) — hacerlo
// requeriría que GradualScroller admita offsets negativos y extender hacia
// atrás el tile de Ground/ParallaxLayer, un cambio bastante más grande que
// no se ha aplicado aquí; si el solape sigue siendo un problema real con
// contenido autorado cerca de positionX=0, ese es el siguiente paso.
const TRANSPORT_OFFSET_FROM_NUBI = 120
const TRANSPORT_MIN_X = 40
const REDUCED_MOTION_CUE_DURATION = 600
const CUE_DURATION = 1200

const BIOME_TRANSPORT_ICON: Record<string, string> = {
    meadow: '🎈',
    farm: '🚜',
    woods: '🍄',
    beach: '⛵',
    space: '🚀',
    prehistory: '🦕'
}

const BIOME_TRANSPORT_COLOR: Record<string, number> = {
    meadow: 0xff7043,
    farm: 0x8d6e63,
    woods: 0x66bb6a,
    beach: 0x42a5f5,
    space: 0x7e57c2,
    prehistory: 0xef6c00
}

const BIOME_TRANSPORT_SHAPE: Record<string, 'balloon' | 'tractor' | 'mushroom' | 'boat' | 'rocket' | 'dino'> = {
    meadow: 'balloon',
    farm: 'tractor',
    woods: 'mushroom',
    beach: 'boat',
    space: 'rocket',
    prehistory: 'dino'
}

export const TRANSPORT_TOUCHED_EVENT = 'transport-touched'

export class TransportLayer {
    private scene: Scene
    private container?: Phaser.GameObjects.Container
    private cueTweens: Phaser.Tweens.Tween[] = []

    constructor(scene: Scene) {
        this.scene = scene
    }

    create(biome: string, groundTopY: number, nubiStartX: number): Phaser.GameObjects.Container {
        this.destroy()

        this.container = this.scene.add.container(0, 0)
        this.container.setDepth(TRANSPORT_DEPTH)

        const x = Math.max(TRANSPORT_MIN_X, nubiStartX - TRANSPORT_OFFSET_FROM_NUBI)
        const y = groundTopY - TRANSPORT_ICON_SIZE

        const hitAreaSize = Math.max(WORLD_MAP_CONFIG.minHitAreaSize, TRANSPORT_ICON_SIZE + 16)
        const zone = this.scene.add.zone(x, y, hitAreaSize, hitAreaSize)
        zone.setInteractive({ useHandCursor: true })
        zone.on('pointerdown', () => {
            this.scene.events.emit(TRANSPORT_TOUCHED_EVENT)
        })

        const visual = this.createTransportVisual(biome, x, y)
        this.applyPassiveCue(biome, visual)

        this.container.add([zone, visual])

        return this.container
    }

    private createTransportVisual(biome: string, x: number, y: number): Phaser.GameObjects.Container {
        const elementContainer = this.scene.add.container(x, y)

        const assetKey = `transport-${biome}`
        if (this.scene.textures.exists(assetKey)) {
            const img = this.scene.add.image(0, 0, assetKey)
            const scale = Math.min(1, TRANSPORT_ICON_SIZE / Math.max(img.width, img.height))
            img.setDisplaySize(img.width * scale, img.height * scale)
            elementContainer.add(img)
            return elementContainer
        }

        const shape = BIOME_TRANSPORT_SHAPE[biome] ?? 'balloon'
        const color = BIOME_TRANSPORT_COLOR[biome] ?? 0xcccccc
        const icon = BIOME_TRANSPORT_ICON[biome] ?? '?'

        const bg = this.createShapeForTransport(shape, color)
        elementContainer.add(bg)

        const label = this.scene.add.text(0, 0, icon, {
            fontSize: '32px',
            color: '#ffffff',
            fontStyle: 'bold'
        })
        label.setOrigin(0.5)
        elementContainer.add(label)

        return elementContainer
    }

    private createShapeForTransport(shape: string, color: number): Phaser.GameObjects.GameObject {
        const half = TRANSPORT_ICON_SIZE / 2

        switch (shape) {
            case 'balloon': {
                const body = this.scene.add.circle(0, 8, half * 0.7, color)
                const basket = this.scene.add.rectangle(0, half + 4, half * 0.5, half * 0.3, 0x795548)
                const rope = this.scene.add.rectangle(0, half * 0.5, 2, half * 0.5, 0x795548)
                return this.scene.add.container(0, 0, [body, rope, basket])
            }
            case 'tractor': {
                const bodyRect = this.scene.add.rectangle(-4, 0, TRANSPORT_ICON_SIZE * 0.6, TRANSPORT_ICON_SIZE * 0.4, color)
                const wheel1 = this.scene.add.circle(-half * 0.4, half * 0.35, half * 0.3, 0x333333)
                const wheel2 = this.scene.add.circle(half * 0.3, half * 0.35, half * 0.2, 0x333333)
                const chimney = this.scene.add.rectangle(-half * 0.3, -half * 0.35, 6, half * 0.4, 0x555555)
                return this.scene.add.container(0, 0, [bodyRect, chimney, wheel1, wheel2])
            }
            case 'mushroom': {
                const cap = this.scene.add.circle(0, -4, half * 0.7, color)
                const stem = this.scene.add.rectangle(0, half * 0.4, half * 0.35, half * 0.5, 0xfff9c4)
                const spot1 = this.scene.add.circle(-8, -10, 5, 0xffffff)
                const spot2 = this.scene.add.circle(8, -4, 4, 0xffffff)
                return this.scene.add.container(0, 0, [stem, cap, spot1, spot2])
            }
            case 'boat': {
                const hull = this.scene.add.triangle(
                    0, 8,
                    -half * 0.7, 0,
                    half * 0.7, 0,
                    0, half * 0.5,
                    color
                )
                const sail = this.scene.add.triangle(
                    0, -4,
                    0, -half * 0.6,
                    half * 0.4, half * 0.15,
                    -2, half * 0.15,
                    0xffffff
                )
                const mast = this.scene.add.rectangle(0, -4, 2, half * 0.7, 0x795548)
                return this.scene.add.container(0, 0, [hull, mast, sail])
            }
            case 'rocket': {
                const bodyR = this.scene.add.rectangle(0, 4, half * 0.45, TRANSPORT_ICON_SIZE * 0.6, color)
                const nose = this.scene.add.triangle(
                    0, -half * 0.55,
                    0, -half * 0.35,
                    half * 0.22, 0,
                    -half * 0.22, 0,
                    0xef5350
                )
                const fin1 = this.scene.add.triangle(
                    -half * 0.25, half * 0.5,
                    0, -half * 0.15,
                    0, half * 0.15,
                    -half * 0.2, half * 0.15,
                    0xef5350
                )
                const fin2 = this.scene.add.triangle(
                    half * 0.25, half * 0.5,
                    0, -half * 0.15,
                    half * 0.2, half * 0.15,
                    0, half * 0.15,
                    0xef5350
                )
                const windowR = this.scene.add.circle(0, -2, 6, 0xbbdefb)
                return this.scene.add.container(0, 0, [bodyR, nose, fin1, fin2, windowR])
            }
            case 'dino': {
                const bodyD = this.scene.add.circle(0, 4, half * 0.5, color)
                const head = this.scene.add.circle(half * 0.35, -half * 0.2, half * 0.3, color)
                const eye = this.scene.add.circle(half * 0.42, -half * 0.28, 3, 0xffffff)
                const tail = this.scene.add.triangle(
                    -half * 0.5, 4,
                    0, -half * 0.1,
                    0, half * 0.1,
                    -half * 0.4, 0,
                    color
                )
                const leg1 = this.scene.add.rectangle(-8, half * 0.5, 6, half * 0.25, color)
                const leg2 = this.scene.add.rectangle(8, half * 0.5, 6, half * 0.25, color)
                return this.scene.add.container(0, 0, [tail, bodyD, head, eye, leg1, leg2])
            }
            default:
                return this.scene.add.circle(0, 0, half, color)
        }
    }

    // Cada transporte tiene su propio "carácter" de reposo en vez de un
    // único cue genérico compartido — ver funciones apply*Cue más abajo.
    // Bajo prefers-reduced-motion todos degradan al mismo pulso de alpha
    // simple, sin excepción por bioma.
    private applyPassiveCue(biome: string, visual: Phaser.GameObjects.Container) {
        const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches

        if (reducedMotion) {
            this.cueTweens.push(this.scene.tweens.add({
                targets: visual,
                alpha: 0.8,
                duration: REDUCED_MOTION_CUE_DURATION,
                yoyo: true,
                repeat: -1,
                ease: 'Sine.easeInOut'
            }))
            return
        }

        switch (biome) {
            case 'meadow':
                this.applyMeadowCue(visual)
                break
            case 'beach':
                this.applyBeachCue(visual)
                break
            case 'farm':
                this.applyFarmCue(visual)
                break
            case 'space':
                this.applySpaceCue(visual)
                break
            case 'woods':
                this.applyWoodsCue(visual)
                break
            case 'prehistory':
                this.applyPrehistoryCue(visual)
                break
            default:
                this.applyGenericCue(visual)
        }
    }

    // Pradera (globo): flota suspendido, sube y baja muy lento, con un
    // balanceo sutil de rotación en un período distinto al de la subida
    // para que no se vea como un movimiento único y mecánico.
    private applyMeadowCue(visual: Phaser.GameObjects.Container) {
        this.cueTweens.push(this.scene.tweens.add({
            targets: visual,
            y: visual.y - 10,
            duration: 2600,
            yoyo: true,
            repeat: -1,
            ease: 'Sine.easeInOut'
        }))

        visual.setAngle(-3)
        this.cueTweens.push(this.scene.tweens.add({
            targets: visual,
            angle: 3,
            duration: 3400,
            yoyo: true,
            repeat: -1,
            ease: 'Sine.easeInOut'
        }))
    }

    // Playa (barco): balanceo rítmico de lado a lado (rotación), como
    // mecido por las olas, con un pequeño vaivén vertical desfasado en
    // cuarto de ciclo para reforzar la sensación de oleaje.
    private applyBeachCue(visual: Phaser.GameObjects.Container) {
        visual.setAngle(-6)
        this.cueTweens.push(this.scene.tweens.add({
            targets: visual,
            angle: 6,
            duration: 1400,
            yoyo: true,
            repeat: -1,
            ease: 'Sine.easeInOut'
        }))

        this.cueTweens.push(this.scene.tweens.add({
            targets: visual,
            y: visual.y + 4,
            duration: 1400,
            delay: 350,
            yoyo: true,
            repeat: -1,
            ease: 'Sine.easeInOut'
        }))
    }

    // Granja (tractor): sacudidas cortas y rápidas de ángulo + una micro
    // vibración vertical en un período ligeramente distinto, como un motor
    // al ralentí — amplitud pequeña, ciclo muy corto.
    private applyFarmCue(visual: Phaser.GameObjects.Container) {
        visual.setAngle(-1.5)
        this.cueTweens.push(this.scene.tweens.add({
            targets: visual,
            angle: 1.5,
            duration: 90,
            yoyo: true,
            repeat: -1,
            ease: 'Sine.easeInOut'
        }))

        this.cueTweens.push(this.scene.tweens.add({
            targets: visual,
            y: visual.y + 1.5,
            duration: 110,
            yoyo: true,
            repeat: -1,
            ease: 'Sine.easeInOut'
        }))
    }

    // Espacio (cohete): el cuerpo queda casi quieto (solo una deriva
    // vertical mínima para no verse muerto); el movimiento real está en la
    // pequeña llama de propulsión añadida en la base, que parpadea rápido
    // en alpha y escala.
    private applySpaceCue(visual: Phaser.GameObjects.Container) {
        this.cueTweens.push(this.scene.tweens.add({
            targets: visual,
            y: visual.y + 1,
            duration: 3000,
            yoyo: true,
            repeat: -1,
            ease: 'Sine.easeInOut'
        }))

        const flame = this.scene.add.triangle(
            0, TRANSPORT_ICON_SIZE / 2 + 2,
            -6, 0,
            6, 0,
            0, 14,
            0xffa726
        )
        flame.setOrigin(0.5, 0)
        visual.add(flame)

        this.cueTweens.push(this.scene.tweens.add({
            targets: flame,
            alpha: 0.4,
            scaleY: 0.6,
            duration: 180,
            yoyo: true,
            repeat: -1,
            ease: 'Sine.easeInOut'
        }))
    }

    // Bosque (seta/escoba): flota como la pradera, pero con un balanceo más
    // errático/mágico — tres tweens independientes (ángulo, x, y) con
    // períodos distintos que se desfasan entre sí en vez de moverse en un
    // único eje sincronizado.
    private applyWoodsCue(visual: Phaser.GameObjects.Container) {
        visual.setAngle(-5)
        this.cueTweens.push(this.scene.tweens.add({
            targets: visual,
            angle: 5,
            duration: 900,
            yoyo: true,
            repeat: -1,
            ease: 'Sine.easeInOut'
        }))

        this.cueTweens.push(this.scene.tweens.add({
            targets: visual,
            x: visual.x + 6,
            duration: 1300,
            yoyo: true,
            repeat: -1,
            ease: 'Sine.easeInOut'
        }))

        this.cueTweens.push(this.scene.tweens.add({
            targets: visual,
            y: visual.y - 8,
            duration: 1700,
            yoyo: true,
            repeat: -1,
            ease: 'Sine.easeInOut'
        }))
    }

    // Prehistoria (dino): pequeño pulso de escala, como si respirara.
    private applyPrehistoryCue(visual: Phaser.GameObjects.Container) {
        this.cueTweens.push(this.scene.tweens.add({
            targets: visual,
            scaleX: 1.06,
            scaleY: 1.06,
            duration: 1400,
            yoyo: true,
            repeat: -1,
            ease: 'Sine.easeInOut'
        }))
    }

    // Respaldo genérico si en el futuro se añade un bioma sin cue propio.
    private applyGenericCue(visual: Phaser.GameObjects.Container) {
        this.cueTweens.push(this.scene.tweens.add({
            targets: visual,
            y: visual.y - 6,
            scaleX: 1.05,
            scaleY: 1.05,
            duration: CUE_DURATION,
            yoyo: true,
            repeat: -1,
            ease: 'Sine.easeInOut'
        }))
    }

    destroy() {
        this.cueTweens.forEach(tween => tween.stop())
        this.cueTweens = []
        this.container?.destroy()
        this.container = undefined
    }
}
