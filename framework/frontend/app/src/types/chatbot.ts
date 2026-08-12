/**
 * Tipos del dominio chatbot (SPRINT-035)
 *
 * Verificado contra el JSON real emitido por backend (no solo el contrato AsyncAPI):
 * - `ChatbotStreamEvent` (es.vargontoc.educational.framework.agents.model) serializa el campo
 *   de tool call como `toolCallInfo`, no `toolCall` como documenta
 *   docs/contracts/api/asyncapi/schemas/chatbot-stream-payload.yaml — divergencia de contrato
 *   pendiente de corregir en backend, ver handoff del sprint.
 */
export type ChatbotEventType = 'TOKEN' | 'TOOL_CALL_START' | 'TOOL_CALL_RESULT' | 'COMPLETE' | 'ERROR'

export interface ToolCallInfo {
  toolName: string
  parameters?: string
  status: 'STARTED' | 'SUCCESS' | 'ERROR'
  summary?: string
  durationMs?: number
  timestamp: string
}

export interface ChatbotStreamEvent {
  event: ChatbotEventType
  conversationId: string
  content?: string
  attempt?: number
  toolCallInfo?: ToolCallInfo
}

export interface ChatMessage {
  role: 'USER' | 'ASSISTANT'
  content: string
  createdAt: string
  toolCalls?: ToolCallInfo[]
}
