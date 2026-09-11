import { Scene } from "phaser"
import type { SpineGameObject } from "@esotericsoftware/spine-phaser-v4"
import { WORLD_MAP_CONFIG } from "../config/worldMapConfig"
import { WORLDMAP_SCROLL_MOVE_EVENT } from "../scroll/GradualScroller"

const NUBI_DEPTH = 4
const SPINE_SKELETON_KEY = 'spine-boy-data'
const SPINE_ATLAS_KEY = 'spineboy-atlas'
const SPINE_SKELETON_PATH = '/assets/animations/spineboy/spineboy-pro.skel'
const SPINE_ATLAS_PATH = '/assets/animations/spineboy/spineboy-pma.atlas'

export class NubiLayer {
    private scene: Scene
    private spineBoy?: SpineGameObject
    private npcEnabled = false
    private scrolling = false
    private onNpcStateChanged = (enabled: boolean) => this.setNpcEnabled(enabled)
    private onScrollMove = (moving: boolean) => this.setScrolling(moving)

    constructor(scene: Scene) {
        this.scene = scene
    }

    preload() {
        this.scene.load.spineBinary(SPINE_SKELETON_KEY, SPINE_SKELETON_PATH)
        this.scene.load.spineAtlas(SPINE_ATLAS_KEY, SPINE_ATLAS_PATH)
    }

    create(npcEnabled: boolean) {
        const { viewportWidth } = WORLD_MAP_CONFIG
        const x = viewportWidth / 6

        this.spineBoy = this.scene.add.spine(x, 550, SPINE_SKELETON_KEY, SPINE_ATLAS_KEY)
        this.spineBoy.setDepth(NUBI_DEPTH)
        this.spineBoy.setScale(0.5)
        this.spineBoy.animationState.setAnimation(0, 'run', true)

        this.setNpcEnabled(npcEnabled)

        this.scene.events.on('npc-state-changed', this.onNpcStateChanged)
        this.scene.events.on(WORLDMAP_SCROLL_MOVE_EVENT, this.onScrollMove)
        this.scene.events.once('shutdown', () => this.destroy())
        this.scene.events.once('destroy', () => this.destroy())
    }

    setNpcEnabled(enabled: boolean) {
        this.npcEnabled = enabled
        this.applyTimeScale()
    }

    private setScrolling(moving: boolean) {
        this.scrolling = moving
        this.applyTimeScale()
    }

    // El paisaje se desplaza por acción del jugador (arrastre), no de forma
    // automática: Nubi permanece fijo en pantalla, así que la animación 'run'
    // solo debe reproducirse mientras ese desplazamiento está en curso (efecto
    // "cinta de correr"). Fuera de eso, se congela en el último frame vía timeScale.
    private applyTimeScale() {
        if (!this.spineBoy) return

        const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
        this.spineBoy.animationState.timeScale = (this.npcEnabled && !reducedMotion && this.scrolling) ? 1 : 0
    }

    destroy() {
        this.scene.events.off('npc-state-changed', this.onNpcStateChanged)
        this.scene.events.off(WORLDMAP_SCROLL_MOVE_EVENT, this.onScrollMove)
        this.spineBoy?.destroy()
        this.spineBoy = undefined
    }
}
