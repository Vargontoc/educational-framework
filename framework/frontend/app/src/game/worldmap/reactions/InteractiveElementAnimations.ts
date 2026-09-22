import { Scene, GameObjects } from "phaser"
import { MEADOW_ELEMENT_SIZE } from "../config/worldMapConfig"

/**
 * Per-element idle and tap animations for the meadow's interactive discovery elements (nuevos assets, ver
 * sprint de biome-meadow). Keyed by `visualAssetKey` (the same key used to resolve the texture in
 * `InteractiveLayer`), because that is the only field the frontend receives that uniquely identifies which
 * of these 14 elements a discovery element is.
 *
 * An asset key that is NOT in {@link BEHAVIOR_BY_ASSET_KEY} keeps the old generic reaction
 * (`EnvironmentReaction`, played by `WorldMapScene`) — this module only covers the 14 meadow codes that have
 * a defined per-element behaviour; it is a safety net for future/undesigned elements.
 */

export type InteractiveVisual = GameObjects.Image | GameObjects.Sprite

type ElementBehaviorKind =
    /** Continuous idle flap, code-only tween (butterflies): no tap-specific action. */
    | 'butterflyFlutter'
    /** Sprite idle loop (`daisy`) that pauses for a sprite tap animation (`daisy-anim`) and then resumes. */
    | 'daisy'
    /** Code-only idle sway (tween) that pauses for a sprite tap animation (`chest`) and then resumes. */
    | 'chest'
    /** Tap-only squash & stretch tween, no sprite sheet. */
    | 'mushroomBoing'
    /** Tap plays its own sprite sheet once, then returns to frame 0. */
    | 'spriteTapOnce'
    /** Tap plays its own sprite sheet once, then after a further delay resets to frame 0. */
    | 'wornTapThenReset'
    /** Static image only: no idle, no tap reaction at all (not even the generic one). */
    | 'static'

const BEHAVIOR_BY_ASSET_KEY: Record<string, { kind: ElementBehaviorKind }> = {
    'butterfly-1': { kind: 'butterflyFlutter' },
    'butterfly-2': { kind: 'butterflyFlutter' },
    'butterfly-3': { kind: 'butterflyFlutter' },
    daisy: { kind: 'daisy' },
    chest: { kind: 'chest' },
    mushroom: { kind: 'mushroomBoing' },
    beehive: { kind: 'spriteTapOnce' },
    swing: { kind: 'spriteTapOnce' },
    rainbow: { kind: 'spriteTapOnce' },
    worn: { kind: 'wornTapThenReset' },
    burrow: { kind: 'static' },
    door: { kind: 'static' },
    shaperbox: { kind: 'static' },
    stump: { kind: 'static' }
}

const SPRITE_BEHAVIOR_KINDS: ElementBehaviorKind[] = ['daisy', 'chest', 'spriteTapOnce', 'wornTapThenReset']

const DAISY_TEXTURE_KEY = 'daisy'
const DAISY_TAP_TEXTURE_KEY = 'daisy-anim'
const DAISY_IDLE_ANIM_KEY = 'daisy-idle-loop'
const DAISY_TAP_ANIM_KEY = 'daisy-tap-play'
const DAISY_IDLE_FRAME_COUNT = 30
const DAISY_TAP_FRAME_COUNT = 60
const DAISY_IDLE_FRAME_RATE = 10
const DAISY_TAP_FRAME_RATE = 20

const CHEST_TEXTURE_KEY = 'chest'
const CHEST_TAP_ANIM_KEY = 'chest-tap-play'

/** beehive/swing/rainbow/worn: 30-frame sheets, own key == asset key, tap animation key is `${assetKey}-tap-play`. */
const SIMPLE_SPRITESHEET_KEYS = ['beehive', 'swing', 'rainbow', 'worn']
const SPRITESHEET_FRAME_COUNT = 30
const TAP_FRAME_RATE = 15

const WORN_RESET_DELAY_MS = 1000

// Butterfly flutter (code-only tween: no sprite sheet for these three, just the static art).
/** Proporcion sobre la escala base (ver `applyTargetSize`), no un valor absoluto. */
const FLUTTER_SCALE_MIN = 0.75
const FLUTTER_FLAP_DURATION = 180
const FLUTTER_DRIFT_X = 14
const FLUTTER_DRIFT_DURATION = 1100
const FLUTTER_BOB_Y = 8
const FLUTTER_BOB_DURATION = 900

