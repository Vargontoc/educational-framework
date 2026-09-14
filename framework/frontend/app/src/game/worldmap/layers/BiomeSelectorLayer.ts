import { Scene } from "phaser"
import { WORLD_MAP_CONFIG } from "../config/worldMapConfig"

const SELECTOR_DEPTH = 100
const OVERLAY_BG_COLOR = 0x000000
const OVERLAY_BG_ALPHA = 0.55
const STICKER_SIZE = 90
const STICKER_INNER_SIZE = 130
const MAP_AREA_WIDTH_RATIO = 0.85
const MAP_AREA_HEIGHT_RATIO = 0.7
const MAP_AREA_TOP_RATIO = 0.25
const REDUCED_MOTION_FADE_DURATION = 100
const FADE_DURATION = 250

const MAP_ASSET_KEY = 'biome-selector-map'

/**
 * Los stickers se colocan en un ANILLO centrado sobre el mapa, uno por cada
 * "hueco" de `sequenceOrder` (1..totalSlots) — no en coordenadas autoradas a
 * mano por bioma. El ángulo de cada bioma es fijo
 * (`slotAngle = RING_START_ANGLE_DEG + (sequenceOrder-1) * 360/totalSlots`),
 * así que si el bioma actual se excluye de la lista (no se puede viajar a
 * donde ya estás), su hueco en el anillo queda simplemente vacío — un
 * anillo simétrico con una plaza libre no se lee como "mapa roto", al
 * contrario de lo que pasaba con posiciones fijas dibujadas a mano sobre el
 * mapa (ver discusión de este mismo layer antes del cambio a anillo).
 *
 * `totalSlots` (nº total de biomas del catálogo, hoy 6) lo pasa quien llama
 * a `open()` — no está hardcodeado aquí a propósito: `BiomeSelectorLayer`
 * solo ve la lista ya filtrada (sin el bioma actual), así que no puede
 * deducir el total de forma fiable a partir de ella (si el bioma excluido
 * es justo el de `sequenceOrder` más alto, el máximo de los restantes
 * daría un total incorrecto). `WorldMapScene` es quien conoce el catálogo
 * completo (`ALL_BIOME_HOSTS.length`) y es la única fuente de verdad para
 * ese número.
 *
 * Por qué es resolución-independiente: el radio y el centro se calculan a
 * partir del recuadro real del mapa ya reescalado (`computeMapLayout`), no
 * de píxeles absolutos, así que el anillo siempre queda proporcional al
 * mapa sea cual sea la resolución final de pantalla (`Scale.ENVELOP`).
 *
 * Los únicos valores a ajustar si el resultado no encaja bien con el arte
 * de `mapa.png` son estos dos:
 * - `RING_RADIUS_RATIO`: fracción del semieje menor del mapa que ocupa el
 *   radio del anillo (más alto separa más los stickers entre sí, pero
 *   arriesga sacarlos fuera del dibujo del mapa si se acerca a 1).
 * - `RING_START_ANGLE_DEG`: ángulo del primer hueco (`sequenceOrder=1`,
 *   MEADOW); -90° lo sitúa arriba, avanzando en sentido horario.
 *
 * `warnIfStickersTooClose` sigue avisando por consola si, tras el cálculo,
 * dos stickers quedan más cerca que su propia zona táctil — con espaciado
 * angular uniforme no debería ocurrir salvo que `RING_RADIUS_RATIO` sea
 * demasiado bajo para el tamaño de `STICKER_SIZE`.
 */
const RING_RADIUS_RATIO = 0.62
const RING_START_ANGLE_DEG = -90

// Animación idónea de reposo (mismo patrón que InteractiveLayer.applyPassiveCue):
// un balanceo sutil de rotación más un pulso de escala, en bucle, para que los
// stickers no se vean estáticos sin ser llamativos. Desfasados por índice
// (STICKER_ANIMATION_STAGGER_MS) para que no se muevan todos a la vez.
const STICKER_SWAY_ANGLE_DEG = 4
const STICKER_SWAY_DURATION = 1800
const STICKER_PULSE_SCALE_TO = 1.06
const STICKER_PULSE_DURATION = 1600
const STICKER_ANIMATION_STAGGER_MS = 120

interface MapLayout {
    x: number
    y: number
    width: number
    height: number
    top: number
}


export const DESTINATION_SELECTED_EVENT = 'destination-selected'
export const SELECTOR_CLOSED_EVENT = 'selector-closed'

export interface BiomeHostInfo {
    biome: string
    sequenceOrder: number | null
}

export class BiomeSelectorLayer {
    private scene: Scene
    private container?: Phaser.GameObjects.Container
    private backdropZone?: Phaser.GameObjects.Zone
    private visible = false
    
    private stickerTweens: Phaser.Tweens.Tween[] = []

    constructor(scene: Scene) {
        this.scene = scene
    }

