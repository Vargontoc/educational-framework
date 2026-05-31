import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import * as authService from '@/services/authService'

export const useSessionStore = defineStore(
  'session',
  () => {
    const familyId = ref<number | null>(null)
    const selectedChildId = ref<number | null>(null)
    const token = ref<string | null>(null)

    const isAuthenticated = computed(() => !!token.value)

    function $reset() {
      familyId.value = null
      selectedChildId.value = null
      token.value = null
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
      isAuthenticated,
      $reset,
      logout
    }
  },
  {
    persist: {
      pick: ['familyId', 'selectedChildId'],
      storage: sessionStorage
    }
  }
)
