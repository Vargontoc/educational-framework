export const WORLD_MAP_CONFIG = {
    viewportWidth: 1280,
    viewportHeight: 720,
    worldWidth: 3560,
    maxScrollOffset: 1280,
    scrollMaxSpeed: 120,
    skyboxAutoScrollSpeed: 15,
    tapMaxDistance: 10,
    nubiWalkSpeed: 220,
    parallaxFactor: 0.4,
    groundFactor: 1,
    inertiaDuration: 500,
    inertiaEase: 'Cubic.out',
    minHitAreaSize: 80,
    interactiveMinSeparation: 60,
    reactionMinDuration: 300,
    reactionMaxDuration: 600,
    nubiIdleScaleFrom: 1.0,
    nubiIdleScaleTo: 1.05,
    nubiIdleDuration: 2000,
    nubiIdleEase: 'Sine.easeInOut',
    edgeProximityThreshold: 200,
    arrivalFadeShort: 800,
    arrivalFadeDistant: 1600,
    arrivalAudioTimeout: 3000,
    arrivalNoVoiceDelay: 1000
} as const

export const DISTANT_BIOME_PAIRS: ReadonlyArray<readonly [string, string]> = [
    ['woods', 'space'],
    ['space', 'woods'],
    ['beach', 'space'],
    ['space', 'beach'],
    ['prehistory', 'space'],
    ['space', 'prehistory']
]

export function isDistantBiomePair(from: string, to: string): boolean {
    return DISTANT_BIOME_PAIRS.some(([a, b]) => a === from && b === to)
}
