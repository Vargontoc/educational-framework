/**
 * Composable useFamilyStatus
 * 
 * Consulta el estado de la familia mediante GET /api/v1/family
 * Segun FEAT-002 y SPRINT-008:
 * - Determina si existe familia registrada
 * - Obtiene el nombre de la familia para la bienvenida
 * - Maneja estados de loading, error y sin familia
 * 
 * Contrato: docs/contracts/api/openapi/paths/family/get-family.yaml
 * Schema: docs/contracts/api/openapi/schemas/family/family-response.yaml
 * Wrapper: docs/contracts/api/openapi/schemas/family/api-family-response.yaml
 */

import { ref, computed, readonly } from 'vue'
import { apiClient, type ApiError } from '../services/api'

/**
 * Datos de la familia segun family-response.yaml
 */
export interface FamilyData {
  id: number
  name: string
  ttsEnabled: boolean
  agentEnabled: boolean
  createdAt: string
  updatedAt: string
}

/**
 * Respuesta completa de la API segun api-family-response.yaml
 * Hereda de api-response.yaml: success, message, errors
 */
export interface ApiFamilyResponse {
  success: boolean
  message: string | null
  errors: string[]
  data: FamilyData
}

/**
 * Estado del composable useFamilyStatus
 */
export interface FamilyStatusState {
  /** Indica si se esta consultando el estado de la familia */
  loading: boolean
  /** Indica si hubo un error en la consulta */
  error: boolean
  /** Mensaje de error si aplica */
  errorMessage: string
  /** Datos de la familia si existe */
  family: FamilyData | null
  /** Indica si hay una familia registrada */
  hasFamily: boolean
  /** Nombre de la familia truncado a 50 caracteres con puntos suspensivos */
  truncatedName: string
}

/**
 * Longitud maxima para el nombre de familia antes de truncar
 * Segun FEAT-002 requisito 5 y SPRINT-008
 */
const MAX_FAMILY_NAME_LENGTH = 50

/**
 * Trunca un nombre de familia a MAX_FAMILY_NAME_LENGTH caracteres con puntos suspensivos
 */
function truncateName(name: string, maxLength: number = MAX_FAMILY_NAME_LENGTH): string {
  if (name.length <= maxLength) {
    return name
  }
  return name.substring(0, maxLength) + '...'
}

export function useFamilyStatus() {
  const loading = ref(false)
  const error = ref(false)
  const errorMessage = ref('')
  const family = ref<FamilyData | null>(null)

  /**
   * Consulta el estado de la familia mediante GET /api/v1/family
   * - 200 con data: familia existe
   * - 404 o success false: no hay familia registrada
   * - Otro error: error de conexion o servidor
   */
  async function fetchFamilyStatus(): Promise<void> {
    loading.value = true
    error.value = false
    errorMessage.value = ''
    family.value = null

    try {
      const response = await apiClient.get<ApiFamilyResponse>('/api/v1/family')

      if (response.success && response.data) {
        family.value = response.data
      } else {
        // success false sin error HTTP: no hay familia registrada
        family.value = null
      }
    } catch (err) {
      const apiError = err as ApiError

      if (apiError.status === 404) {
        // 404: no hay familia registrada, no es un error
        family.value = null
      } else {
        // Error de conexion o servidor
        error.value = true
        errorMessage.value = apiError.message || 'Error al consultar el estado de familia'
      }
    } finally {
      loading.value = false
    }
  }

  /**
   * Nombre de la familia truncado a 50 caracteres con puntos suspensivos
   */
  const truncatedName = computed(() => {
    if (!family.value?.name) return ''
    return truncateName(family.value.name)
  })

  /**
   * Indica si hay una familia registrada
   */
  const hasFamily = computed(() => family.value !== null)

  return {
    loading: readonly(loading),
    error: readonly(error),
    errorMessage: readonly(errorMessage),
    family: readonly(family),
    hasFamily,
    truncatedName,
    fetchFamilyStatus
  }
}
