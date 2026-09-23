import type { Scene } from "phaser"
import type { RECOGNITION_TYPE, RecognitionElement } from "../GameEvent"

interface ManifestFile {
    type: string
    key: string
    url: string
}

type Manifest = Record<string, { files?: ManifestFile[] }>

interface AssetEntry {
    key: string
    url: string
}

const MANIFEST_URL = '/assets-manifest.json'
const DEFAULT_KEEP_RECENT = 3
const COLOR_BLOCK_PREFIX = 'recognition-color-'
const COLOR_CODE_PREFIX = 'color_'
const SPLASH_FILE = 'splash'
const ITEM_FILE_PATTERN = /^item_\d+$/

const PACK_KEYS: Partial<Record<RECOGNITION_TYPE, string>> = {
    LETTER: 'recognition-letters',
    NUMBER: 'recognition-numbers',
    SHAPE: 'recognition-shapes',
    ANIMAL: 'recognition-animals',
    COMPARISON: 'recognition-comparison',
    MEMORY: 'recognition-memory'
}

/** Texturas de las cartas del juego de memoria: las mismas para todas las partidas. */
export const MEMORY_CARD_COVER_KEY = 'memory-card-cover'
export const MEMORY_CARD_REVERSE_KEY = 'memory-card-reverse'

let manifestPromise: Promise<Manifest> | undefined

function fetchManifest(): Promise<Manifest> {
    if (!manifestPromise) {
        manifestPromise = fetch(MANIFEST_URL)
            .then(res => {
                if (!res.ok) throw new Error(`manifest ${res.status}`)
                return res.json() as Promise<Manifest>
            })
            .catch(error => {
                manifestPromise = undefined
                throw error
            })
    }
    return manifestPromise
}

function shuffled<T>(items: T[]): T[] {
    const copy = [...items]
    for (let i = copy.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1))
        ;[copy[i], copy[j]] = [copy[j], copy[i]]
    }
    return copy
}

/**
 * Carga por ronda, solo lo que falta, de las texturas de una categoria del minijuego.
 * COLOR carga por bloque (`recognition-color-[color]`): el splash de cada opcion y un
 * item comun a todas ellas; sus texturas se prefijan con el bloque (`recognition-color-red/splash`)
 * porque todos los bloques repiten las claves `splash` e `item_N`.
 */
export class DynamicAssetLoader {
    private scene: Scene
    private inflight = new Map<string, Promise<void>>()
    private ownedKeys = new Set<string>()
    private roundKeys: string[][] = []
    private preloadedColorRounds = new Map<string, Promise<string | null>>()

    constructor(scene: Scene) {
        this.scene = scene
    }

    /**
     * LETTER, NUMBER, ANIMAL y SHAPE usan el `code` como key de textura; COLOR, el splash de su bloque;
     * COMPARISON y MEMORY, `resourceRefs['image']` (o el `code`); el resto, `resourceRefs['image']`.
     */
    static textureKey(element: RecognitionElement, category: RECOGNITION_TYPE | null | undefined): string | null {
        if (category === 'COLOR') {
            const block = DynamicAssetLoader.colorBlock(element)
            return block ? DynamicAssetLoader.colorSplashKey(block) : null
        }
        if (category === 'LETTER' || category === 'NUMBER' || category === 'ANIMAL' || category === 'SHAPE') return element.code || null
        if (category === 'COMPARISON' || category === 'MEMORY') return element.resourceRefs?.['image'] ?? (element.code || null)
        return element.resourceRefs?.['image'] ?? null
    }

    /** `code: "color_red"` -> bloque `recognition-color-red`. */
    static colorBlock(element: RecognitionElement): string | null {
        const code = element.code
        if (!code || !code.startsWith(COLOR_CODE_PREFIX)) return null
        return COLOR_BLOCK_PREFIX + code.slice(COLOR_CODE_PREFIX.length)
    }

    static colorSplashKey(block: string): string {
        return `${block}/${SPLASH_FILE}`
    }

    static colorItemKey(block: string, item: string): string {
        return `${block}/${item}`
    }

    /** Pide el manifest por adelantado para que la primera ronda no espere a esa peticion. Nunca rechaza. */
    static warmUp(): void {
        fetchManifest().catch(() => undefined)
    }

