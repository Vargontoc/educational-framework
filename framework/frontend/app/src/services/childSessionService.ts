import api from '@/shared/api/axios'
import type {
  ApiResponse,
  OpenChildSessionRequest,
  ChildSessionResponse
} from '@/shared/types/api'

export async function openChildSession(payload: OpenChildSessionRequest): Promise<ChildSessionResponse> {
  const { data } = await api.post<ApiResponse<ChildSessionResponse>>('/api/v1/sessions/children', payload)
  return data.data
}

export async function closeChildSession(id: number): Promise<void> {
  await api.delete(`/api/v1/sessions/children/${id}`)
}

export async function getActiveChildSessions(familyId: number): Promise<ChildSessionResponse[]> {
  const { data } = await api.get<ApiResponse<ChildSessionResponse[]>>('/api/v1/sessions/children', {
    params: { familyId }
  })
  return data.data
}

export async function expelSession(sessionId: number): Promise<void> {
  await api.delete(`/api/v1/sessions/children/${sessionId}/expel`)
}