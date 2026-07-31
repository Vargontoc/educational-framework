import { apiClient, type ApiError } from './api'
import type { ApiFamilyResponse, FamilyData } from '../composables/useFamilyStatus'
import type { FamilyUpdatePayload } from '../types/family-config'

export interface ChildProfileExtended {
  id: number
  familyId: number
  name: string
  active: boolean
  birthday: string
  avatar: string
  npcVoiceEnabled: boolean
  npcEnabled: boolean
  npcVoiceVolume: number
  colorVisionMode: string
  createdAt: string
  updatedAt: string
}

/** @deprecated Usar ChildProfileExtended. Se eliminará en SPRINT-027. */
export type ChildProfile = ChildProfileExtended

export interface ApiListChildProfileResponse {
  success: boolean
  message: string | null
  errors: string[]
  data: ChildProfileExtended[]
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
  npcVoiceEnabled: boolean
  npcEnabled: boolean
  npcVoiceVolume: number
  colorVisionMode: string | null
}

export interface UpdateChildProfileRequest {
  name?: string
  birthday?: string
  avatar?: string
  npcVoiceEnabled?: boolean
  npcVoiceVolume?: number
  npcEnabled?: boolean
  colorVisionMode?: string
}

export interface VerifyPinResult {
  success: boolean
  errorKey?: 'invalid-pin' | 'connection' | 'server'
}

export interface CreateChildResult {
  success: boolean
  data?: ChildProfileExtended
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
  data: ChildProfileExtended
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

export async function getChildren(): Promise<ChildProfileExtended[]> {
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
      npcVoiceEnabled: request.npcVoiceEnabled,
      npcEnabled: request.npcEnabled,
      npcVoiceVolume: request.npcVoiceVolume,
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

export async function getChild(id: number): Promise<ChildProfileExtended> {
  const response = await apiClient.get<ApiChildProfileResponse>(
    `/api/v1/family/children/${id}`
  )
  return response.data
}

export async function updateChild(
  id: number,
  request: UpdateChildProfileRequest
): Promise<ChildProfileExtended> {
  const response = await apiClient.put<ApiChildProfileResponse>(
    `/api/v1/family/children/${id}`,
    request
  )
  return response.data
}

export async function deleteChild(id: number): Promise<boolean> {
  try {
    await apiClient.delete(`/api/v1/family/children/${id}`)
    return true
  } catch (error) {
    console.error('Error al eliminar perfil:', error)
    return false
  }
}

export async function toggleChildActivation(id: number): Promise<boolean> {
  try {
    await apiClient.put(`/api/v1/family/children/activation/${id}`)
    return true
  } catch (error) {
    console.error('Error al cambiar estado de activación:', error)
    return false
  }
}

/**
 * Actualiza la configuración global de familia mediante PATCH /api/v1/family
 * 
 * Permite envío parcial: solo se envían los campos modificados.
 * El backend valida y aplica los cambios, devolviendo la familia actualizada.
 * 
 * @param config - Payload parcial con campos a actualizar (config global, name, pin)
 * @returns Promise con los datos de familia actualizados
 * @throws ApiError con status y message según el tipo de error:
 *   - status 0: error de red/conexión
 *   - status 400: error de validación (PIN inválido, volumen fuera de rango)
 *   - status 401: no autenticado (el caller debe manejar logout)
 *   - status 5xx: error del servidor
 * 
 * @example
 * // Actualizar solo volumen de audio general
 * await updateFamilyConfig({ audioGeneralVolume: 50 })
 * 
 * @example
 * // Actualizar múltiples campos
 * await updateFamilyConfig({
 *   audioGeneralEnabled: false,
 *   npcVoiceVolume: 75,
 *   pin: '1234'
 * })
 */
export async function updateFamilyConfig(
  config: FamilyUpdatePayload
): Promise<FamilyData> {
  const response = await apiClient.patch<ApiFamilyResponse>('/api/v1/family', config)
  return response.data
}

/**
 * Verifica si un error es de tipo ApiError
 */
export function isApiError(error: unknown): error is ApiError {
  return (
    typeof error === 'object' &&
    error !== null &&
    'status' in error &&
    'message' in error
  )
}

/**
 * Obtiene un mensaje de usuario amigable según el tipo de error
 */
export function getUserFriendlyErrorMessage(error: unknown): string {
  if (!isApiError(error)) {
    return 'Error desconocido'
  }

  switch (error.status) {
    case 0:
      return 'No se pudo guardar. Revisa tu conexión.'
    case 400:
      return 'Los datos no son válidos. Revisa e inténtalo de nuevo.'
    case 401:
      return 'Tu sesión ha expirado. Inicia sesión de nuevo.'
    case 500:
    case 502:
    case 503:
      return 'Error del servidor. Inténtalo más tarde.'
    default:
      return error.message || 'Error al guardar la configuración'
  }
}