    /** Carga las texturas de la ronda que aun no estan en cache. Nunca rechaza. */
    async loadRoundAssets(items: RecognitionElement[], category: RECOGNITION_TYPE): Promise<void> {
        if (category === 'COLOR') {
            await this.loadColorRound(items)
            return
        }
        const keys = this.collectKeys(items, category)
        this.trackRound(keys)
        await this.ensureLoaded(keys, category)
    }

    /** Precarga en segundo plano; un fallo no afecta a la partida. */
    preloadNextRound(nextItems: RecognitionElement[], category: RECOGNITION_TYPE): Promise<void> {
        if (category === 'COLOR') {
            return this.preloadColorRound(nextItems)
        }
        const keys = this.collectKeys(nextItems, category)
        this.trackRound(keys)
        return this.ensureLoaded(keys, category)
    }

    /**
     * COLOR: carga el splash de cada bloque de la ronda y elige un item comun a todos ellos.
     * @returns la clave del item elegido (`item_2`), o `null` si ningun item se pudo cargar en todos los bloques
     */
    async loadColorRound(items: RecognitionElement[]): Promise<string | null> {
        const signature = this.colorRoundSignature(items)
        const preloaded = this.preloadedColorRounds.get(signature)
        if (preloaded) {
            this.preloadedColorRounds.delete(signature)
            return preloaded
        }
        return this.resolveColorRound(items)
    }

    private preloadColorRound(items: RecognitionElement[]): Promise<void> {
        const signature = this.colorRoundSignature(items)
        // Keep the choice so the round that follows reuses the item already loaded for it.
        const round = this.resolveColorRound(items)
        this.preloadedColorRounds.set(signature, round)
        return round.then(() => undefined)
    }

    /**
     * Libera las texturas cargadas por este loader que no pertenecen a las
     * `keepRecent` rondas mas recientes. Con 0 libera todas.
     */
    cleanupOldTextures(keepRecent: number = DEFAULT_KEEP_RECENT): void {
        const kept = keepRecent > 0 ? this.roundKeys.slice(-keepRecent) : []
        const keepKeys = new Set(kept.flat())

        this.ownedKeys.forEach(key => {
            if (keepKeys.has(key)) return
            if (this.scene.textures.exists(key)) {
                this.scene.textures.remove(key)
            }
            this.ownedKeys.delete(key)
            this.inflight.delete(key)
        })
        this.roundKeys = kept
        if (keepRecent <= 0) this.preloadedColorRounds.clear()
    }

    private colorBlocks(items: RecognitionElement[]): string[] {
        const blocks = items
            .map(item => DynamicAssetLoader.colorBlock(item))
            .filter((block): block is string => !!block)
        return [...new Set(blocks)]
    }

    private colorRoundSignature(items: RecognitionElement[]): string {
        return this.colorBlocks(items).sort().join('|')
    }

    private async resolveColorRound(items: RecognitionElement[]): Promise<string | null> {
        const blocks = this.colorBlocks(items)
        if (blocks.length === 0) return null

        let manifest: Manifest
        try {
            manifest = await fetchManifest()
        } catch (error) {
            console.warn('Asset manifest not available, using placeholders:', error)
            return null
        }

        const entryFor = (block: string, file: string): AssetEntry | null => {
            const found = manifest[block]?.files?.find(f => f.key === file && f.type === 'image')
            return found ? { key: `${block}/${file}`, url: found.url } : null
        }

        const splashes = blocks
            .map(block => entryFor(block, SPLASH_FILE))
            .filter((entry): entry is AssetEntry => !!entry)
        await this.ensureFilesLoaded(splashes)

        for (const item of this.commonItems(blocks, manifest)) {
            const entries = blocks.map(block => entryFor(block, item))
            if (entries.some(entry => !entry)) continue

            const itemEntries = entries as AssetEntry[]
            await this.ensureFilesLoaded(itemEntries)
            if (itemEntries.every(entry => this.scene.textures.exists(entry.key))) {
                this.trackRound([...splashes.map(s => s.key), ...itemEntries.map(e => e.key)])
                return item
            }
        }

        this.trackRound(splashes.map(s => s.key))
        return null
    }

