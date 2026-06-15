import { ref } from 'vue'
import { defineStore } from 'pinia'

export const useAudioStore = defineStore('audio', () => {
  const isPlaying = ref(false)

  let _queue: Promise<void> = Promise.resolve()

  function _playNow(mp3Data: ArrayBuffer, timeoutMs: number): Promise<void> {
    return new Promise((resolve, reject) => {
      isPlaying.value = true
      const blob = new Blob([mp3Data], { type: 'audio/mpeg' })
      const url = URL.createObjectURL(blob)
      const audio = new Audio(url)

      const cleanup = () => {
        URL.revokeObjectURL(url)
        isPlaying.value = false
      }

      const timeoutId = setTimeout(() => {
        cleanup()
        reject(new Error('AUDIO_TIMEOUT'))
      }, timeoutMs)

      audio.addEventListener('ended', () => {
        clearTimeout(timeoutId)
        cleanup()
        resolve()
      }, { once: true })

      audio.addEventListener('error', () => {
        clearTimeout(timeoutId)
        cleanup()
        reject(new Error('AUDIO_PLAYBACK_ERROR'))
      }, { once: true })

      audio.play().catch((err) => {
        clearTimeout(timeoutId)
        cleanup()
        reject(err)
      })
    })
  }

  function playAudio(mp3Data: ArrayBuffer, timeoutMs = 30_000): Promise<void> {
    const next = _queue.then(() => _playNow(mp3Data, timeoutMs))
    _queue = next.catch(() => {})
    return next
  }

  return { isPlaying, playAudio }
})
