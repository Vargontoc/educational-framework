import api from '@/shared/api/axios'
import type {
  ApiResponse,
  ChildProfileResponse,
  CreateChildProfileRequest,
  UpdateChildProfileRequest
} from '@/shared/types/api'

export async function getChildren(): Promise<ChildProfileResponse[]> {
  const { data } = await api.get<ApiResponse<ChildProfileResponse[]>>('/api/v1/family/children')
  return data.data
}

export async function createChild(payload: CreateChildProfileRequest): Promise<ChildProfileResponse> {
  const { data } = await api.post<ApiResponse<ChildProfileResponse>>('/api/v1/family/children', payload)
  return data.data
}

export async function updateChild(id: number, payload: UpdateChildProfileRequest): Promise<ChildProfileResponse> {
  const { data } = await api.patch<ApiResponse<ChildProfileResponse>>(`/api/v1/family/children/${id}`, payload)
  return data.data
}

export async function deleteChild(id: number): Promise<void> {
  await api.delete(`/api/v1/family/children/${id}`)
}

export async function toggleChildActivation(id: number): Promise<void> {
  await api.put(`/api/v1/family/children/activation/${id}`)
}
