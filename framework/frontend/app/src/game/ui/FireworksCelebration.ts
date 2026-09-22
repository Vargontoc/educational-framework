import { Scene, TintModes } from "phaser"

/**
 * Celebration of the minigames: several fireworks bursting at random points of the viewport, each one with its
 * own tint. It uses the `fireworks.png` spritesheet (6 columns x 5 rows of 256x256 frames, one yellow burst that
 * opens and falls); the tint is applied as a fill, so any colour keeps the shape of the burst.
 */

export const FIREWORKS_TEXTURE_KEY = 'effect-fireworks'
const FIREWORKS_URL = '/assets/animations/effects/fireworks.png'
const FRAME_SIZE = 256
const FRAME_COUNT = 30
const FRAME_RATE = 24
const ANIMATION_KEY = 'fireworks-burst'

// Bright colours that stand out on every biome background (green is left out: the meadow is green).
const BURST_COLORS = [0xFF3B30, 0xFF9500, 0xFFD60A, 0xFF2D95, 0x8E5CF7, 0x32ADE6, 0x00C7BE, 0xFFFFFF]
const BURST_COUNT_MIN = 5
const BURST_COUNT_MAX = 7
const STAGGER_MAX_MS = 900
const SCALE_MIN = 0.9
const SCALE_MAX = 1.5
// Where the bursts go: the upper part of the viewport, because each burst falls as it fades.
const AREA_LEFT = 0.12
const AREA_RIGHT = 0.88
const AREA_TOP = 0.14
const AREA_BOTTOM = 0.6
const GRID_COLUMNS = 3
const GRID_ROWS = 2
// The centre of the burst inside its frame (fraction of the frame), so the burst opens where it was placed.
const ORIGIN_X = 0.5
const ORIGIN_Y = 0.38
// Reduced motion: no animation, a static frame of the burst.
const REDUCED_MOTION_FRAME = 12
const REDUCED_MOTION_DURATION_MS = 1500
const END_MARGIN_MS = 150

export interface FireworksResult {
    /** Number of bursts launched. */
    count: number
    /** Time until the last burst has finished: the caller can leave the scene after it. */
    durationMs: number
}

export class FireworksCelebration {
    /** Starts loading the spritesheet (once per game) so it is ready when the celebration comes. */
    static preload(scene: Scene): void {
        if (scene.textures.exists(FIREWORKS_TEXTURE_KEY)) return
        if ([...scene.load.list].some(file => file.key === FIREWORKS_TEXTURE_KEY)) return

        scene.load.spritesheet(FIREWORKS_TEXTURE_KEY, FIREWORKS_URL, {
            frameWidth: FRAME_SIZE,
            frameHeight: FRAME_SIZE
        })
        if (!scene.load.isLoading()) scene.load.start()
    }

    /**
     * Launches the bursts: 5 to 7 of them, spread over the viewport with a random tint, size and start time.
     * Without the spritesheet (failed to load) nothing is drawn, but the returned duration still holds.
     */
    static play(scene: Scene, options: { reducedMotion: boolean, depth: number }): FireworksResult {
        const count = BURST_COUNT_MIN + Math.floor(Math.random() * (BURST_COUNT_MAX - BURST_COUNT_MIN + 1))
        const animationMs = Math.ceil(FRAME_COUNT / FRAME_RATE * 1000)
        const durationMs = options.reducedMotion
            ? REDUCED_MOTION_DURATION_MS
            : STAGGER_MAX_MS + animationMs + END_MARGIN_MS

        const launch = () => FireworksCelebration.launch(scene, count, options)
        if (scene.textures.exists(FIREWORKS_TEXTURE_KEY)) {
            launch()
        } else {
            FireworksCelebration.preload(scene)
            scene.load.once(`filecomplete-spritesheet-${FIREWORKS_TEXTURE_KEY}`, launch)
        }
        return { count, durationMs }
    }

    private static launch(scene: Scene, count: number, options: { reducedMotion: boolean, depth: number }): void {
        if (!scene.sys.isActive()) return

        if (!scene.anims.exists(ANIMATION_KEY)) {
            scene.anims.create({
                key: ANIMATION_KEY,
                frames: scene.anims.generateFrameNumbers(FIREWORKS_TEXTURE_KEY, { start: 0, end: FRAME_COUNT - 1 }),
                frameRate: FRAME_RATE,
                repeat: 0
            })
        }

        const points = FireworksCelebration.pickPoints(scene.scale.width, scene.scale.height, count)
        const colors = FireworksCelebration.pickColors(count)

        points.forEach((point, i) => {
            const scale = SCALE_MIN + Math.random() * (SCALE_MAX - SCALE_MIN)
            const burst = scene.add.sprite(point.x, point.y, FIREWORKS_TEXTURE_KEY, options.reducedMotion ? REDUCED_MOTION_FRAME : 0)
            burst.setOrigin(ORIGIN_X, ORIGIN_Y)
            burst.setScale(scale * scene.scale.height / 720)
            burst.setDepth(options.depth)
            burst.setScrollFactor(0)
            burst.setTint(colors[i])
            burst.setTintMode(TintModes.FILL)

            if (options.reducedMotion) return

            burst.setVisible(false)
            scene.time.delayedCall(Math.random() * STAGGER_MAX_MS, () => {
                burst.setVisible(true)
                burst.play(ANIMATION_KEY)
                burst.once('animationcomplete', () => burst.destroy())
            })
        })
    }

    /**
     * One point per cell of a grid over the firework area, in random order and jittered inside their cell:
     * random, but never all piled in the same corner. With more bursts than cells the extra ones go anywhere.
     */
    private static pickPoints(width: number, height: number, count: number): Array<{ x: number, y: number }> {
        const cellWidth = (AREA_RIGHT - AREA_LEFT) * width / GRID_COLUMNS
        const cellHeight = (AREA_BOTTOM - AREA_TOP) * height / GRID_ROWS

        const cells: Array<{ x: number, y: number }> = []
        for (let row = 0; row < GRID_ROWS; row++) {
            for (let column = 0; column < GRID_COLUMNS; column++) {
                cells.push({
                    x: AREA_LEFT * width + (column + 0.15 + Math.random() * 0.7) * cellWidth,
                    y: AREA_TOP * height + (row + 0.15 + Math.random() * 0.7) * cellHeight
                })
            }
        }
        FireworksCelebration.shuffle(cells)

        const points = cells.slice(0, count)
        while (points.length < count) {
            points.push({
                x: (AREA_LEFT + Math.random() * (AREA_RIGHT - AREA_LEFT)) * width,
                y: (AREA_TOP + Math.random() * (AREA_BOTTOM - AREA_TOP)) * height
            })
        }
        return points
    }

    /** Random colours without repeating one until the palette runs out. */
    private static pickColors(count: number): number[] {
        const colors: number[] = []
        while (colors.length < count) {
            const round = [...BURST_COLORS]
            FireworksCelebration.shuffle(round)
            colors.push(...round)
        }
        return colors.slice(0, count)
    }

    private static shuffle<T>(items: T[]): void {
        for (let i = items.length - 1; i > 0; i--) {
            const j = Math.floor(Math.random() * (i + 1))
            ;[items[i], items[j]] = [items[j], items[i]]
        }
    }
}
