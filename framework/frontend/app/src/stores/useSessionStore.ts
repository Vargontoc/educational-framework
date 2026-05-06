import { ref } from 'vue'
import { defineStore } from 'pinia'

export const useSessionStore = defineStore(
  'session',
  () => {
    const familyId = ref<number | null>(null)
    const selectedChildId = ref<number | null>(null)
    const isAuthenticated = ref(false)
    // in-memory only — never persisted, cleared on page refresh by design (ADR-010 §3.3)
    const token = ref<string | null>(null)

    function $reset() {
      familyId.value = null
      selectedChildId.value = null
      isAuthenticated.value = false
      token.value = null
    }

    return { familyId, selectedChildId, isAuthenticated, token, $reset }
  },
  {
    persist: {
      pick: ['familyId', 'selectedChildId', 'isAuthenticated'],
      storage: sessionStorage
    }
  }
)
