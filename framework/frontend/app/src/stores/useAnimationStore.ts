import { ref } from 'vue'
import { defineStore } from 'pinia'

export type AnimationState = 'idle' | 'waiting' | 'speaking' | 'error'

export const useAnimationStore = defineStore('animation', () => {
  const state = ref<AnimationState>('idle')

  function setWaiting() {
    state.value = 'waiting'
  }

  function setSpeaking() {
    state.value = 'speaking'
  }

  function setIdle() {
    state.value = 'idle'
  }

  function setError() {
    state.value = 'error'
  }

  function reset() {
    state.value = 'idle'
  }

  return { state, setWaiting, setSpeaking, setIdle, setError, reset }
})