// Chest idle sway (code-only tween, paused while the tap sprite animation plays).
const SWAY_ANGLE_DEG = 3
const SWAY_DURATION = 1200

// Mushroom "boing" (code-only tween, no sprite sheet). Las escalas son proporciones sobre la escala base.
const BOING_SQUASH_DURATION = 90
const BOING_OVERSHOOT_DURATION = 110
const BOING_SETTLE_DURATION = 160
const BOING_SQUASH_SCALE_X = 1.25
const BOING_SQUASH_SCALE_Y = 0.75
const BOING_OVERSHOOT_SCALE_X = 0.85
const BOING_OVERSHOOT_SCALE_Y = 1.15

const BASE_SCALE_X_KEY = 'baseScaleX'
const BASE_SCALE_Y_KEY = 'baseScaleY'

// Reduced-motion fallback for a tap that would otherwise play a sprite animation.
const REDUCED_TAP_FADE_DURATION = 400

function reducedMotion(): boolean {
    return window.matchMedia('(prefers-reduced-motion: reduce)').matches
}

/**
 * Reduce `visual` al tamaño configurado para `assetKey` en `MEADOW_ELEMENT_SIZE` (ver `worldMapConfig.ts`,
 * ahí es donde se ajusta), sin ampliarlo nunca por encima de su tamaño nativo, y guarda la escala resultante
 * como "escala base" (`baseScaleX`/`baseScaleY`, en `setData`) para que las animaciones de escala (aleteo de
 * mariposa, "boing" de la seta) sean relativas a ella en vez de pisarla con un valor absoluto. Un `assetKey`
 * sin tamaño configurado se deja en su tamaño nativo.
 */
export function applyTargetSize(visual: InteractiveVisual, assetKey: string): void {
    const target = MEADOW_ELEMENT_SIZE[assetKey]
    if (target) {
        const scale = Math.min(1, target / Math.max(visual.width, visual.height))
        visual.setDisplaySize(visual.width * scale, visual.height * scale)
    }
    visual.setData(BASE_SCALE_X_KEY, visual.scaleX)
    visual.setData(BASE_SCALE_Y_KEY, visual.scaleY)
}

/** Lado mayor (px logicos) al que se reduce `assetKey` (`MEADOW_ELEMENT_SIZE`), o `undefined` si no está configurado. */
export function targetSizeFor(assetKey: string): number | undefined {
    return MEADOW_ELEMENT_SIZE[assetKey]
}

function baseScaleOf(visual: InteractiveVisual): { x: number, y: number } {
    const x = visual.getData(BASE_SCALE_X_KEY)
    const y = visual.getData(BASE_SCALE_Y_KEY)
    return { x: typeof x === 'number' ? x : 1, y: typeof y === 'number' ? y : 1 }
}

/** Whether `assetKey` needs an animated `Sprite` (idle loop and/or tap sheet) rather than a plain static `Image`. */
export function needsSprite(assetKey: string): boolean {
    const behavior = BEHAVIOR_BY_ASSET_KEY[assetKey]
    return !!behavior && SPRITE_BEHAVIOR_KINDS.includes(behavior.kind)
}

/** Whether `assetKey` has a defined behaviour here (including the "static, no reaction at all" ones). */
export function hasBehavior(assetKey: string): boolean {
    return assetKey in BEHAVIOR_BY_ASSET_KEY
}

/**
 * Registers (once) the Phaser animations these behaviours need. Safe to call every time `InteractiveLayer` is
 * (re)created: each animation is only created if missing, and only if its texture is actually loaded (a biome
 * other than meadow, or a missing asset, simply skips it — nothing plays for those, per `needsSprite`/`playTap`
 * checking `scene.anims.exists` before using them).
 */
export function ensureAnimations(scene: Scene): void {
    createOnceFrom(scene, DAISY_IDLE_ANIM_KEY, DAISY_TEXTURE_KEY, DAISY_IDLE_FRAME_COUNT, DAISY_IDLE_FRAME_RATE, -1)
    createOnceFrom(scene, DAISY_TAP_ANIM_KEY, DAISY_TAP_TEXTURE_KEY, DAISY_TAP_FRAME_COUNT, DAISY_TAP_FRAME_RATE, 0)
    createOnceFrom(scene, CHEST_TAP_ANIM_KEY, CHEST_TEXTURE_KEY, SPRITESHEET_FRAME_COUNT, TAP_FRAME_RATE, 0)
    SIMPLE_SPRITESHEET_KEYS.forEach(key =>
        createOnceFrom(scene, tapAnimKeyOf(key), key, SPRITESHEET_FRAME_COUNT, TAP_FRAME_RATE, 0))
}

