import { apiClient } from './api'
import { useParentalAuthStore } from '../stores/parentalAuth'

export interface ConversationMessageResponse {
  role: string
  content: string
  createdAt: string
}

export interface ConversationResponse {
  conversationId: string
  title: string | null
  startedAt: string
  lastMessageAt: string
  message: ConversationMessageResponse[]
}

interface ApiConversationsResponse {
  success: boolean
  message: string | null
  errors: string[]
  data: ConversationResponse[]
}

interface ApiConversationResponse {
  success: boolean
  message: string | null
  errors: string[]
  data: ConversationResponse
}

interface ApiBooleanResponse {
  success: boolean
  message: string | null
  errors: string[]
  data: boolean
}


interface CommandResponse {
  trigger: string
  description: string
}

interface ApiCommandResponse {
  success: boolean
  message: string | null
  errors: string[]
  data: CommandResponse[]
}

interface UpdateTitle {
  conversation: string
  title: string
}
/**
 * Obtiene la última conversación de la familia autenticada.
 * Devuelve null tanto si no hay conversaciones previas como si la petición falla
 * (usuario nuevo y error de red no deben bloquear la vista, ver R3 del sprint).
 */
export async function getLastConversation(): Promise<ConversationResponse | null> {
  const token = useParentalAuthStore().token
  if (!token) return null

  try {
    const response = await apiClient.get<ApiConversationsResponse>(
      '/api/v1/agents/conversations',
      { limit: '1' },
      { Authorization: `Bearer ${token}` }
    )
    if (response.success && response.data.length > 0) {
      return response.data[0]
    }
    return null
  } catch {
    return null
  }
}

export async function getCommands(): Promise<CommandResponse[] | null> {
  const token = useParentalAuthStore().token;
  if(!token) return null

  try {
    const response = await apiClient.get<ApiCommandResponse>('/api/v1/agents/conversations/commands', {}, {Authorization: `Bearer ${token}`})
    if(response.success && response.data.length > 0)
      return response.data;

    return null
  }catch {
    return null
  }
}

/**
 * Lista las conversaciones de la familia autenticada, más reciente primero.
 */
export async function listConversations(limit = 20): Promise<ConversationResponse[]> {
  const token = useParentalAuthStore().token
  if (!token) return []

  try {
    const response = await apiClient.get<ApiConversationsResponse>(
      '/api/v1/agents/conversations',
      { limit: String(limit) },
      { Authorization: `Bearer ${token}` }
    )
    return response.success ? response.data : []
  } catch {
    return []
  }
}

/**
 * Obtiene una conversación concreta con todos sus mensajes.
 */
export async function getConversationById(conversationId: string): Promise<ConversationResponse | null> {
  const token = useParentalAuthStore().token
  if (!token) return null

  try {
    const response = await apiClient.get<ApiConversationResponse>(
      `/api/v1/agents/conversations/${conversationId}`,
      undefined,
      { Authorization: `Bearer ${token}` }
    )
    return response.success ? response.data : null
  } catch {
    return null
  }
}

/**
 * Actualiza el título de una conversación. Devuelve null si falla
 * (el modal debe mostrar un error y conservar el título anterior).
 */
export async function updateConversationTitle(
  conversationId: string,
  title: string
): Promise<ConversationResponse | null> {
  const token = useParentalAuthStore().token
  if (!token) return null

  const update = { } as UpdateTitle
  update.title = title
  update.conversation = conversationId

  try {
    const response = await apiClient.patch<ApiConversationResponse>(
      '/api/v1/agents/conversations',
      update,
      { Authorization: `Bearer ${token}` }
    )
    return response.success ? response.data : null
  } catch {
    return null
  }
}

/**
 * Elimina una conversación completa.
 */
export async function deleteConversationById(conversationId: string): Promise<boolean> {
  const token = useParentalAuthStore().token
  if (!token) return false

  try {
    const response = await apiClient.delete<ApiBooleanResponse>(
      `/api/v1/agents/conversations/${conversationId}`,
      { Authorization: `Bearer ${token}` }
    )
    return response.success && response.data
  } catch {
    return false
  }
}
