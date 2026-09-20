import type { AudioCache } from '@/services/AudioCache'

/**
 * Registro del audio de ronda (ROUND_PROMPT) de una escena de minijuego.
 * Los AudioBuffer viven en el AudioCache compartido; aqui solo se recuerda
 * que ids pertenecen a la escena para poder repetirlos y liberarlos al salir.
 */
export class RoundAudioCache {
    private audioCache?: AudioCache
    private ids = new Set<string>()
    private currentId?: string

    constructor(audioCache?: AudioCache) {
        this.audioCache = audioCache
    }

    setCurrent(audioId?: string): void {
        this.currentId = audioId
        if (audioId) this.ids.add(audioId)
    }

    getCurrentId(): string | undefined {
        return this.currentId
    }

    isCached(audioId: string): boolean {
        return this.audioCache?.has(audioId) ?? false
    }

    clear(): void {
        this.ids.forEach(id => this.audioCache?.delete(id))
        this.ids.clear()
        this.currentId = undefined
    }
}
