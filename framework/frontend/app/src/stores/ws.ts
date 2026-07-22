import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ConnectionStatus } from '../services/websocket'

/**
 * Store de estado WebSocket
 * Mantiene el estado de conexión de los canales GameChannel y ParentChannel
 * 
 * Según ADR-010:
 * - useWSStore: estado de conexión de ambos canales WebSocket
 * - Estado reactivo expuesto para que componentes respondan visualmente
 */
export const useWSStore = defineStore('ws', () => {
  // Estado de conexión de cada canal
  const gameChannelStatus = ref<ConnectionStatus>('disconnected')
  const parentChannelStatus = ref<ConnectionStatus>('disconnected')
  
  // Canal activo actualmente en uso
  const activeChannel = ref<'game' | 'parent' | null>(null)

  /**
   * Actualizar estado del canal de juego
   */
  function setGameChannelStatus(status: ConnectionStatus): void {
    gameChannelStatus.value = status
    if (status === 'connected') {
      activeChannel.value = 'game'
    } else if (status === 'disconnected' && activeChannel.value === 'game') {
      activeChannel.value = null
    }
  }

  /**
   * Actualizar estado del canal parental
   */
  function setParentChannelStatus(status: ConnectionStatus): void {
    parentChannelStatus.value = status
    if (status === 'connected') {
      activeChannel.value = 'parent'
    } else if (status === 'disconnected' && activeChannel.value === 'parent') {
      activeChannel.value = null
    }
  }

  /**
   * Verificar si algún canal está conectado
   */
  function isConnected(): boolean {
    return gameChannelStatus.value === 'connected' || parentChannelStatus.value === 'connected'
  }

  /**
   * Verificar si algún canal está reconectando
   */
  function isReconnecting(): boolean {
    return gameChannelStatus.value === 'reconnecting' || parentChannelStatus.value === 'reconnecting'
  }

  /**
   * Resetear estado de conexión
   */
  function reset(): void {
    gameChannelStatus.value = 'disconnected'
    parentChannelStatus.value = 'disconnected'
    activeChannel.value = null
  }

  return {
    gameChannelStatus,
    parentChannelStatus,
    activeChannel,
    setGameChannelStatus,
    setParentChannelStatus,
    isConnected,
    isReconnecting,
    reset
  }
})