function tapAnimKeyOf(assetKey: string): string {
    return `${assetKey}-tap-play`
}

function createOnceFrom(scene: Scene, animKey: string, textureKey: string, frameCount: number, frameRate: number, repeat: number): void {
    if (scene.anims.exists(animKey)) return
    if (!scene.textures.exists(textureKey)) return

    scene.anims.create({
        key: animKey,
        frames: scene.anims.generateFrameNumbers(textureKey, { start: 0, end: frameCount - 1 }),
        frameRate,
        repeat
    })
}

/** Starts the idle behaviour of `assetKey` (if it has one) on the just-created `visual`. No-op otherwise. */
export function attachIdle(scene: Scene, visual: InteractiveVisual, assetKey: string): void {
    const behavior = BEHAVIOR_BY_ASSET_KEY[assetKey]
    if (!behavior) return

    switch (behavior.kind) {
        case 'butterflyFlutter':
            attachButterflyFlutter(scene, visual)
            break
        case 'daisy':
            attachDaisyIdle(scene, visual as GameObjects.Sprite)
            break
        case 'chest':
            attachChestSway(scene, visual)
            break
        default:
            break
    }
}

/** Plays the tap behaviour of `assetKey` (if it has one). No-op for `'static'` and for keys without a behaviour. */
export function playTap(scene: Scene, visual: InteractiveVisual, assetKey: string): void {
    const behavior = BEHAVIOR_BY_ASSET_KEY[assetKey]
    if (!behavior) return

    switch (behavior.kind) {
        case 'daisy':
            playDaisyTap(scene, visual as GameObjects.Sprite)
            break
        case 'chest':
            playChestTap(scene, visual as GameObjects.Sprite)
            break
        case 'mushroomBoing':
            playMushroomBoing(scene, visual)
            break
        case 'spriteTapOnce':
            playSpriteTapOnce(scene, visual as GameObjects.Sprite, assetKey)
            break
        case 'wornTapThenReset':
            playWornTap(scene, visual as GameObjects.Sprite)
            break
        case 'butterflyFlutter':
        case 'static':
        default:
            break
    }
}

// --- butterflies ---------------------------------------------------------------------------------------------

function attachButterflyFlutter(scene: Scene, visual: InteractiveVisual): void {
    if (reducedMotion()) return

    const base = baseScaleOf(visual)
    scene.tweens.add({
        targets: visual,
        scaleX: base.x * FLUTTER_SCALE_MIN,
        duration: FLUTTER_FLAP_DURATION,
        yoyo: true,
        repeat: -1,
        ease: 'Sine.easeInOut'
    })
    scene.tweens.add({
        targets: visual,
        x: visual.x + FLUTTER_DRIFT_X,
        duration: FLUTTER_DRIFT_DURATION,
        yoyo: true,
        repeat: -1,
        ease: 'Sine.easeInOut'
    })
    scene.tweens.add({
        targets: visual,
        y: visual.y - FLUTTER_BOB_Y,
        duration: FLUTTER_BOB_DURATION,
        yoyo: true,
        repeat: -1,
        ease: 'Sine.easeInOut'
    })
}

// --- daisy (idle loop + tap sheet) ----------------------------------------------------------------------------

function attachDaisyIdle(scene: Scene, sprite: GameObjects.Sprite): void {
    if (reducedMotion()) return
    if (!scene.anims.exists(DAISY_IDLE_ANIM_KEY)) return
    sprite.play(DAISY_IDLE_ANIM_KEY)
}

function playDaisyTap(scene: Scene, sprite: GameObjects.Sprite): void {
    if (reducedMotion() || !scene.anims.exists(DAISY_TAP_ANIM_KEY)) {
        reducedTapFlash(scene, sprite)
        return
    }

    sprite.play(DAISY_TAP_ANIM_KEY)
    sprite.once('animationcomplete', () => {
        if (!sprite.active) return
        if (scene.anims.exists(DAISY_IDLE_ANIM_KEY)) {
            sprite.play(DAISY_IDLE_ANIM_KEY)
        } else {
            sprite.setFrame(0)
        }
    })
}

