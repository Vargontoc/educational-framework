import api from '@/shared/api/axios'
import type {
  ApiResponse,
  ChildSessionResponse,
  OpenChildSessionRequest
} from '@/shared/types/api'

export async function openChildSession(childProfileId: number): Promise<ChildSessionResponse> {
  const payload: OpenChildSessionRequest = { childProfileId }
  const { data } = await api.post<ApiResponse<ChildSessionResponse>>('/api/v1/sessions/children', payload)
  return data.data
}
