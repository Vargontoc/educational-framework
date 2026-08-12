import { watch } from 'vue'
import { useWSStore } from '../stores/ws'
import { useChatbotPendingResponse } from './useChatbotPendingResponse'
import i18n from '../i18n'

const STREAM_CLIENT_TIMEOUT_MS = 65000 // > 60s timeout de backend (SPRINT-080)

export function useChatbotStream() {
  const wsStore = useWSStore()
  const { setPending, clearPending, isWaitingForChatbot } = useChatbotPendingResponse()
  let timeoutHandle: ReturnType<typeof setTimeout> | null = null

  function clearTimeoutGuard(): void {
    if (timeoutHandle) clearTimeout(timeoutHandle)
    timeoutHandle = null
  }

  function send(message: string): void {
    if (isWaitingForChatbot.value) return

    wsStore.chatbotError = null
    wsStore.appendUserMessage(message)
    setPending()
    wsStore.publishParentMessage('/app/chatbot/send', { message })

    timeoutHandle = setTimeout(() => {
      wsStore.chatbotError = i18n.global.t('views.chatbot.errorTimeout')
      clearPending()
    }, STREAM_CLIENT_TIMEOUT_MS)
  }

  // ERROR desbloquea el input de inmediato
  watch(() => wsStore.chatbotError, (err) => {
    if (err) {
      clearPending()
      clearTimeoutGuard()
    }
  })

  // COMPLETE empuja un mensaje ASSISTANT: es la única señal de éxito que desbloquea el envío
  // (chatbotMessages también crece con el eco local del mensaje USER, que no debe desbloquear)
  watch(() => wsStore.chatbotMessages.length, () => {
    const last = wsStore.chatbotMessages[wsStore.chatbotMessages.length - 1]
    if (last?.role === 'ASSISTANT') {
      clearPending()
      clearTimeoutGuard()
    }
  })

  return { send, isWaitingForChatbot }
}
