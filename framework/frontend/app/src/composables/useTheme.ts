import { ref, watch } from 'vue'

/**
 * Composable para gestión de temas (claro/oscuro)
 * 
 * Según ADR-018 y SPRINT-003:
 * - El modo oscuro es exclusivo del panel parental
 * - La preferencia persiste en localStorage
 * - La experiencia infantil siempre usa tema claro
 * - El tema se aplica mediante atributo data-theme en el root element
 */

type Theme = 'light' | 'dark'

const STORAGE_KEY = 'nubi-theme-preference'

// Estado global reactivo
const currentTheme = ref<Theme>('light')
let isInitialized = false

/**
 * Obtiene el tema guardado en localStorage
 */
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

/**
 * Guarda el tema en localStorage
 */
function storeTheme(theme: Theme): void {
  try {
    localStorage.setItem(STORAGE_KEY, theme)
  } catch (e) {
    console.warn('Error saving theme to localStorage:', e)
  }
}

/**
 * Aplica el tema al documento
 */
function applyTheme(theme: Theme): void {
  document.documentElement.setAttribute('data-theme', theme)
  currentTheme.value = theme
}

/**
 * Inicializa el tema desde localStorage o usa 'light' por defecto
 */
function initTheme(): void {
  if (isInitialized) return
  
  const storedTheme = getStoredTheme()
  const initialTheme = storedTheme || 'light'
  applyTheme(initialTheme)
  
  // Observar cambios en el tema
  watch(currentTheme, (newTheme) => {
    applyTheme(newTheme)
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
