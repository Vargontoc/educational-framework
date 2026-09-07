import { ref, onMounted, onUnmounted } from 'vue'

export function useGameOrientation() {
  const isPortrait = ref(false)

  function updateOrientation() {
    isPortrait.value = window.innerHeight > window.innerWidth
  }

  let resizeTimeout: ReturnType<typeof setTimeout> | null = null

  function handleResize() {
    if (resizeTimeout !== null) {
      clearTimeout(resizeTimeout)
    }
    resizeTimeout = setTimeout(() => {
      updateOrientation()
      resizeTimeout = null
    }, 150)
  }

  function handleOrientationChange() {
    updateOrientation()
  }

  onMounted(() => {
    updateOrientation()
    window.addEventListener('resize', handleResize)
    window.addEventListener('orientationchange', handleOrientationChange)
    if (screen.orientation) {
      screen.orientation.addEventListener('change', handleOrientationChange)
    }
  })

  onUnmounted(() => {
    if (resizeTimeout !== null) {
      clearTimeout(resizeTimeout)
    }
    window.removeEventListener('resize', handleResize)
    window.removeEventListener('orientationchange', handleOrientationChange)
    if (screen.orientation) {
      screen.orientation.removeEventListener('change', handleOrientationChange)
    }
  })

  return { isPortrait }
}
