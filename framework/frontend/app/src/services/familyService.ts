import { apiClient, type ApiError } from './api'
import type { ApiFamilyResponse, FamilyData } from '../composables/useFamilyStatus'

export interface CreateFamilyRequest {
  name: string
  pin: string
}

export interface CreateFamilyResult {
  success: boolean
  data?: FamilyData
  errorKey?: 'validation' | 'conflict' | 'server' | 'connection'
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
