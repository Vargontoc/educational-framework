import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * Store de sesión familiar
 * Mantiene el estado de la familia activa, hijo seleccionado y autenticación con backend
 * Persistencia: sessionStorage para mantener sesión entre recargas accidentales
 */
export const useSessionStore = defineStore('session', () => {
  // Estado reactivo
  const familyId = ref<string | null>(null)
  const selectedChildId = ref<string | null>(null)
  const isAuthenticated = ref(false)

  // Acciones y getters se implementarán en sprints posteriores

  return {
    familyId,
    selectedChildId,
    isAuthenticated
  }
})
