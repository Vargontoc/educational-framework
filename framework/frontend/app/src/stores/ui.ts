import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * Store de estado UI
 * Mantiene el estado de modales, pantallas de carga, mensajes de error y estados UX
 */
export const useUIStore = defineStore('ui', () => {
  // Estado reactivo
  const isLoading = ref(false)
  const errorMessage = ref<string | null>(null)
  const modalOpen = ref(false)

  // Acciones y getters se implementarán en sprints posteriores

  return {
    isLoading,
    errorMessage,
    modalOpen
  }
})
