import { AudioContextManager } from './AudioContextManager'

/**
 * Decodifica ArrayBuffer a AudioBuffer usando Web Audio API.
 */
export class AudioDecoder {
  private audioContextManager: AudioContextManager

  constructor() {
    this.audioContextManager = AudioContextManager.getInstance()
  }

  /**
   * Decodifica datos de audio (MP3, WAV, etc.) a AudioBuffer.
   */
  async decode(audioData: ArrayBuffer): Promise<AudioBuffer> {
    const ctx = await this.audioContextManager.getAudioContext()
    return await ctx.decodeAudioData(audioData.slice(0))
  }

  /**
   * Libera recursos del decodificador.
   */
  dispose(): void {
    this.audioContextManager.dispose()
  }
}
