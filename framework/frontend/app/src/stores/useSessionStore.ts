import { ref } from 'vue'
import { defineStore } from 'pinia'
import type { ChildSessionResponse } from '@/shared/types/api'
import * as authService from '@/services/authService'

export const useSessionStore = defineStore(
  'session',
  () => {
    const familyId = ref<number | null>(null)
    const selectedChildId = ref<number | null>(null)
    const token = ref<string | null>(null)
    const activeChildSession = ref<ChildSessionResponse | null>(null)

    function $reset() {
      familyId.value = null
      selectedChildId.value = null
      token.value = null
      activeChildSession.value = null
    }

    function isAuthenticated(): boolean {
      return !!token.value
    }

    function setActiveChildSession(session: ChildSessionResponse) {
      activeChildSession.value = session
    }

    function clearActiveChildSession() {
      activeChildSession.value = null
    }

    function hasActiveChildSession(childId: string): boolean {
      if (!activeChildSession.value) return false
      return String(activeChildSession.value.childProfileId) === childId
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
      activeChildSession,
      $reset,
      logout,
      isAuthenticated,
      setActiveChildSession,
      clearActiveChildSession,
      hasActiveChildSession
    }
  },
  {
    persist: {
      pick: ['familyId', 'selectedChildId'],
      storage: sessionStorage
    }
  }
)
