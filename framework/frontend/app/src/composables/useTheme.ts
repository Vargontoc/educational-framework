import { ref, watch } from 'vue'

/**
 * Composable para gestión de temas (claro/oscuro)
 *
 * Según ADR-017, ADR-018 y SPRINT-003/SPRINT-017:
 * - El modo oscuro es EXCLUSIVO del panel parental
 * - La experiencia infantil SIEMPRE usa tema claro
 * - La preferencia persiste en localStorage
 * - El tema NO se aplica a document.documentElement: cada contenedor
 *   lo aplica a su propio root (ParentPanelLayout) mediante :data-theme
 */

type Theme = 'light' | 'dark'

const STORAGE_KEY = 'nubi-theme-preference'

const currentTheme = ref<Theme>('light')
let isInitialized = false

function getStoredTheme(): Theme | null {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored === 'light' || stored === 'dark') {
      return stored
    }
  } catch (e) {
    console.warn('Error reading theme from localStorage:', e)
  }
  return null
}

function storeTheme(theme: Theme): void {
  try {
    localStorage.setItem(STORAGE_KEY, theme)
  } catch (e) {
    console.warn('Error saving theme to localStorage:', e)
  }
}

function initTheme(): void {
  if (isInitialized) return

  const storedTheme = getStoredTheme()
  const initialTheme = storedTheme || 'light'
  currentTheme.value = initialTheme

  watch(currentTheme, (newTheme) => {
    storeTheme(newTheme)
  })

  isInitialized = true
}

/**
 * Composable useTheme
 * 
 * @returns Objeto con el tema actual y funciones para cambiarlo
 */
export function useTheme() {
  // Inicializar tema al usar el composable
  if (!isInitialized) {
    initTheme()
  }

  /**
   * Cambia al tema oscuro
   */
  function setDarkMode(): void {
    currentTheme.value = 'dark'
  }

  /**
   * Cambia al tema claro
   */
  function setLightMode(): void {
    currentTheme.value = 'light'
  }

  /**
   * Alterna entre tema claro y oscuro
   */
  function toggleTheme(): void {
    currentTheme.value = currentTheme.value === 'light' ? 'dark' : 'light'
  }

  /**
   * Fuerza el tema claro (para experiencia infantil)
   */
  function forceLightMode(): void {
    currentTheme.value = 'light'
  }


  function getCurrentTheme() : string {
    return currentTheme.value
  }
  return {
    theme: currentTheme,
    isDark: () => currentTheme.value === 'dark',
    isLight: () => currentTheme.value === 'light',
    setDarkMode,
    setLightMode,
    toggleTheme,
    forceLightMode,
    getCurrentTheme
  }
}
