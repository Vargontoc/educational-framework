import api from '@/shared/api/axios'
import type {
  ApiResponse,
  LoginRequest,
  LoginResponse
} from '@/shared/types/api'

export async function login(pin: string): Promise<LoginResponse> {
  const payload: LoginRequest = { pin }
  const { data } = await api.post<ApiResponse<LoginResponse>>('/api/v1/auth/login', payload)
  return data.data
}

export async function logout(): Promise<void> {
  await api.post('/api/v1/auth/logout')
}
