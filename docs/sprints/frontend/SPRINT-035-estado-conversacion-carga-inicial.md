# SPRINT-035 — Estado de conversación del chatbot y carga inicial

## Estado

- **Estado:** pending
- **Fecha de creación:** 2026-08-11
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** SPRINT-034 (cliente STOMP), SPRINT-080-backend (streaming STOMP chatbot, pending), `ChatbotHistoryController` (historial, ya presente en el repo)
- **Impacto estimado:** extiende `useWSStore` con el estado de mensajes/streaming del chatbot; añade carga de perfiles y última conversación al entrar en `ChatbotView`.

## Objetivo

- Suscribir el topic `/topic/family/{familyId}/chatbot` y despachar sus eventos al estado extendido de `useWSStore`.
- Cargar la última conversación (`GET /api/v1/agents/conversations?limit=1`) y los nombres de perfiles (`GET /api/v1/family/children`) al entrar en `ChatbotView`.

## Contexto

SPRINT-034 deja disponibles `useWSStore.subscribeParentTopic`/`publishParentMessage` de forma genérica. Este sprint añade el dominio de datos del chatbot (mensajes, streaming, tool calls) al mismo store, según decisión de producto de extender `useWSStore` en vez de crear un store dedicado.

Contratos de referencia: `docs/contracts/api/asyncapi/schemas/chatbot-stream-payload.yaml` y `tool-call-info-payload.yaml`. El desajuste de nombres detectado inicialmente entre el modelo Java (`TOOL_CALL_STARRT`, `toolCallInfo`, `toolname`) y el contrato ya ha sido corregido en backend (`ChatbotEventType.TOOL_CALL_START`, `ChatbotStreamEvent.toolCall`, `ToolCallInfo.toolName`) — la Tarea 35.1 debe re-confirmar contra el JSON real emitido antes de dar por cerrado el tipado frontend, por si hay más divergencias no detectadas.

## Diseño funcional-técnico

### 1. Tipos del dominio chatbot

**Archivo:** `framework/frontend/app/src/types/chatbot.ts` (nuevo)

```typescript
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
  toolCall?: ToolCallInfo
}

export interface ChatMessage {
  role: 'USER' | 'ASSISTANT'
  content: string
  createdAt: string
  toolCalls?: ToolCallInfo[]
}
```

### 2. Servicio de historial

**Archivo:** `framework/frontend/app/src/services/chatbotService.ts` (nuevo)

```typescript
export interface ConversationResponse {
  conversationId: string
  startedAt: string
  lastMessageAt: string
  message: { role: string; content: string; createdAt: string }[]
}

export async function getLastConversation(): Promise<ConversationResponse | null> {
  const response = await apiClient.get<ApiResponse<ConversationResponse[]>>(
    '/api/v1/agents/conversations',
    { limit: '1' }
  )
  return response.data[0] ?? null
}
```

Nota: el campo `message` (singular) del contrato backend se mantiene tal cual para no introducir una discrepancia adicional; se mapea a un array `messages` solo dentro del store/composable frontend por claridad interna.

### 3. Extensión de `useWSStore` — estado de chatbot

**Archivo:** `stores/ws.ts` (modificar, sobre lo hecho en SPRINT-034)

```typescript
const chatbotMessages = ref<ChatMessage[]>([])
const chatbotStreamBuffer = ref('')
const chatbotConversationId = ref<string | null>(null)
const chatbotError = ref<string | null>(null)

function handleChatbotEvent(raw: IMessage) {
  const event: ChatbotStreamEvent = JSON.parse(raw.body)
  chatbotConversationId.value = event.conversationId
  switch (event.event) {
    case 'TOKEN':
      chatbotStreamBuffer.value += event.content ?? ''
      break
    case 'TOOL_CALL_START':
    case 'TOOL_CALL_RESULT':
      // se anexa al mensaje ASSISTANT en construcción; render en SPRINT-036
      break
    case 'COMPLETE':
      chatbotMessages.value.push({
        role: 'ASSISTANT',
        content: event.content ?? chatbotStreamBuffer.value,
        createdAt: new Date().toISOString()
      })
      chatbotStreamBuffer.value = ''
      break
    case 'ERROR':
      chatbotError.value = event.content ?? 'Error desconocido'
      chatbotStreamBuffer.value = ''
      break
  }
}

function subscribeChatbotTopic(familyId: number) {
  subscribeParentTopic(`/topic/family/${familyId}/chatbot`, handleChatbotEvent)
}

function loadChatbotHistory(messages: ChatMessage[]) {
  chatbotMessages.value = messages
}
```

La suscripción al topic de chatbot (`subscribeChatbotTopic`) se invoca una única vez, al entrar por primera vez en `ChatbotView` (no en `ParentPanelLayout`), reutilizando la conexión ya abierta por SPRINT-034 — evita procesar eventos de chatbot en secciones donde no se muestran.

### 4. Carga inicial en `ChatbotView.vue`

**Archivo:** `framework/frontend/app/src/views/ChatbotView.vue` (reemplaza el placeholder actual)

```typescript
onMounted(async () => {
  wsStore.subscribeChatbotTopic(authStore.familyId!)
  loading.value = true
  const [conversation] = await Promise.all([
    getLastConversation(),
    fetchProfiles() // useChildProfiles ya existente
  ])
  if (conversation) {
    wsStore.loadChatbotHistory(
      conversation.message.map(m => ({
        role: m.role as 'USER' | 'ASSISTANT',
        content: m.content,
        createdAt: m.createdAt
      }))
    )
  }
  loading.value = false
})
```

