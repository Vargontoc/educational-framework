import { ref } from 'vue'
import { defineStore } from 'pinia'
import * as authService from '@/services/authService'

export const useSessionStore = defineStore(
  'session',
  () => {
    const familyId = ref<number | null>(null)
    const selectedChildId = ref<number | null>(null)
    const token = ref<string | null>(null)

    function $reset() {
      familyId.value = null
      selectedChildId.value = null
      token.value = null
    }

    function isAuthenticated(): boolean {
      return !!token.value
    }

    async function logout() {
      try {
        await authService.logout()
      } catch {
      }
      $reset()
    }

    return {
      familyId,
      selectedChildId,
      token,
      $reset,
      logout,
      isAuthenticated
    }
  },
  {
    persist: {
      pick: ['familyId', 'selectedChildId'],
      storage: sessionStorage
    }
  }
)
