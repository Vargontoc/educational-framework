import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

/**
 * Store de sesión familiar
 * Mantiene el estado de la familia activa, hijo seleccionado y autenticación con backend
 * Persistencia: sessionStorage para mantener sesión entre recargas accidentales
 * 
 * Según ADR-010:
 * - useSessionStore: familia activa, hijo seleccionado, estado de autenticación
 * - Persistencia ligera via sessionStorage
 */

const STORAGE_KEY = 'nubi-session'

interface SessionState {
  familyId: string | null
  selectedChildId: string | null
  isAuthenticated: boolean
}

// Cargar estado inicial desde sessionStorage
function loadFromStorage(): SessionState {
  try {
    const stored = sessionStorage.getItem(STORAGE_KEY)
    if (stored) {
      return JSON.parse(stored)
    }
  } catch (e) {
    console.warn('Error loading session from storage:', e)
  }
  return {
    familyId: null,
    selectedChildId: null,
    isAuthenticated: false
  }
}

// Guardar estado en sessionStorage
function saveToStorage(state: SessionState): void {
  try {
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(state))
  } catch (e) {
    console.warn('Error saving session to storage:', e)
  }
}

// Limpiar sessionStorage
function clearStorage(): void {
  try {
    sessionStorage.removeItem(STORAGE_KEY)
  } catch (e) {
    console.warn('Error clearing session storage:', e)
  }
}

export const useSessionStore = defineStore('session', () => {
  // Estado inicial desde sessionStorage
  const initialState = loadFromStorage()
  
  // Estado reactivo
  const familyId = ref<string | null>(initialState.familyId)
  const selectedChildId = ref<string | null>(initialState.selectedChildId)
  const isAuthenticated = ref(initialState.isAuthenticated)

  // Persistir cambios en sessionStorage
  watch(
    [familyId, selectedChildId, isAuthenticated],
    () => {
      saveToStorage({
        familyId: familyId.value,
        selectedChildId: selectedChildId.value,
        isAuthenticated: isAuthenticated.value
      })
    },
    { deep: true }
  )

  /**
   * Establecer la sesión familiar autenticada
   */
  function setSession(familyIdValue: string, childId: string | null = null): void {
    familyId.value = familyIdValue
    selectedChildId.value = childId
    isAuthenticated.value = true
  }

  /**
   * Seleccionar un niño para la sesión de juego
   */
  function selectChild(childId: string): void {
    selectedChildId.value = childId
  }

  /**
   * Cerrar sesión y limpiar estado
   */
  function logout(): void {
    familyId.value = null
    selectedChildId.value = null
    isAuthenticated.value = false
    clearStorage()
  }

  return {
    familyId,
    selectedChildId,
    isAuthenticated,
    setSession,
    selectChild,
    logout
  }
})
