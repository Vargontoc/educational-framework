import { ref, onMounted, onUnmounted } from 'vue'

/**
 * useInactivityDetector - Detecta inactividad del usuario
 * 
 * Características:
 * - Monitoriza eventos: mouse, keyboard, touch, scroll
 * - Timeout configurable (default: 5 minutos)
 * - Callback cuando se detecta inactividad
 * - Cleanup automático de event listeners
 */

export interface InactivityDetectorOptions {
  /** Timeout de inactividad en milisegundos (default: 300000 = 5 min) */
  timeout?: number
  /** Callback cuando se detecta inactividad */
  onInactive?: () => void
  /** Callback cuando el usuario vuelve a estar activo */
  onActive?: () => void
  /** Activar detector automáticamente */
  autoStart?: boolean
}

export function useInactivityDetector(options: InactivityDetectorOptions = {}) {
  const {
    timeout = 300000, // 5 minutos
    onInactive,
    onActive,
    autoStart = true
  } = options

  const isActive = ref(true)
  const lastActivity = ref(Date.now())
  let inactivityTimer: ReturnType<typeof setTimeout> | null = null
  let checkInterval: ReturnType<typeof setInterval> | null = null

  const events = ['mousedown', 'mousemove', 'keydown', 'touchstart', 'scroll']

  function resetTimer() {
    if (inactivityTimer) {
      clearTimeout(inactivityTimer)
    }
    
    lastActivity.value = Date.now()
    
    if (!isActive.value) {
      isActive.value = true
      onActive?.()
    }
    
    inactivityTimer = setTimeout(() => {
      isActive.value = false
      onInactive?.()
    }, timeout)
  }

  function start() {
    resetTimer()
    
    // Check interval para actualizar lastActivity
    checkInterval = setInterval(() => {
      const elapsed = Date.now() - lastActivity.value
      if (elapsed >= timeout && isActive.value) {
        isActive.value = false
        onInactive?.()
      }
    }, 1000)
  }

  function stop() {
    if (inactivityTimer) {
      clearTimeout(inactivityTimer)
      inactivityTimer = null
    }
    
    if (checkInterval) {
      clearInterval(checkInterval)
      checkInterval = null
    }
  }

  function addEventListeners() {
    events.forEach(event => {
      window.addEventListener(event, resetTimer, { passive: true })
    })
  }

  function removeEventListeners() {
    events.forEach(event => {
      window.removeEventListener(event, resetTimer)
    })
  }

  onMounted(() => {
    if (autoStart) {
      addEventListeners()
      start()
    }
  })

  onUnmounted(() => {
    removeEventListeners()
    stop()
  })

  return {
    isActive,
    lastActivity,
    start: () => {
      addEventListeners()
      start()
    },
    stop: () => {
      removeEventListeners()
      stop()
    },
    reset: resetTimer
  }
}
