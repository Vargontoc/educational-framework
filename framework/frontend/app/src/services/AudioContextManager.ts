/**
 * Gestiona el AudioContext global y resuelve problemas de autoplay.
 * Patrón Singleton para garantizar una única instancia de AudioContext.
 */
export class AudioContextManager {
  private static instance: AudioContextManager | null = null
  private audioContext: AudioContext | null = null
  private unlockListeners: Array<() => void> = []

  private constructor() {}

  static getInstance(): AudioContextManager {
    if (!AudioContextManager.instance) {
      AudioContextManager.instance = new AudioContextManager()
    }
    return AudioContextManager.instance
  }

  /**
   * Obtiene el AudioContext global, creándolo si es necesario.
   */
  async getAudioContext(): Promise<AudioContext> {
    if (!this.audioContext) {
      const AudioContextClass = window.AudioContext ?? (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext
      this.audioContext = new AudioContextClass()
      this.setupUnlockListeners()
    }
    return this.audioContext
  }

  /**
   * Resume el AudioContext si está suspendido (política de autoplay).
   */
  async resumeIfNeeded(): Promise<void> {
    const ctx = await this.getAudioContext()
    if (ctx.state === 'suspended') {
      await ctx.resume()
    }
  }

  /**
   * Registra listeners de interacción para desbloquear el audio.
   */
  private setupUnlockListeners(): void {
    const unlock = (): void => {
      if (this.audioContext && this.audioContext.state === 'suspended') {
        void this.audioContext.resume()
      }
    }

    const onInteraction = (): void => {
      void unlock()
      this.removeUnlockListeners()
    }

    const handlers: Array<[string, () => void]> = [
      ['click', onInteraction],
      ['touchstart', onInteraction],
      ['keydown', onInteraction],
    ]

    for (const [event, handler] of handlers) {
      document.addEventListener(event, handler, { once: false, passive: true })
      this.unlockListeners.push(() => document.removeEventListener(event, handler))
    }
  }

  private removeUnlockListeners(): void {
    for (const remove of this.unlockListeners) {
      remove()
    }
    this.unlockListeners = []
  }

  /**
   * Libera recursos del AudioContextManager.
   */
  dispose(): void {
    this.removeUnlockListeners()
    if (this.audioContext) {
      void this.audioContext.close()
      this.audioContext = null
    }
    AudioContextManager.instance = null
  }
}
