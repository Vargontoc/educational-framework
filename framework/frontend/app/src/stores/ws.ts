import { defineStore } from 'pinia'
import { ref, shallowRef } from 'vue'
import type { IMessage } from '@stomp/stompjs'
import type { ConnectionStatus } from '../services/websocket'
import { StompParentClient } from '../services/stompParentClient'
import type { ChatbotStreamEvent, ChatMessage, ToolCallInfo } from '../types/chatbot'
import i18n from '../i18n'

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

  // Cliente STOMP del canal parental (shallowRef: instancia de clase, no reactiva en profundidad)
  const stompClient = shallowRef<StompParentClient | null>(null)

  /**
   * Conectar el canal parental (STOMP sobre /ws/parent)
   */
  function connectParentChannel(token: string): void {
    if (stompClient.value?.connected) return
    stompClient.value = new StompParentClient({ onStatusChange: setParentChannelStatus })
    stompClient.value.connect(token)
  }

  /**
   * Desconectar el canal parental
   */
  function disconnectParentChannel(): void {
    stompClient.value?.disconnect()
    stompClient.value = null
    setParentChannelStatus('disconnected')
  }

  /**
   * Suscribirse a un topic del canal parental
   */
  function subscribeParentTopic(destination: string, callback: (message: IMessage) => void): void {
    stompClient.value?.subscribe(destination, callback)
  }

  /**
   * Cancelar la suscripción a un topic del canal parental
   */
  function unsubscribeParentTopic(destination: string): void {
    stompClient.value?.unsubscribe(destination)
  }

  /**
   * Publicar un mensaje en el canal parental
   */
  function publishParentMessage(destination: string, body: unknown): void {
    stompClient.value?.publish(destination, body)
  }

  // Estado de dominio del chatbot (SPRINT-035 / SPRINT-036)
  const chatbotMessages = ref<ChatMessage[]>([])
  const chatbotStreamBuffer = ref('')
  const chatbotStreamToolCalls = ref<ToolCallInfo[]>([])
  const chatbotConversationId = ref<string | null>(null)
  const chatbotError = ref<string | null>(null)

  /**
   * Despachar un evento de streaming del chatbot recibido por el topic
   */
  function handleChatbotEvent(raw: IMessage): void {
    const event: ChatbotStreamEvent = JSON.parse(raw.body)
    chatbotConversationId.value = event.conversationId
    switch (event.event) {
      case 'TOKEN':
        chatbotStreamBuffer.value += event.content ?? ''
        break
      case 'TOOL_CALL_START':
        if (event.toolCallInfo) {
          chatbotStreamToolCalls.value.push(event.toolCallInfo)
        }
        break
      case 'TOOL_CALL_RESULT': {
        if (!event.toolCallInfo) break
        const inFlightIndex = chatbotStreamToolCalls.value.findIndex(
          tc => tc.toolName === event.toolCallInfo!.toolName && tc.status === 'STARTED'
        )
        if (inFlightIndex !== -1) {
          chatbotStreamToolCalls.value[inFlightIndex] = event.toolCallInfo
        } else {
          chatbotStreamToolCalls.value.push(event.toolCallInfo)
        }
        break
      }
      case 'COMPLETE':
        chatbotMessages.value.push({
          role: 'ASSISTANT',
          content: event.content ?? chatbotStreamBuffer.value,
          createdAt: new Date().toISOString(),
          toolCalls: chatbotStreamToolCalls.value.length > 0 ? [...chatbotStreamToolCalls.value] : undefined
        })
        chatbotStreamBuffer.value = ''
        chatbotStreamToolCalls.value = []
        break
      case 'ERROR':
        chatbotError.value = event.content ?? i18n.global.t('views.chatbot.errorGeneric')
        chatbotStreamBuffer.value = ''
        chatbotStreamToolCalls.value = []
        break
    }
  }

  /**
   * Suscribirse al topic de streaming del chatbot de la familia.
   * Idempotente: StompParentClient.subscribe ya evita duplicar la suscripción
   * al mismo destino.
   */
  function subscribeChatbotTopic(familyId: number): void {
    subscribeParentTopic(`/topic/family/${familyId}/chatbot`, handleChatbotEvent)
  }

  /**
   * Cargar una conversación completa (mensajes + id) en el estado activo,
   * p. ej. al entrar en la vista o al seleccionar una desde el historial.
   */
  function loadChatbotConversation(conversationId: string, messages: ChatMessage[]): void {
    chatbotConversationId.value = conversationId
    chatbotMessages.value = messages
    chatbotStreamBuffer.value = ''
    chatbotStreamToolCalls.value = []
    chatbotError.value = null
  }

  /**
   * Volver a un estado de conversación vacío (comando `/new`).
   * El backend no mantiene continuidad entre mensajes (cada envío crea su propia
   * conversación), así que "nueva conversación" es, hoy, un reseteo puramente local.
   */
  function resetChatbotConversation(): void {
    chatbotConversationId.value = null
    chatbotMessages.value = []
    chatbotStreamBuffer.value = ''
    chatbotStreamToolCalls.value = []
    chatbotError.value = null
  }

  /**
   * Anexar localmente el mensaje del usuario recién enviado.
   * El backend no reemite el mensaje del propio usuario por el topic
   * (solo lo persiste), así que el eco es responsabilidad del frontend.
   */
  function appendUserMessage(content: string): void {
    chatbotMessages.value.push({
      role: 'USER',
      content,
      createdAt: new Date().toISOString()
    })
  }

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
    } else if (status === 'disconnected') {
      if (activeChannel.value === 'parent') {
        activeChannel.value = null
      }
      // ADR-010: sin cola de eventos offline — se descarta el streaming en curso
      if (chatbotStreamBuffer.value || chatbotStreamToolCalls.value.length > 0) {
        chatbotStreamBuffer.value = ''
        chatbotStreamToolCalls.value = []
        chatbotError.value = i18n.global.t('views.chatbot.errorConnectionLost')
      }
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
    reset,
    connectParentChannel,
    disconnectParentChannel,
    subscribeParentTopic,
    unsubscribeParentTopic,
    publishParentMessage,
    chatbotMessages,
    chatbotStreamBuffer,
    chatbotStreamToolCalls,
    chatbotConversationId,
    chatbotError,
    subscribeChatbotTopic,
    loadChatbotConversation,
    resetChatbotConversation,
    appendUserMessage
  }
})