    // totalSlots: nº total de biomas del catálogo (no solo los que se
    // muestran) — ver comentario junto a RING_RADIUS_RATIO más arriba sobre
    // por qué debe pasarlo el llamador en vez de inferirse aquí.
    open(hosts: BiomeHostInfo[], totalSlots: number) {
        if (this.visible) return
        this.visible = true

        this.container = this.scene.add.container(0, 0)
        this.container.setDepth(SELECTOR_DEPTH)

        const { viewportWidth, viewportHeight } = WORLD_MAP_CONFIG

        const backdrop = this.scene.add.rectangle(
            viewportWidth / 2, viewportHeight / 2,
            viewportWidth, viewportHeight,
            OVERLAY_BG_COLOR, OVERLAY_BG_ALPHA
        )
        backdrop.setScrollFactor(0)

        this.backdropZone = this.scene.add.zone(
            viewportWidth / 2, viewportHeight / 2,
            viewportWidth, viewportHeight
        )
        this.backdropZone.setInteractive({ useHandCursor: false })
        this.backdropZone.on('pointerdown', (_pointer: Phaser.Input.Pointer) => {
            this.close()
        })

        this.container.add([backdrop, this.backdropZone])

        this.buildMapArea(hosts, totalSlots)
        // this.buildNubiPlaceholder()

        const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
        if (!reducedMotion) {
            this.container.setAlpha(0)
            this.scene.tweens.add({
                targets: this.container,
                alpha: 1,
                duration: FADE_DURATION,
                ease: 'Sine.easeOut'
            })
        }
    }

    close() {
        if (!this.visible || !this.container) return

        const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches

        if (reducedMotion) {
            this.cleanup()
            return
        }

        this.scene.tweens.add({
            targets: this.container,
            alpha: 0,
            duration: REDUCED_MOTION_FADE_DURATION,
            ease: 'Sine.easeIn',
            onComplete: () => {
                this.cleanup()
            }
        })
    }

    private cleanup() {
        this.scene.events.emit(SELECTOR_CLOSED_EVENT)
        this.stickerTweens.forEach(tween => tween.stop())
        this.stickerTweens = []
        this.container?.destroy()
        this.container = undefined
        this.backdropZone = undefined
        this.visible = false
    }


    private buildMapArea(hosts: BiomeHostInfo[], totalSlots: number) {
        if (!this.container) return


        const layout = this.computeMapLayout()

        if (this.scene.textures.exists(MAP_ASSET_KEY)) {
            const mapImage = this.scene.add.image(
                layout.x + layout.width / 2, layout.y + layout.height / 2,
                MAP_ASSET_KEY
            )
            mapImage.setDisplaySize(layout.width, layout.height)
            this.container.add(mapImage)
        } else {
            const mapBg = this.scene.add.rectangle(
                layout.x + layout.width / 2, layout.y + layout.height / 2,
                layout.width, layout.height,
                0xfff8e1, 0.9
            )
            mapBg.setStrokeStyle(3, 0x8d6e63)
            this.container.add(mapBg)
        }

        this.layoutStickers(hosts, layout, totalSlots)
    }

    // Encaja mapa.png (o el rectángulo placeholder si el asset no cargó)
    // dentro de MAP_AREA_WIDTH_RATIO/MAP_AREA_HEIGHT_RATIO del viewport,
    // preservando su proporción real en vez de estirarlo. El anillo de
    // stickers (ver RING_* arriba) se calcula siempre a partir de este
    // recuadro ya resuelto (`layout`), así que cualquier cambio de
    // resolución, de estos ratios o del propio tamaño del PNG se propaga
    // automáticamente sin tocar ninguna posición.
    private computeMapLayout(): MapLayout {
        const { viewportWidth, viewportHeight } = WORLD_MAP_CONFIG
        const areaWidth = viewportWidth * MAP_AREA_WIDTH_RATIO
        const areaHeight = viewportHeight * MAP_AREA_HEIGHT_RATIO
        const areaTop = viewportHeight * MAP_AREA_TOP_RATIO

        if (this.scene.textures.exists(MAP_ASSET_KEY)) {
            const source = this.scene.textures.get(MAP_ASSET_KEY).getSourceImage()
            const nativeWidth = source.width
            const nativeHeight = source.height
            const scale = Math.min(areaWidth / nativeWidth, areaHeight / nativeHeight)
            const width = nativeWidth * scale
            const height = nativeHeight * scale
            return {
                x: (viewportWidth - width) / 2,
                y: areaTop + (areaHeight - height) / 2,
                width,
                height,
                top: areaTop
            }
        }

        return { x: (viewportWidth - areaWidth) / 2, y: areaTop, width: areaWidth, height: areaHeight, top: areaTop }
    }

