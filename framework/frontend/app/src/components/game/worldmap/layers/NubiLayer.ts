import { Scene } from "phaser"
import type { SpineGameObject } from "@esotericsoftware/spine-phaser-v4"
import { WORLD_MAP_CONFIG } from "../config/worldMapConfig"

const NUBI_DEPTH = 3
const SPINE_SKELETON_KEY = 'spine-boy-data'
const SPINE_ATLAS_KEY = 'spineboy-atlas'
const SPINE_SKELETON_PATH = '/animations/spineboy/spineboy-pro.skel'
const SPINE_ATLAS_PATH = '/animations/spineboy/spineboy-pma.atlas'

export class NubiLayer {
    private scene: Scene
    private spineBoy?: SpineGameObject
    private onNpcStateChanged = (enabled: boolean) => this.setNpcEnabled(enabled)

    constructor(scene: Scene) {
        this.scene = scene
    }

    preload() {
        this.scene.load.spineBinary(SPINE_SKELETON_KEY, SPINE_SKELETON_PATH)
        this.scene.load.spineAtlas(SPINE_ATLAS_KEY, SPINE_ATLAS_PATH)
    }

    create(npcEnabled: boolean) {
        const { viewportWidth, viewportHeight } = WORLD_MAP_CONFIG
        const x = viewportWidth / 6
        const y = viewportHeight / 2

        this.spineBoy = this.scene.add.spine(x, y, SPINE_SKELETON_KEY, SPINE_ATLAS_KEY)
        this.spineBoy.setDepth(NUBI_DEPTH)
        this.spineBoy.setScale(0.5)
        this.spineBoy.animationState.setAnimation(0, 'run', true)

        this.setNpcEnabled(npcEnabled)

        this.scene.events.on('npc-state-changed', this.onNpcStateChanged)
        this.scene.events.once('shutdown', () => this.destroy())
        this.scene.events.once('destroy', () => this.destroy())
    }

    setNpcEnabled(enabled: boolean) {
        if (!this.spineBoy) return

        const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
        // Sin tween de escala (esto es una animación Spine, no el placeholder circular):
        // pausar/reanudar la reproducción vía timeScale respeta tanto la preferencia
        // parental de NPC como prefers-reduced-motion sin depender de nombres de animación.
        this.spineBoy.animationState.timeScale = (enabled && !reducedMotion) ? 1 : 0
    }

    destroy() {
        this.scene.events.off('npc-state-changed', this.onNpcStateChanged)
        this.spineBoy?.destroy()
        this.spineBoy = undefined
    }
}
