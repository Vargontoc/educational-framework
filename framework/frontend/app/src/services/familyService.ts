import api from '@/shared/api/axios'
import type {
  ApiResponse,
  FamilyResponse,
  CreateFamilyRequest,
  UpdateFamilyRequest
} from '@/shared/types/api'

export async function getFamily(): Promise<FamilyResponse> {
  const { data } = await api.get<ApiResponse<FamilyResponse>>('/api/v1/family')
  return data.data
}

export async function createFamily(payload: CreateFamilyRequest): Promise<FamilyResponse> {
  const { data } = await api.post<ApiResponse<FamilyResponse>>('/api/v1/family', payload)
  return data.data
}

export async function updateFamily(payload: UpdateFamilyRequest): Promise<FamilyResponse> {
  const { data } = await api.patch<ApiResponse<FamilyResponse>>('/api/v1/family', payload)
  return data.data
}
