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

/**
 * Elementos del mapa del mundo que se asientan sobre el suelo y cuyo ajuste vertical se puede configurar por bioma.
 * - hills: la capa de colinas de fondo (`background-[bioma]`).
 * - exitPortal: el portal de salida al final del mundo.
 * - transport: el transporte para viajar entre biomas.
 * - nubi: Nubi.
 */
export type BiomeYOffsetTarget = 'hills' | 'exitPortal' | 'transport' | 'nubi'

/**
 * Ajuste vertical (en unidades del mapa: canvas de 1280x720) respecto a su posicion por defecto, que apoya el
 * elemento sobre el borde superior de la imagen del suelo (`ground-[bioma]`). Positivo = hacia ABAJO, negativo = hacia arriba.
 * Sirve para compensar el arte de cada bioma (p. ej. un suelo cuya imagen deja transparente la parte de arriba).
 * Los biomas o elementos que no aparezcan aqui no se desplazan.
 *
 * Para ajustar un elemento, cambia su valor aqui; no hace falta tocar las capas.
 */
export const BIOME_Y_OFFSETS: Readonly<Record<string, Readonly<Partial<Record<BiomeYOffsetTarget, number>>>>> = {
    meadow: { hills: 60, exitPortal: 60, transport: 60, nubi: 80 }
}

export function biomeYOffset(biome: string | undefined, target: BiomeYOffsetTarget): number {
    return (biome ? BIOME_Y_OFFSETS[biome]?.[target] : undefined) ?? 0
}

/**
 * Tamaño (lado mayor, en px lógicos del canvas de 1280x720) al que se reduce cada elemento interactivo del
 * bioma meadow (mariposas, daisy, cofre...; ver `InteractiveLayer`/`InteractiveElementAnimations`). Los assets
 * llegan en tamaños nativos muy dispares (de 400 a 2500 px) y nunca se amplían por encima de su tamaño nativo,
 * solo se reducen. La zona táctil del elemento se calcula a partir de este mismo valor (+30px de margen).
 *
 * Para ajustar el tamaño de un elemento, cambia su valor aquí; no hace falta tocar ningún otro fichero.
 * Referencia de otros elementos del mapa ya calibrados: el transporte y el portal de salida miden ~128, Nubi se
 * ve a ~160.
 */
export const MEADOW_ELEMENT_SIZE: Readonly<Record<string, number>> = {
    'butterfly-1': 64,
    'butterfly-2': 64,
    'butterfly-3': 64,
    daisy: 90,
    chest: 130,
    mushroom: 80,
    beehive: 130,
    burrow: 110,
    door: 150,
    swing: 170,
    worn: 120,
    shaperbox: 130,
    rainbow: 220,
    stump: 120
}
