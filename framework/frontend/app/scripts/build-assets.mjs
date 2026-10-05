/**
 * Pipeline de assets del juego: convierte los originales (assets-src/) a WebP y los reduce al tamaño
 * con el que realmente se dibujan, para no agotar la memoria de GPU de moviles y tablets.
 *
 * Por que reducir y no solo recomprimir: la GPU guarda la imagen descomprimida (ancho x alto x 4 bytes),
 * asi que WebP solo reduce la descarga; lo que evita las texturas en negro por falta de memoria son las
 * dimensiones. El canvas de Phaser es fijo (1280x720 logicos, ver GameView.vue), por lo que una textura
 * no necesita mas pixeles que su tamano logico por un margen (ASSET_MARGIN).
 *
 * Entrada:  assets-manifest.src.json (versionado) + originales en assets-src/<ruta de la url sin '/assets/'>
 * Salida:   public/assets/<ruta>.webp  y  public/assets-manifest.json (generados, no se versionan)
 *
 * - Solo se procesan las entradas del manifiesto cuyo original esta en assets-src/. El resto se copia tal cual.
 * - Nunca modifica ni borra los originales (public/assets esta fuera de git: no hay copia de seguridad).
 * - Es incremental: una entrada no cambiada no se vuelve a procesar.
 *
 * Uso: npm run assets   (se ejecuta solo antes de `dev` y `build`)
 */
import sharp from 'sharp'
import { fileURLToPath } from 'node:url'
import { dirname, join, extname } from 'node:path'
import { mkdir, readFile, writeFile, stat } from 'node:fs/promises'

const ROOT = join(dirname(fileURLToPath(import.meta.url)), '..')
const SRC_DIR = join(ROOT, 'assets-src')
const PUBLIC_DIR = join(ROOT, 'public')
const MANIFEST_SRC = join(ROOT, 'assets-manifest.src.json')
const MANIFEST_OUT = join(PUBLIC_DIR, 'assets-manifest.json')
const WORLD_MAP_CONFIG = join(ROOT, 'src/game/worldmap/config/worldMapConfig.ts')
const CACHE_FILE = join(ROOT, 'node_modules/.cache/build-assets.json')

/** Margen sobre el tamano logico de dibujo (animaciones de pulsacion que escalan >1, zoom del mapa...). */
const ASSET_MARGIN = 1.5
const WEBP = { quality: 82, alphaQuality: 100, effort: 5 }
const WEBP_LOSSLESS = { lossless: true, effort: 5 }

/**
 * Reglas por pack para lo que no es un elemento del prado (ese se dimensiona con MEADOW_ELEMENT_SIZE).
 * - maxSide: lado mayor maximo en px; solo reduce, nunca amplia. Las opciones de reconocimiento se dibujan
 *   como mucho a ~480 px logicos (ver ResponsiveLayout.ts: comparacion 240 CSS x escala 2), asi que 512 basta.
 * - lossless: trazo plano (letras, numeros, formas) donde la compresion con perdida dejaria bordes sucios.
 * Sin regla (p. ej. el pack `dev`): solo se convierte a WebP, sin reescalar (BiomeSelectorLayer depende de las
 * dimensiones nativas de algunos assets).
 */
const PACK_RULES = [
    { test: /^recognition-(letters|numbers|shapes)$/, maxSide: 512, lossless: true },
    { test: /^recognition-/, maxSide: 512 }
]

function ruleFor(packName) {
    return PACK_RULES.find((rule) => rule.test.test(packName)) ?? {}
}

/**
 * Tamano logico (lado mayor) de cada elemento del prado. Fuente unica: MEADOW_ELEMENT_SIZE en worldMapConfig.ts.
 * Se lee del propio fichero para no duplicar los numeros; si el formato cambia, el script falla en vez de adivinar.
 */
