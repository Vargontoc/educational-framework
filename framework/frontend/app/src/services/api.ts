/**
 * Cliente API base
 * Servicio centralizado para comunicación REST con el backend
 * Configuración de base URL desde variables de entorno
 */

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

export interface ApiResponse<T> {
  data: T
  message?: string
  timestamp?: string
}

export interface ApiError {
  status: number
  message: string
  details?: unknown
}

/**
 * Realiza una petición HTTP con manejo de errores
 */
async function request<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const url = `${API_BASE_URL}${endpoint}`
  
  const config: RequestInit = {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    ...options,
  }

  try {
    const response = await fetch(url, config)

    if (!response.ok) {
      const error: ApiError = {
        status: response.status,
        message: response.statusText,
      }

      try {
        const errorData = await response.json()
        error.details = errorData
        if (errorData.message) {
          error.message = errorData.message
        }
      } catch {
        // Si no se puede parsear el error, usar el status text
      }

      throw error
    }

    // Para respuestas 204 No Content
    if (response.status === 204) {
      return {} as T
    }

    return await response.json()
  } catch (error) {
    // Si ya es un ApiError, relanzarlo
    if (error && typeof error === 'object' && 'status' in error) {
      throw error
    }

    // Error de red o conexión
    throw {
      status: 0,
      message: 'Error de conexión con el servidor',
      details: error,
    } as ApiError
  }
}

/**
 * Cliente API con métodos HTTP
 */
function buildHeaders(headers?: Record<string, string>): Record<string, string> {
  return {
    'Content-Type': 'application/json',
    ...headers,
  }
}

export const apiClient = {
  get<T>(endpoint: string, params?: Record<string, string>): Promise<T> {
    const queryString = params ? `?${new URLSearchParams(params).toString()}` : ''
    return request<T>(`${endpoint}${queryString}`, { method: 'GET' })
  },

  post<T>(endpoint: string, data?: unknown, headers?: Record<string, string>): Promise<T> {
    return request<T>(endpoint, {
      method: 'POST',
      body: data ? JSON.stringify(data) : undefined,
      headers: buildHeaders(headers),
    })
  },

  put<T>(endpoint: string, data?: unknown): Promise<T> {
    return request<T>(endpoint, {
      method: 'PUT',
      body: data ? JSON.stringify(data) : undefined,
    })
  },

  delete<T>(endpoint: string, headers?: Record<string, string>): Promise<T> {
    return request<T>(endpoint, {
      method: 'DELETE',
      headers: buildHeaders(headers),
    })
  },
}

export default apiClient
