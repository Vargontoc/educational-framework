import { apiClient } from './api'

export interface ChildSession {
  id: number
  childProfileId: number
  familyId: number
  status: string
  startedAt: string
  endedAt: string | null
  durationSeconds: number | null
  lastActivityAt: string
}

interface ApiListChildSessionResponse {
  success: boolean
  message: string | null
  errors: string[]
  data: ChildSession[]
}

export async function getActiveSessions(familyId: number): Promise<ChildSession[]> {
  const response = await apiClient.get<ApiListChildSessionResponse>(
    `/api/v1/sessions/children?familyId=${familyId}`
  )
  if (response.success && response.data) {
    return response.data
  }
  return []
}

export async function expelSession(sessionId: number): Promise<boolean> {
  try {
    await apiClient.delete(`/api/v1/sessions/children/${sessionId}/expel`)
    return true
  } catch (error) {
    console.error('Error al expulsar sesión:', error)
    return false
  }
}
