import api from '@/shared/api/axios'
import type {
  ApiResponse,
  ChildProfileResponse,
  CreateChildProfileRequest
} from '@/shared/types/api'

export async function getChildren(): Promise<ChildProfileResponse[]> {
  const { data } = await api.get<ApiResponse<ChildProfileResponse[]>>('/api/v1/family/children')
  return data.data
}

export async function createChild(payload: CreateChildProfileRequest): Promise<ChildProfileResponse> {
  const { data } = await api.post<ApiResponse<ChildProfileResponse>>('/api/v1/family/children', payload)
  return data.data
}
