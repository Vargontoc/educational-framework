import { apiClient } from './api'
import { useParentalAuthStore } from '@/stores/parentalAuth'
import type {
  AbandonmentSignalResponse,
  DiaryActivityResponse,
  DiaryPeriod,
  DiarySummaryResponse
} from '@/types/diary'

interface ApiDiarySummaryResponse {
  success: boolean
  message: string | null
  errors: string[] | null
  data: DiarySummaryResponse
}

interface ApiDiaryActivitiesResponse {
  success: boolean
  message: string | null
  errors: string[] | null
  data: DiaryActivityResponse[]
}

interface ApiAbandonmentSignalResponse {
  success: boolean
  message: string | null
  errors: string[] | null
  data: AbandonmentSignalResponse | null
}

function authHeaders(): Record<string, string> {
  const token = useParentalAuthStore().token
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export async function getSummary(childProfileId: number, period: DiaryPeriod): Promise<DiarySummaryResponse> {
  const response = await apiClient.get<ApiDiarySummaryResponse>(
    `/api/v1/diary/children/${childProfileId}/summary`,
    { period },
    authHeaders()
  )
  return response.data
}

export async function getActivities(childProfileId: number, period: DiaryPeriod): Promise<DiaryActivityResponse[]> {
  const response = await apiClient.get<ApiDiaryActivitiesResponse>(
    `/api/v1/diary/children/${childProfileId}/activities`,
    { period },
    authHeaders()
  )
  return response.data ?? []
}

export async function getAbandonmentSignal(
  childProfileId: number,
  activityId: number
): Promise<AbandonmentSignalResponse | null> {
  const response = await apiClient.get<ApiAbandonmentSignalResponse>(
    `/api/v1/diary/children/${childProfileId}/abandonment-signal`,
    { activityId: String(activityId) },
    authHeaders()
  )
  return response.data
}