    // Coloca cada sticker en su hueco fijo del anillo (ver comentario de
    // RING_* arriba), derivado de `sequenceOrder` — no del orden del array
    // `hosts` ni de su longitud. Un host sin `sequenceOrder` (no debería
    // ocurrir con los 6 biomas del catálogo, todos lo traen) cae en el hueco
    // 1 como fallback defensivo.
    private layoutStickers(hosts: BiomeHostInfo[], layout: MapLayout, totalSlots: number) {
        if (!this.container) return
        if (hosts.length === 0) return
        const centerX = layout.x + layout.width / 2
        const centerY = layout.y + layout.height / 2
        const radius = Math.min(layout.width, layout.height) / 2 * RING_RADIUS_RATIO

        const placements = hosts.map((host) => {
            const sequenceOrder = host.sequenceOrder ?? 1
            const angleDeg = RING_START_ANGLE_DEG + ((sequenceOrder - 1) * (360 / totalSlots))
            const angleRad = angleDeg * (Math.PI / 180)
            const cx = centerX + Math.cos(angleRad) * radius
            const cy = centerY + Math.sin(angleRad) * radius
            return { biome: host.biome, cx, cy }
        })

        this.warnIfStickersTooClose(placements)

        placements.forEach(({ biome, cx, cy }, index) => this.createSticker(biome, cx, cy, index))
    }

    // Aviso de desarrollo (no bloquea nada): si dos stickers quedan más
    // cerca entre sí que su propia zona táctil, el niño podría tocar uno por
    // error al intentar tocar el de al lado (riesgo señalado explícitamente
    // por Contenido). Con espaciado angular uniforme no debería dispararse
    // salvo que RING_RADIUS_RATIO sea demasiado bajo para STICKER_SIZE.
    private warnIfStickersTooClose(placements: Array<{ biome: string; cx: number; cy: number }>) {
        const minDistance = Math.max(WORLD_MAP_CONFIG.minHitAreaSize, STICKER_SIZE)

        for (let i = 0; i < placements.length; i++) {
            for (let j = i + 1; j < placements.length; j++) {
                const dx = placements[i].cx - placements[j].cx
                const dy = placements[i].cy - placements[j].cy
                const distance = Math.sqrt(dx * dx + dy * dy)

                if (distance < minDistance) {
                    console.warn(
                        `BiomeSelectorLayer: los stickers "${placements[i].biome}" y "${placements[j].biome}" ` +
                        `quedan a ${Math.round(distance)}px (mínimo recomendado ${minDistance}px) — ` +
                        `riesgo de toque accidental. Sube RING_RADIUS_RATIO o baja STICKER_SIZE.`
                    )
                }
            }
        }
    }

    private createSticker(biome: string, cx: number, cy: number, index: number) {
        if (!this.container) return

        const stickerContainer = this.scene.add.container(cx, cy)

        const assetKey = `sticker-${biome.toLowerCase()}`
        if (this.scene.textures.exists(assetKey)) {
            const img = this.scene.add.image(0, 0, assetKey)
            const scale = Math.min(1, STICKER_INNER_SIZE / Math.max(img.width, img.height))
            img.setDisplaySize(img.width * scale, img.height * scale)
            stickerContainer.add(img)
        }

        const hitSize = Math.max(WORLD_MAP_CONFIG.minHitAreaSize, STICKER_SIZE)
        const zone = this.scene.add.zone(0, 0, hitSize, hitSize)
        zone.setInteractive({ useHandCursor: true })
        zone.on('pointerdown', (pointer: Phaser.Input.Pointer) => {
            pointer.event.stopPropagation()
            this.handleStickerTouch(biome)
        })
        stickerContainer.add(zone)

        this.container.add(stickerContainer)
        this.applyIdleAnimation(stickerContainer, index)
    }

    // Balanceo (rotación) + pulso (escala) en bucle, desfasados por índice
    // para que el conjunto no se mueva de forma sincronizada. Se omite bajo
    // prefers-reduced-motion, igual que el resto de animaciones del layer.
    private applyIdleAnimation(target: Phaser.GameObjects.Container, index: number) {
        const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
        if (reducedMotion) return

        const delay = index * STICKER_ANIMATION_STAGGER_MS

        target.setAngle(-STICKER_SWAY_ANGLE_DEG)
        const swayTween = this.scene.tweens.add({
            targets: target,
            angle: STICKER_SWAY_ANGLE_DEG,
            duration: STICKER_SWAY_DURATION,
            delay,
            yoyo: true,
            repeat: -1,
            ease: 'Sine.easeInOut'
        })

        const pulseTween = this.scene.tweens.add({
            targets: target,
            scaleX: STICKER_PULSE_SCALE_TO,
            scaleY: STICKER_PULSE_SCALE_TO,
            duration: STICKER_PULSE_DURATION,
            delay,
            yoyo: true,
            repeat: -1,
            ease: 'Sine.easeInOut'
        })

        this.stickerTweens.push(swayTween, pulseTween)
    }


    private handleStickerTouch(biome: string) {
        this.visible = false
        this.scene.events.emit(DESTINATION_SELECTED_EVENT, biome)
        this.cleanup()
    }
}