async function readMeadowElementSizes() {
    const source = await readFile(WORLD_MAP_CONFIG, 'utf8')
    const block = source.match(/MEADOW_ELEMENT_SIZE[^=]*=\s*\{([^}]*)\}/)
    if (!block) {
        throw new Error('build-assets: no se encuentra MEADOW_ELEMENT_SIZE en worldMapConfig.ts')
    }
    const sizes = {}
    for (const [, key, value] of block[1].matchAll(/['"]?([\w-]+)['"]?\s*:\s*(\d+)/g)) {
        sizes[key] = Number(value)
    }
    if (Object.keys(sizes).length === 0) {
        throw new Error('build-assets: MEADOW_ELEMENT_SIZE esta vacio o no se pudo interpretar')
    }
    return sizes
}

async function exists(path) {
    try {
        await stat(path)
        return true
    } catch {
        return false
    }
}

async function loadCache() {
    try {
        return JSON.parse(await readFile(CACHE_FILE, 'utf8'))
    } catch {
        return {}
    }
}

/** Reduccion (nunca ampliacion) para que el lado mayor del elemento visible mida `target`. */
function scaleFor(longestSide, target) {
    if (!target) return 1
    return Math.min(1, target / longestSide)
}

async function processEntry(entry, packName, sizes, cache, report) {
    // Las URLs del manifiesto aparecen con y sin barra inicial: se conserva el estilo original al reescribirlas.
    const leadingSlash = entry.url.startsWith('/')
    const relative = entry.url.replace(/^\/?assets\//, '')
    const masterPath = join(SRC_DIR, relative)
    if (!(await exists(masterPath))) {
        return entry // sin original en assets-src: se deja tal cual (se sirve desde public/ como antes)
    }

    const outRelative = relative.replace(new RegExp(`${extname(relative)}$`), '.webp')
    const outPath = join(PUBLIC_DIR, 'assets', outRelative)
    const outUrl = `${leadingSlash ? '/' : ''}assets/${outRelative}`

    const meta = await sharp(masterPath).metadata()
    const isSheet = entry.type === 'spritesheet'
    const frame = isSheet ? entry.frameConfig : undefined
    const rule = ruleFor(packName)
    const logicalSize = packName === 'biome-meadow' ? sizes[entry.key.replace(/-anim$/, '')] : undefined
    const target = logicalSize ? Math.ceil(logicalSize * ASSET_MARGIN) : rule.maxSide
    const webp = rule.lossless ? WEBP_LOSSLESS : WEBP

    let width = meta.width
    let height = meta.height
    let newFrame
    if (isSheet) {
        const { frameWidth, frameHeight } = frame
        if (meta.width % frameWidth !== 0 || meta.height % frameHeight !== 0) {
            throw new Error(`build-assets: ${entry.key}: ${meta.width}x${meta.height} no es multiplo del fotograma ${frameWidth}x${frameHeight}`)
        }
        const f = scaleFor(Math.max(frameWidth, frameHeight), target)
        const cols = meta.width / frameWidth
        const rows = meta.height / frameHeight
        newFrame = { frameWidth: Math.round(frameWidth * f), frameHeight: Math.round(frameHeight * f) }
        width = cols * newFrame.frameWidth
        height = rows * newFrame.frameHeight
    } else {
        const f = scaleFor(Math.max(meta.width, meta.height), target)
        width = Math.round(meta.width * f)
        height = Math.round(meta.height * f)
    }

    const master = await stat(masterPath)
    const signature = `${master.mtimeMs}:${master.size}:${width}x${height}:${rule.lossless ? 'lossless' : WEBP.quality}`
    if (cache[outPath] !== signature || !(await exists(outPath))) {
        await mkdir(dirname(outPath), { recursive: true })
        await sharp(masterPath)
            .resize(width, height, { fit: 'fill', kernel: 'lanczos3' })
            .webp(webp)
            .toFile(outPath)
        cache[outPath] = signature
    }

    const out = await stat(outPath)
    report.push({
        key: entry.key,
        before: `${meta.width}x${meta.height}`,
        after: `${width}x${height}`,
        gpuBefore: meta.width * meta.height * 4,
        gpuAfter: width * height * 4,
        diskBefore: master.size,
        diskAfter: out.size
    })

    const updated = { ...entry, url: outUrl }
    if (newFrame) updated.frameConfig = { ...frame, ...newFrame }
    return updated
}

const mb = (bytes) => (bytes / 1e6).toFixed(1)

async function main() {
    const sizes = await readMeadowElementSizes()
    const cache = await loadCache()
    const manifest = JSON.parse(await readFile(MANIFEST_SRC, 'utf8'))
    const report = []

    for (const [packName, pack] of Object.entries(manifest)) {
        if (!Array.isArray(pack.files)) continue
        pack.files = await Promise.all(
            pack.files.map((entry) =>
                entry.type === 'image' || entry.type === 'spritesheet'
                    ? processEntry(entry, packName, sizes, cache, report)
                    : entry
            )
        )
    }

    await mkdir(dirname(CACHE_FILE), { recursive: true })
    await writeFile(CACHE_FILE, JSON.stringify(cache))
    await writeFile(MANIFEST_OUT, JSON.stringify(manifest, null, 4))

    const sum = (field) => report.reduce((total, row) => total + row[field], 0)
    console.log(`build-assets: ${report.length} assets procesados`)
    for (const row of report.filter((r) => r.before !== r.after)) {
        console.log(`  ${row.key.padEnd(28)} ${row.before.padStart(10)} -> ${row.after.padStart(10)}   GPU ${mb(row.gpuBefore)} -> ${mb(row.gpuAfter)} MB`)
    }
    console.log(`  total GPU  ${mb(sum('gpuBefore'))} -> ${mb(sum('gpuAfter'))} MB`)
    console.log(`  total disco ${mb(sum('diskBefore'))} -> ${mb(sum('diskAfter'))} MB`)
}

main().catch((error) => {
    console.error(error)
    process.exit(1)
})
