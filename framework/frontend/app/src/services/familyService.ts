import { apiClient, type ApiError } from './api'
import type { ApiFamilyResponse, FamilyData } from '../composables/useFamilyStatus'

export interface ChildProfile {
  id: number
  name: string
  avatar: string
  birthday: string
}

export interface ApiListChildProfileResponse {
  success: boolean
  message: string | null
  errors: string[]
  data: ChildProfile[]
}

export interface CreateFamilyRequest {
  name: string
  pin: string
}

export interface CreateFamilyResult {
  success: boolean
  data?: FamilyData
  errorKey?: 'validation' | 'conflict' | 'server' | 'connection'
}

export async function getFamily(): Promise<FamilyData | null> {
  try {
    const response = await apiClient.get<ApiFamilyResponse>('/api/v1/family')
    if (response.success && response.data) {
      return response.data
    }
    return null
  } catch (err) {
    const apiError = err as ApiError
    if (apiError.status === 404) {
      return null
    }
    throw apiError
  }
}

export async function getChildren(): Promise<ChildProfile[]> {
  const response = await apiClient.get<ApiListChildProfileResponse>('/api/v1/family/children')
  if (response.success && response.data) {
    return response.data
  }
  return []
}

export async function createFamily(request: CreateFamilyRequest): Promise<CreateFamilyResult> {
  try {
    const response = await apiClient.post<ApiFamilyResponse>('/api/v1/family', {
      name: request.name,
      pin: request.pin
    })

    if (response.success && response.data) {
      return { success: true, data: response.data }
    }

    return { success: false, errorKey: 'server' }
  } catch (err) {
    const apiError = err as ApiError

    if (apiError.status === 0) {
      return { success: false, errorKey: 'connection' }
    }
    if (apiError.status === 400) {
      return { success: false, errorKey: 'validation' }
    }
    if (apiError.status === 409) {
      return { success: false, errorKey: 'conflict' }
    }
    return { success: false, errorKey: 'server' }
  }
}