Usar `NubiSkeleton` mientras `loading` es `true`.

## Contratos y dependencias externas

| Capa | Dependencia | Estado |
|---|---|---|
| Backend | SPRINT-080-backend desplegado (topic `/topic/family/{familyId}/chatbot` emitiendo eventos reales) | ⏳ Pendiente |
| Backend | `StompSubscribeInterceptor` actualizado para aceptar `/topic/family/{familyId}/chatbot` (Tarea 80.6 del sprint backend) | ⏳ Pendiente |
| Frontend | SPRINT-034 completado (cliente STOMP y conexión activa en el panel) | ⏳ Pendiente |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|---|---|---|
| R1 | `ConversationResponse.message` singular es un nombre de campo inusual y puede inducir errores de tipado | BAJA | Documentado explícitamente en el tipo; valorar pedir a backend renombrar a `messages` en un sprint de limpieza de contrato. |
| R2 | Si el usuario entra en Chatbot antes de que la suscripción al topic se confirme, se pueden perder los primeros eventos de un envío inmediato | MEDIA | Deshabilitar el envío de mensajes hasta que `parentChannelStatus === 'connected'` (se aplica en SPRINT-036). |
| R3 | Historial vacío (usuario nuevo) | BAJA | Mostrar estado vacío (`NubiEmptyState`) en vez de error. |

## Tareas del sprint

### Tarea 35.1: Verificar y tipar el contrato de streaming

**Archivo:** `types/chatbot.ts` (nuevo). **Criterios:** los tipos coinciden exactamente con el JSON real emitido por backend; verificado con un mensaje de prueba end-to-end contra backend en desarrollo.

### Tarea 35.2: Implementar `chatbotService.ts`

**Archivo:** nuevo. **Criterios:** `getLastConversation()` devuelve la conversación más reciente o `null`; maneja errores de red devolviendo `null` sin bloquear la vista.

### Tarea 35.3: Extender `useWSStore` con estado de chatbot

**Archivo:** `stores/ws.ts` (modificar). **Criterios:** `chatbotMessages`, `chatbotStreamBuffer`, `chatbotConversationId`, `chatbotError` reactivos; `handleChatbotEvent` despacha correctamente los 5 tipos de evento; `subscribeChatbotTopic` idempotente.

### Tarea 35.4: Implementar carga inicial en `ChatbotView.vue`

**Archivo:** modificar (sustituye placeholder). **Criterios:** al entrar se ve skeleton mientras carga; tras cargar, se ve la última conversación o un estado vacío; los nombres de perfiles quedan disponibles en memoria para SPRINT-036 (atajo `@`).

### Tarea 35.5: Manejo de reconexión con conversación en curso

**Criterios:** si el canal se desconecta mientras `chatbotStreamBuffer` tiene contenido parcial, al reconectar se descarta el buffer y se muestra el error correspondiente (coherente con ADR-010, "sin cola de eventos offline").

## Archivos afectados

| Archivo | Tipo de cambio |
|---|---|
| `framework/frontend/app/src/types/chatbot.ts` | Nuevo |
| `framework/frontend/app/src/services/chatbotService.ts` | Nuevo |
| `framework/frontend/app/src/stores/ws.ts` | Modificación |
| `framework/frontend/app/src/views/ChatbotView.vue` | Modificación (reemplaza placeholder) |

## Estimación

- **Duración:** 2 días
- **Complejidad:** Media
- **Riesgo:** Medio (depende de disponibilidad real del backend de streaming)

## Criterios de aceptación del sprint

1. Al entrar en Chatbot, se suscribe el topic de la familia autenticada.
2. Se muestra la última conversación existente, o un estado vacío si no hay ninguna.
3. Los nombres de los perfiles infantiles quedan disponibles en memoria tras la carga inicial.
4. Los eventos `TOKEN`/`COMPLETE`/`ERROR` recibidos por el topic actualizan `useWSStore` correctamente.
5. `vue-tsc --noEmit` sin errores.

## Evidencias esperadas

- Prueba manual con backend de desarrollo: enviar un mensaje de prueba vía cliente STOMP y verificar que `useWSStore.chatbotMessages` se actualiza.
- Captura de la vista Chatbot mostrando la última conversación cargada.
- Captura del estado vacío para una familia sin conversaciones previas.

## Dependencias bloqueantes

- [ ] SPRINT-034 verificado.
- [ ] SPRINT-080-backend desplegado en entorno de desarrollo (al menos los eventos `TOKEN`/`COMPLETE`/`ERROR`; `TOOL_CALL_*` puede llegar más tarde sin bloquear este sprint, ver SPRINT-036).

## Handoffs a otras capas

### Backend

- Confirmar disponibilidad de `/api/v1/agents/conversations?limit=1` y del topic de streaming en el entorno de desarrollo del frontend.
- Valorar renombrar `ConversationResponse.message` a `messages` (mejora de contrato, no bloqueante).

## Notas adicionales

- Este sprint no implementa el envío de mensajes ni el bloqueo del input — eso es SPRINT-036.
- El renderizado de `TOOL_CALL_START`/`TOOL_CALL_RESULT` en la UI también se implementa en SPRINT-036; aquí solo se garantiza que el evento no rompe el despacho del store.
