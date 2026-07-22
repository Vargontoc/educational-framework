import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * Store de estado WebSocket
 * Mantiene el estado de conexión de los canales GameChannel y ParentChannel
 */
export const useWSStore = defineStore('ws', () => {
  // Estado reactivo
  const gameChannelConnected = ref(false)
  const parentChannelConnected = ref(false)
  const activeChannel = ref<'game' | 'parent' | null>(null)

  // Acciones y getters se implementarán en sprints posteriores

  return {
    gameChannelConnected,
    parentChannelConnected,
    activeChannel
  }
})