    /**
     * Items (`item_N`) presentes en TODOS los bloques de la ronda, en orden aleatorio: los bloques
     * no tienen los mismos items, y el mismo item ha de poder pintarse en cada opcion.
     */
    private commonItems(blocks: string[], manifest: Manifest): string[] {
        const perBlock = blocks.map(block =>
            (manifest[block]?.files ?? [])
                .filter(f => f.type === 'image' && ITEM_FILE_PATTERN.test(f.key))
                .map(f => f.key))
        if (perBlock.length === 0) return []

        const common = perBlock[0].filter(item => perBlock.every(list => list.includes(item)))
        return shuffled(common)
    }

    private collectKeys(items: RecognitionElement[], category: RECOGNITION_TYPE): string[] {
        const keys = items
            .map(item => DynamicAssetLoader.textureKey(item, category))
            .filter((key): key is string => !!key)
        // The memory cards (cover and face) go with the pack of the elements they show.
        if (category === 'MEMORY') keys.push(MEMORY_CARD_COVER_KEY, MEMORY_CARD_REVERSE_KEY)
        return [...new Set(keys)]
    }

    private trackRound(keys: string[]): void {
        const last = this.roundKeys[this.roundKeys.length - 1]
        const sameAsLast = last && last.length === keys.length && last.every(k => keys.includes(k))
        if (!sameAsLast) this.roundKeys.push(keys)
    }

    private async ensureLoaded(keys: string[], category: RECOGNITION_TYPE): Promise<void> {
        const packKey = PACK_KEYS[category]
        if (!packKey || keys.length === 0) return

        let manifest: Manifest
        try {
            manifest = await fetchManifest()
        } catch (error) {
            console.warn('Asset manifest not available, using placeholders:', error)
            return
        }

        const files = manifest[packKey]?.files ?? []
        const entries: AssetEntry[] = []
        for (const key of keys) {
            const file = files.find(f => f.key === key && f.type === 'image')
            if (file) {
                entries.push({ key, url: file.url })
            } else if (!this.scene.textures.exists(key) && !this.inflight.has(key)) {
                console.warn('Texture not in manifest, using placeholder:', key)
            }
        }
        await this.ensureFilesLoaded(entries)
    }

    private async ensureFilesLoaded(entries: AssetEntry[]): Promise<void> {
        const pending: Promise<void>[] = []
        let queued = false

        for (const { key, url } of entries) {
            const inflight = this.inflight.get(key)
            if (inflight) {
                pending.push(inflight)
                continue
            }
            if (this.scene.textures.exists(key)) continue

            const promise = this.queueImage(key, url)
            this.inflight.set(key, promise)
            this.ownedKeys.add(key)
            pending.push(promise)
            queued = true
        }

        if (queued) this.startLoader()
        await Promise.all(pending)
    }

    private startLoader(): void {
        const load = this.scene.load
        if (load.isLoading()) return // files added mid-load are picked up by the running batch

        if (load.isReady()) {
            load.start()
            return
        }

        // Loader is finishing a previous batch: start again once it completes.
        load.once('complete', () => {
            if (load.isReady()) load.start()
        })
    }

    private queueImage(key: string, url: string): Promise<void> {
        const load = this.scene.load
        return new Promise<void>(resolve => {
            const done = () => {
                load.off(`filecomplete-image-${key}`, done)
                load.off('loaderror', onError)
                load.off('complete', onBatchComplete)
                this.inflight.delete(key)
                resolve()
            }
            const fail = () => {
                console.warn('Texture failed to load, using placeholder:', key)
                this.ownedKeys.delete(key)
                done()
            }
            const onError = (file: { key: string }) => {
                if (file.key === key) fail()
            }
            // A 200 response that is not a valid image (e.g. SPA fallback HTML for a missing file)
            // emits neither `filecomplete` nor `loaderror`: only the batch `complete` tells us it is over.
            const onBatchComplete = () => {
                const pendingFiles = load.list.size + load.inflight.size
                if (pendingFiles > 0) return
                if (this.scene.textures.exists(key)) done()
                else fail()
            }
            load.on(`filecomplete-image-${key}`, done)
            load.on('loaderror', onError)
            load.on('complete', onBatchComplete)
            load.image(key, `/${url.replace(/^\//, '')}`)
        })
    }
}
