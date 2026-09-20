import type { Scene } from "phaser"
import type { RECOGNITION_TYPE, RecognitionElement } from "../GameEvent"

interface ManifestFile {
    type: string
    key: string
    url: string
}

type Manifest = Record<string, { files?: ManifestFile[] }>

const MANIFEST_URL = '/assets-manifest.json'
const DEFAULT_KEEP_RECENT = 3

const PACK_KEYS: Partial<Record<RECOGNITION_TYPE, string>> = {
    LETTER: 'recognition-letters',
    NUMBER: 'recognition-numbers',
    SHAPE: 'recognition-shapes',
    ANIMAL: 'recognition-animals'
}

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

/**
 * Carga por ronda, solo lo que falta, de las texturas de una categoria del minijuego.
 * COLOR no pasa por aqui: sus texturas se generan en runtime.
 */
export class DynamicAssetLoader {
    private scene: Scene
    private inflight = new Map<string, Promise<void>>()
    private ownedKeys = new Set<string>()
    private roundKeys: string[][] = []

    constructor(scene: Scene) {
        this.scene = scene
    }

    /** LETTER y NUMBER usan el `code` como key de textura; el resto, `resourceRefs['image']`. */
    static textureKey(element: RecognitionElement, category: RECOGNITION_TYPE | null | undefined): string | null {
        if (category === 'COLOR') return null
        if (category === 'LETTER' || category === 'NUMBER') return element.code || null
        return element.resourceRefs?.['image'] ?? null
    }

    /** Carga las texturas de la ronda que aun no estan en cache. Nunca rechaza. */
    async loadRoundAssets(items: RecognitionElement[], category: RECOGNITION_TYPE): Promise<void> {
        const keys = this.collectKeys(items, category)
        this.trackRound(keys)
        await this.ensureLoaded(keys, category)
    }

    /** Precarga en segundo plano; un fallo no afecta a la partida. */
    preloadNextRound(nextItems: RecognitionElement[], category: RECOGNITION_TYPE): Promise<void> {
        const keys = this.collectKeys(nextItems, category)
        this.trackRound(keys)
        return this.ensureLoaded(keys, category)
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
    }

    private collectKeys(items: RecognitionElement[], category: RECOGNITION_TYPE): string[] {
        const keys = items
            .map(item => DynamicAssetLoader.textureKey(item, category))
            .filter((key): key is string => !!key)
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
        const pending: Promise<void>[] = []
        let queued = false

        for (const key of keys) {
            const inflight = this.inflight.get(key)
            if (inflight) {
                pending.push(inflight)
                continue
            }
            if (this.scene.textures.exists(key)) continue

            const file = files.find(f => f.key === key && f.type === 'image')
            if (!file) {
                console.warn('Texture not in manifest, using placeholder:', key)
                continue
            }

            const promise = this.queueImage(key, file.url)
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
