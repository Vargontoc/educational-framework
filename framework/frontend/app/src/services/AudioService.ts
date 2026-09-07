import Phaser from 'phaser'
import { BinaryFrameParser } from './BinaryFrameParser'
import { AudioContextManager } from './AudioContextManager'
import type { AudioCache } from './AudioCache'
import type { AudioDecoder } from './AudioDecoder'

/**
 * Configuración del AudioService.
 */
export interface AudioServiceConfig {
  scene: Phaser.Scene
  audioCache: AudioCache
  audioDecoder: AudioDecoder
}

interface QueueItem {
  type: 'static' | 'dynamic'
  key: string
}

/**
 * Módulo centralizado para reproducción de audio en Phaser.
 * Soporta audio estático (WAV cargado en Phaser cache) y audio dinámico (MP3 decodificado).
 */
export class AudioService extends Phaser.Events.EventEmitter {
  private scene: Phaser.Scene
  private audioCache: AudioCache
  private audioDecoder: AudioDecoder
  private queue: QueueItem[] = []
  private currentSound: Phaser.Sound.BaseSound | null = null
  private audioContextManager: AudioContextManager
  private dynamicSource: AudioBufferSourceNode | null = null
  private dynamicGainNode: GainNode | null = null
  private dynamicAudioId: string | null = null
  private dynamicOffset = 0
  private dynamicStartTime = 0
  private isPaused = false
  private disposed = false

  constructor(config: AudioServiceConfig) {
    super()
    this.scene = config.scene
    this.audioCache = config.audioCache
    this.audioDecoder = config.audioDecoder
    this.audioContextManager = AudioContextManager.getInstance()
  }

  /**
   * Reproduce audio estático (WAV cargado en Phaser cache).
   */
  playStatic(key: string): void {
    if (this.disposed) return
    this.stopCurrent()

    try {
      const sound = this.scene.sound.add(key)
      this.currentSound = sound
      this.dynamicOffset = 0
      this.isPaused = false

      sound.once('complete', () => {
        this.currentSound = null
        this.emit('audio-completed', key)
        this.playNext()
      })

      sound.once('error', () => {
        this.currentSound = null
        this.emit('audio-error', { key, error: 'Playback error' })
        this.playNext()
      })

      sound.play()
      this.emit('audio-started', key)
    } catch (error) {
      this.emit('audio-error', { key, error })
      this.playNext()
    }
  }

  /**
   * Reproduce audio dinámico (MP3 decodificado y almacenado en AudioCache).
   */
  async playDynamic(audioId: string): Promise<void> {
    if (this.disposed) return

    const audioBuffer = this.audioCache.get(audioId)
    if (!audioBuffer) {
      this.emit('audio-error', { key: audioId, error: 'Audio not found in cache' })
      this.playNext()
      return
    }

    this.stopCurrent()

    try {
      const ctx = await this.audioContextManager.getAudioContext()
      await this.audioContextManager.resumeIfNeeded()

      const source = ctx.createBufferSource()
      const gainNode = ctx.createGain()

      source.buffer = audioBuffer
      source.connect(gainNode)
      gainNode.connect(ctx.destination)

      this.dynamicSource = source
      this.dynamicGainNode = gainNode
      this.dynamicAudioId = audioId
      this.dynamicStartTime = ctx.currentTime
      this.isPaused = false

      source.onended = () => {
        if (!this.isPaused && this.dynamicSource === source) {
          this.dynamicSource = null
          this.dynamicGainNode = null
          this.dynamicAudioId = null
          this.dynamicOffset = 0
          this.emit('audio-completed', audioId)
          this.playNext()
        }
      }

      source.start(0, this.dynamicOffset)
      this.emit('audio-started', audioId)
    } catch (error) {
      this.dynamicSource = null
      this.dynamicGainNode = null
      this.dynamicOffset = 0
      this.emit('audio-error', { key: audioId, error })
      this.playNext()
    }
  }

  /**
   * Añade un elemento a la cola de reproducción.
   */
  enqueue(type: 'static' | 'dynamic', key: string): void {
    this.queue.push({ type, key })
  }

  /**
   * Reproduce el siguiente elemento de la cola.
   */
  playNext(): void {
    if (this.disposed) return
    if (this.isCurrentlyPlaying()) return

    const next = this.queue.shift()
    if (!next) return

    if (next.type === 'static') {
      this.playStatic(next.key)
    } else {
      void this.playDynamic(next.key)
    }
  }

  /**
   * Detiene la reproducción actual.
   */
  stop(): void {
    this.stopCurrent()
    this.dynamicOffset = 0
    this.queue = []
  }

  /**
   * Pausa la reproducción actual.
   */
  pause(): void {
    if (this.disposed) return

    if (this.currentSound && this.currentSound.isPlaying) {
      this.currentSound.pause()
      this.isPaused = true
    } else if (this.dynamicSource) {
      const ctx = this.dynamicSource.context as AudioContext
      this.dynamicOffset += ctx.currentTime - this.dynamicStartTime
      this.dynamicSource.onended = null
      this.dynamicSource.stop()
      this.dynamicSource.disconnect()
      this.dynamicSource = null
      this.isPaused = true
    }
  }

  /**
   * Reanuda la reproducción pausada.
   */
  resume(): void {
    if (this.disposed) return
    if (!this.isPaused) return

    if (this.currentSound && this.currentSound.isPaused) {
      this.currentSound.resume()
      this.isPaused = false
    } else if (this.dynamicAudioId) {
      const audioId = this.dynamicAudioId
      this.dynamicAudioId = null
      this.isPaused = false
      void this.playDynamic(audioId)
    }
  }

  /**
   * Indica si hay audio reproduciéndose actualmente.
   */
  isCurrentlyPlaying(): boolean {
    if (this.currentSound && this.currentSound.isPlaying) return true
    if (this.dynamicSource && !this.isPaused) return true
    return false
  }

  /**
   * Procesa un binary frame del WebSocket.
   * Extrae el audio, lo decodifica y lo almacena en la cache.
   */
  async handleBinaryFrame(buffer: ArrayBuffer): Promise<void> {
    if (this.disposed) return

    try {
      const { audioId, audioData } = BinaryFrameParser.parse(buffer)
      const audioBuffer = await this.audioDecoder.decode(audioData)
      this.audioCache.set(audioId, audioBuffer)
      this.emit('audio-received', audioId)
    } catch (error) {
      this.emit('audio-error', { key: 'binary-frame', error })
    }
  }

  /**
   * Detiene la reproducción actual sin limpiar la cola.
   */
  private stopCurrent(): void {
    if (this.currentSound) {
      this.currentSound.off('complete')
      this.currentSound.off('error')
      this.currentSound.stop()
      this.currentSound.destroy()
      this.currentSound = null
    }

    if (this.dynamicSource) {
      this.dynamicSource.onended = null
      try {
        this.dynamicSource.stop()
      } catch {
        // source may already be stopped
      }
      this.dynamicSource.disconnect()
      this.dynamicSource = null

      if (this.dynamicGainNode) {
        this.dynamicGainNode.disconnect()
        this.dynamicGainNode = null
      }
    }

    this.dynamicAudioId = null
    this.isPaused = false
  }

  /**
   * Libera todos los recursos del AudioService.
   */
  dispose(): void {
    this.disposed = true
    this.stopCurrent()
    this.dynamicOffset = 0
    this.queue = []
    this.removeAllListeners()
  }
}
