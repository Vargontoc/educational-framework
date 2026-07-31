import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const TOKEN_KEY = 'nubi-parental-token'
const COOLDOWN_MS = 15000
const MAX_ATTEMPTS = 3

export const useParentalAuthStore = defineStore('parentalAuth', () => {
  const token = ref<string | null>(null)
  const familyId = ref<number | null>(null)
  const sessionId = ref<number | null>(null)
  const loginAttempts = ref(0)
  const cooldownUntil = ref<number>(0)

  let cooldownTimer: ReturnType<typeof setInterval> | null = null

  const isAuthenticated = computed(() => !!token.value)
  const isInCooldown = computed(() => Date.now() < cooldownUntil.value)

  function setAuth(data: { token: string; sessionId: number; familyId: number }) {
    token.value = data.token
    sessionId.value = data.sessionId
    familyId.value = data.familyId
  }

  function clearAuth() {
    token.value = null
    sessionId.value = null
    familyId.value = null
    sessionStorage.removeItem(TOKEN_KEY)
  }

  function incrementAttempts() {
    loginAttempts.value++
    if (loginAttempts.value >= MAX_ATTEMPTS) {
      cooldownUntil.value = Date.now() + COOLDOWN_MS
      startCooldownTimer()
    }
  }

  function resetAttempts() {
    loginAttempts.value = 0
    cooldownUntil.value = 0
    stopCooldownTimer()
  }

  function startCooldownTimer() {
    stopCooldownTimer()
    cooldownTimer = setInterval(() => {
      if (Date.now() >= cooldownUntil.value) {
        resetAttempts()
      }
    }, 1000)
  }

  function stopCooldownTimer() {
    if (cooldownTimer !== null) {
      clearInterval(cooldownTimer)
      cooldownTimer = null
    }
  }

  function cleanup() {
    stopCooldownTimer()
  }

  return {
    token,
    familyId,
    sessionId,
    loginAttempts,
    cooldownUntil,
    isAuthenticated,
    isInCooldown,
    setAuth,
    clearAuth,
    incrementAttempts,
    resetAttempts,
    cleanup
  }
})