// --- chest (idle sway + tap sheet) ----------------------------------------------------------------------------

function attachChestSway(scene: Scene, visual: InteractiveVisual): void {
    if (reducedMotion()) return

    scene.tweens.add({
        targets: visual,
        angle: SWAY_ANGLE_DEG,
        duration: SWAY_DURATION,
        yoyo: true,
        repeat: -1,
        ease: 'Sine.easeInOut'
    })
}

function playChestTap(scene: Scene, sprite: GameObjects.Sprite): void {
    const pausedTweens = pauseTweensOf(scene, sprite)

    if (reducedMotion() || !scene.anims.exists(CHEST_TAP_ANIM_KEY)) {
        reducedTapFlash(scene, sprite)
        resumeTweens(pausedTweens)
        return
    }

    sprite.play(CHEST_TAP_ANIM_KEY)
    sprite.once('animationcomplete', () => {
        if (!sprite.active) return
        resumeTweens(pausedTweens)
    })
}

// --- beehive / swing / rainbow (tap sheet, no idle) -----------------------------------------------------------

function playSpriteTapOnce(scene: Scene, sprite: GameObjects.Sprite, assetKey: string): void {
    const animKey = tapAnimKeyOf(assetKey)

    if (reducedMotion() || !scene.anims.exists(animKey)) {
        reducedTapFlash(scene, sprite)
        return
    }

    sprite.play(animKey)
    sprite.once('animationcomplete', () => {
        if (sprite.active) sprite.setFrame(0)
    })
}

// --- worn (tap sheet, resets to frame 0 a further second after the animation finishes) -------------------------

function playWornTap(scene: Scene, sprite: GameObjects.Sprite): void {
    const animKey = tapAnimKeyOf('worn')
    const scheduleReset = () => scene.time.delayedCall(WORN_RESET_DELAY_MS, () => {
        if (sprite.active) sprite.setFrame(0)
    })

    if (reducedMotion() || !scene.anims.exists(animKey)) {
        reducedTapFlash(scene, sprite)
        scheduleReset()
        return
    }

    sprite.play(animKey)
    sprite.once('animationcomplete', () => {
        if (!sprite.active) return
        scheduleReset()
    })
}

// --- mushroom "boing" (tween only) ------------------------------------------------------------------------------

function playMushroomBoing(scene: Scene, visual: InteractiveVisual): void {
    if (reducedMotion()) {
        reducedTapFlash(scene, visual)
        return
    }

    const base = baseScaleOf(visual)
    scene.tweens.killTweensOf(visual)
    visual.setScale(base.x, base.y)
    scene.tweens.add({
        targets: visual,
        scaleX: base.x * BOING_SQUASH_SCALE_X,
        scaleY: base.y * BOING_SQUASH_SCALE_Y,
        duration: BOING_SQUASH_DURATION,
        ease: 'Sine.easeOut',
        onComplete: () => {
            if (!visual.active) return
            scene.tweens.add({
                targets: visual,
                scaleX: base.x * BOING_OVERSHOOT_SCALE_X,
                scaleY: base.y * BOING_OVERSHOOT_SCALE_Y,
                duration: BOING_OVERSHOOT_DURATION,
                ease: 'Sine.easeInOut',
                onComplete: () => {
                    if (!visual.active) return
                    scene.tweens.add({
                        targets: visual,
                        scaleX: base.x,
                        scaleY: base.y,
                        duration: BOING_SETTLE_DURATION,
                        ease: 'Back.easeOut'
                    })
                }
            })
        }
    })
}

// --- shared helpers ------------------------------------------------------------------------------------------

function pauseTweensOf(scene: Scene, visual: InteractiveVisual): Phaser.Tweens.Tween[] {
    const tweens = scene.tweens.getTweensOf(visual) as Phaser.Tweens.Tween[]
    tweens.forEach(tween => tween.pause())
    return tweens
}

function resumeTweens(tweens: Phaser.Tweens.Tween[]): void {
    tweens.forEach(tween => {
        if (!tween.isDestroyed()) tween.resume()
    })
}

/** Minimal reduced-motion feedback for a tap that would otherwise play a sprite animation. */
function reducedTapFlash(scene: Scene, visual: InteractiveVisual): void {
    visual.setAlpha(0.5)
    scene.tweens.add({
        targets: visual,
        alpha: 1,
        duration: REDUCED_TAP_FADE_DURATION,
        ease: 'Sine.easeInOut'
    })
}
