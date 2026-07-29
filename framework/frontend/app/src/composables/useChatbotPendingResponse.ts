import { ref } from 'vue'

const isWaitingForChatbot = ref(false)

export function useChatbotPendingResponse() {
  function setPending() {
    isWaitingForChatbot.value = true
  }

  function clearPending() {
    isWaitingForChatbot.value = false
  }

  return {
    isWaitingForChatbot,
    setPending,
    clearPending
  }
}
