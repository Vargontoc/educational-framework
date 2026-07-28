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

export interface CreateChildRequest {
  name: string
  birthday: string
  avatar: string
  ttsEnabled: boolean
  agentEnabled: boolean
  colorVisionMode: string | null
}

export interface VerifyPinResult {
  success: boolean
  errorKey?: 'invalid-pin' | 'connection' | 'server'
}

export interface CreateChildResult {
  success: boolean
  data?: ChildProfile
  errorKey?: 'validation' | 'conflict' | 'server' | 'connection'
  errorMessage?: string
}

interface ApiLoginResponse {
  success: boolean
  message: string | null
  errors: string[]
  data: {
    token: string
    sessionId: number
    familyId: number
    createAt: string
  }
}

interface ApiChildProfileResponse {
  success: boolean
  message: string | null
  errors: string[]
  data: ChildProfile
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

export async function verifyPin(pin: string): Promise<VerifyPinResult> {
  try {
    const response = await apiClient.post<ApiLoginResponse>('/api/v1/auth/login', { pin })
    if (response.success) {
      return { success: true }
    }
    return { success: false, errorKey: 'invalid-pin' }
  } catch (err) {
    const apiError = err as ApiError
    if (apiError.status === 0) {
      return { success: false, errorKey: 'connection' }
    }
    if (apiError.status === 401) {
      return { success: false, errorKey: 'invalid-pin' }
    }
    return { success: false, errorKey: 'server' }
  }
}

export async function createChild(request: CreateChildRequest): Promise<CreateChildResult> {
  try {
    const response = await apiClient.post<ApiChildProfileResponse>('/api/v1/family/children', {
      name: request.name,
      birthday: request.birthday,
      avatar: request.avatar,
      ttsEnabled: request.ttsEnabled,
      agentEnabled: request.agentEnabled,
      colorVisionMode: request.colorVisionMode
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
      const details = apiError.details as Record<string, unknown> | undefined
      const message = details?.message as string | undefined
      return { success: false, errorKey: 'validation', errorMessage: message }
    }
    if (apiError.status === 409) {
      return { success: false, errorKey: 'conflict' }
    }
    return { success: false, errorKey: 'server' }
  }
}
