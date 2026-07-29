import { ref, onUnmounted } from 'vue'

const INACTIVITY_TIMEOUT = 180000 // 3 minutes

export function useInactivityTimer(onExpire: () => void) {
  const isActive = ref(false)
  const remainingTime = ref(INACTIVITY_TIMEOUT)
  const isPaused = ref(false)

  let timer: ReturnType<typeof setTimeout> | null = null
  let interval: ReturnType<typeof setInterval> | null = null
  let lastTick = 0

  const events = ['mousemove', 'keydown', 'touchstart', 'click', 'scroll'] as const

  function clearTimers() {
    if (timer) {
      clearTimeout(timer)
      timer = null
    }
    if (interval) {
      clearInterval(interval)
      interval = null
    }
  }

  function startCountdown() {
    clearTimers()
    remainingTime.value = INACTIVITY_TIMEOUT
    lastTick = Date.now()

    interval = setInterval(() => {
      if (isPaused.value) return
      const elapsed = Date.now() - lastTick
      remainingTime.value = Math.max(0, INACTIVITY_TIMEOUT - elapsed)
    }, 1000)

    timer = setTimeout(() => {
      if (!isPaused.value) {
        stop()
        onExpire()
      }
    }, INACTIVITY_TIMEOUT)
  }

  function handleActivity() {
    if (!isActive.value || isPaused.value) return
    startCountdown()
  }

  function start() {
    if (isActive.value) return
    isActive.value = true
    isPaused.value = false
    events.forEach((event) => {
      window.addEventListener(event, handleActivity, { passive: true })
    })
    startCountdown()
  }

  function stop() {
    isActive.value = false
    isPaused.value = false
    events.forEach((event) => {
      window.removeEventListener(event, handleActivity)
    })
    clearTimers()
  }

  function pause() {
    if (!isActive.value || isPaused.value) return
    isPaused.value = true
    if (timer) {
      clearTimeout(timer)
      timer = null
    }
  }

  function resume() {
    if (!isActive.value || !isPaused.value) return
    isPaused.value = false
    const elapsed = INACTIVITY_TIMEOUT - remainingTime.value
    const newTimeout = Math.max(0, INACTIVITY_TIMEOUT - elapsed)
    lastTick = Date.now()

    timer = setTimeout(() => {
      if (!isPaused.value) {
        stop()
        onExpire()
      }
    }, newTimeout)
  }

  function reset() {
    if (!isActive.value) return
    startCountdown()
  }

  onUnmounted(() => {
    stop()
  })

  return {
    isActive,
    remainingTime,
    isPaused,
    start,
    stop,
    pause,
    resume,
    reset
  }
}
