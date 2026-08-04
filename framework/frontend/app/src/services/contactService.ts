import { apiClient } from './api'

export type ContactMessageType = 'COMMENT' | 'SUGGEST' | 'ERROR'

interface ContactRequest {
  type: ContactMessageType
  message: string
}

interface ContactResponse {
  sent: boolean
  timestamp: string
}

export const contactService = {
  async sendMessage(type: ContactMessageType, message: string): Promise<ContactResponse> {
    const payload: ContactRequest = { type, message }
    return apiClient.post<ContactResponse>('/api/v1/contact', payload)
  }
}
