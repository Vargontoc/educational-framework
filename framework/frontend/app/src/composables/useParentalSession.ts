import { apiClient, type ApiError } from '../services/api'
import { useParentalAuthStore } from '../stores/parentalAuth'

export type LoginErrorKey = 'invalid-pin' | 'connection' | 'server'
export type LogoutErrorKey = 'connection' | 'server'

interface ApiLoginResponse {
  success: boolean
  message: string | null
  errors: string[]
  data: {
    token: string
    sessionId: number
    familyId: number
    createAt: string
  }
}

export function useParentalSession() {
  const authStore = useParentalAuthStore()

  async function login(pin: string): Promise<{ success: boolean; errorKey?: LoginErrorKey }> {
    try {
      const response = await apiClient.post<ApiLoginResponse>('/api/v1/auth/login', { pin })
      if (response.success && response.data) {
        authStore.setAuth({
          token: response.data.token,
          sessionId: response.data.sessionId,
          familyId: response.data.familyId
        })
        authStore.resetAttempts()
        return { success: true }
      }
      return { success: false, errorKey: 'invalid-pin' }
    } catch (err) {
      const apiError = err as ApiError
      if (apiError.status === 0) {
        return { success: false, errorKey: 'connection' }
      }
      if (apiError.status === 401) {
        return { success: false, errorKey: 'invalid-pin' }
      }
      return { success: false, errorKey: 'server' }
    }
  }

  async function logout(): Promise<void> {
    const currentToken = authStore.token
    try {
      if (currentToken) {
        await apiClient.post('/api/v1/auth/logout', undefined, {
          'Authorization': `Bearer ${currentToken}`
        })
      }
    } catch {
      // Clear local state regardless of API result
    } finally {
      authStore.clearAuth()
    }
  }

  return { login, logout }
}
