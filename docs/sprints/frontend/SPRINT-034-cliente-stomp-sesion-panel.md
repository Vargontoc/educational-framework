# SPRINT-034 — Cliente STOMP nativo y sesión del canal parental

## Estado

- **Estado:** pending
- **Fecha de creación:** 2026-08-11
- **Responsable principal:** frontend
- **Prioridad:** ALTA
- **Dependencias:** SPRINT-080-backend (streaming STOMP chatbot, pending), ADR-010 (aceptada), acceso parental por PIN ya implementado
- **Impacto estimado:** Sustituye el cliente WebSocket "crudo" sin consumidores (`createParentWebSocket`) por un cliente STOMP real; conecta la sesión al ciclo de vida completo del panel parental (no solo Chatbot); requiere un cambio de estrategia de autenticación en el backend (ver Riesgo R1).

## Objetivo

- Cliente STOMP real basado en `@stomp/stompjs` conectado al endpoint `/ws/parent`.
- Conexión establecida al entrar en cualquier sección de `/panel` (no solo Chatbot), para poder emitir avisos generales (toasts) por el mismo canal además del futuro topic de chatbot.
- Desconexión al salir del árbol `/panel` (Documentación, "Salir", cierre de sesión por inactividad).
- Extensión de `useWSStore` para exponer suscripciones/publicación genéricas que el resto de sprints (chatbot) puedan reutilizar.

## Contexto

`ADR-010` describe de forma abstracta un `ParentChannel` como "WebSocket". El backend real (`WebSocketConfig.java`, `WebSocketAuthInterceptor.java`, módulo `session/infrastructure/websocket`) expone `/ws/parent` como broker **STOMP + SockJS**, con prefijo de aplicación `/app` y broker simple `/topic`. El cliente actual en `services/websocket.ts` (`createParentWebSocket`) es un `WebSocket` nativo sin framing STOMP — incompatible con ese endpoint. No tiene consumidores en el código (verificado), por lo que puede sustituirse sin regresión.

Decisiones de producto/técnicas confirmadas (análisis técnico 2026-08-11):
- Cliente STOMP nativo sobre WebSocket (sin `sockjs-client`), apuntando al transporte WebSocket directo del endpoint SockJS del backend.
- Conexión vinculada al layout `ParentPanelLayout.vue` (todo el panel), no solo a `ChatbotView.vue`.
- Estado de canal y de mensajes vive en `useWSStore` (extensión del store existente), no en un store nuevo.

### ⚠️ Riesgo bloqueante detectado: autenticación del handshake con WebSocket nativo

El backend actual valida el token leyendo la cabecera HTTP `Authorization: Bearer <token>` **durante el handshake** (`WebSocketAuthInterceptor implements HandshakeInterceptor`). La API `WebSocket` nativa del navegador **no permite establecer cabeceras HTTP personalizadas** en la petición de handshake — limitación de la plataforma, no de la librería cliente. Con el cliente nativo decidido, el token no puede llegar al interceptor tal como está implementado hoy.

| Opción | Descripción | Valoración |
|---|---|---|
| A. Token en query string (`/ws/parent/websocket?token=...`) | Backend añade lectura de `token` como query param | Funciona, pero expone el token en logs de acceso HTTP/proxies |
| **B. Autenticar en el frame STOMP `CONNECT`** | Mover la validación a un `ChannelInterceptor` sobre `StompCommand.CONNECT`, leyendo `Authorization` del frame STOMP (viaja en el payload WS, no en cabeceras HTTP). `@stomp/stompjs` lo soporta vía `connectHeaders` | Patrón estándar para clientes STOMP puros; no expone el token en URLs/logs. Requiere refactor de backend |

**Recomendación:** Opción B. Se marca como dependencia bloqueante de backend (ver "Dependencias bloqueantes").

## Diseño funcional-técnico

### 1. Dependencia nueva

`package.json`: añadir `@stomp/stompjs`. No se añade `sockjs-client`.

### 2. Servicio `stompParentClient.ts`

**Archivo:** `framework/frontend/app/src/services/stompParentClient.ts` (nuevo; convive con `services/websocket.ts`, que conserva `createGameWebSocket`/`WebSocketClient` para GameChannel sin cambios)

```typescript
import { Client, type IMessage, type StompSubscription } from '@stomp/stompjs'
import type { ConnectionStatus } from './websocket'

const WS_BASE_URL = import.meta.env.VITE_WS_BASE_URL || 'ws://localhost:8080'

export interface StompParentClientOptions {
  onStatusChange?: (status: ConnectionStatus) => void
}

export class StompParentClient {
  private client: Client
  private subscriptions = new Map<string, StompSubscription>()

  constructor(private options: StompParentClientOptions = {}) {
    this.client = new Client({
      brokerURL: `${WS_BASE_URL}/ws/parent/websocket`,
      reconnectDelay: 0, // backoff manual, ver connect()
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000
    })

    this.client.onConnect = () => this.options.onStatusChange?.('connected')
    this.client.onWebSocketClose = () => this.options.onStatusChange?.('disconnected')
    this.client.onStompError = () => this.options.onStatusChange?.('disconnected')
  }

  connect(token: string): void {
    this.client.connectHeaders = { Authorization: `Bearer ${token}` }
    this.options.onStatusChange?.('connecting')
    this.client.activate()
  }

  disconnect(): void {
    this.subscriptions.forEach((sub) => sub.unsubscribe())
    this.subscriptions.clear()
    this.client.deactivate()
    this.options.onStatusChange?.('disconnected')
  }

  subscribe(destination: string, callback: (message: IMessage) => void): void {
    if (this.subscriptions.has(destination)) return
    const sub = this.client.subscribe(destination, callback)
    this.subscriptions.set(destination, sub)
  }

  unsubscribe(destination: string): void {
    this.subscriptions.get(destination)?.unsubscribe()
    this.subscriptions.delete(destination)
  }

  publish(destination: string, body: unknown): void {
    this.client.publish({ destination, body: JSON.stringify(body) })
  }

  get connected(): boolean {
    return this.client.connected
  }
}
```

`reconnectDelay: 0` porque el backoff exponencial con jitter se gestiona manualmente (coherente con la política ya implementada en `WebSocketClient` para GameChannel, ADR-010). Extraer la fórmula de backoff a una función compartida `computeBackoffDelay()` en `services/websocket.ts` para no duplicarla.

### 3. Extensión de `useWSStore`

**Archivo:** `framework/frontend/app/src/stores/ws.ts` (modificar)

```typescript
const stompClient = shallowRef<StompParentClient | null>(null)

function connectParentChannel(token: string) {
  if (stompClient.value?.connected) return
  stompClient.value = new StompParentClient({ onStatusChange: setParentChannelStatus })
  stompClient.value.connect(token)
}

function disconnectParentChannel() {
  stompClient.value?.disconnect()
  stompClient.value = null
}

function subscribeParentTopic(destination: string, callback: (message: IMessage) => void) {
  stompClient.value?.subscribe(destination, callback)
}

function publishParentMessage(destination: string, body: unknown) {
  stompClient.value?.publish(destination, body)
}
```

`stompClient` usa `shallowRef` (no `ref`) para evitar que Vue intente hacer reactiva profundamente una instancia de clase. El resto de `useWSStore` (`gameChannelStatus`, `activeChannel`, etc.) queda sin cambios.

### 4. Ciclo de vida en `ParentPanelLayout.vue`

**Archivo:** `framework/frontend/app/src/layouts/ParentPanelLayout.vue` (modificar)

```typescript
import { useWSStore } from '../stores/ws'
import { useParentalAuthStore } from '../stores/parentalAuth'

const wsStore = useWSStore()
const authStore = useParentalAuthStore()

onMounted(() => {
  start() // ya existe (inactivity timer)
  if (authStore.token) {
    wsStore.connectParentChannel(authStore.token)
  }
})

onUnmounted(() => {
  stop()
  wsStore.disconnectParentChannel()
})
```

Al desmontarse `ParentPanelLayout` (navegar a `/docs`, volver a `Home` tras "Salir", o logout automático por inactividad) se cierra la conexión STOMP automáticamente, sin lógica adicional — el layout solo vive bajo `/panel`.

## Contratos y dependencias externas

### Contratos

- `docs/contracts/api/asyncapi/channels/parent.yaml`: confirma protocolo STOMP y requisito de `Authorization: Bearer <token>`; no especifica el mecanismo exacto (handshake vs. frame CONNECT). Actualizar el contrato tras cerrar la Opción B con backend para dejarlo explícito.

### Dependencias externas

| Capa | Dependencia | Estado |
|---|---|---|
| Backend | Mover autenticación de `WebSocketAuthInterceptor` de `HandshakeInterceptor` a `ChannelInterceptor` sobre `StompCommand.CONNECT` (Opción B) | ⏳ Pendiente — bloqueante |
| Backend | SPRINT-080-backend (streaming chatbot) no es requisito de este sprint, pero sí de SPRINT-035/036 | ⏳ Pendiente |
| Agents/TTS | Ninguna | ✅ Sin dependencia |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|---|---|---|
| R1 | WebSocket nativo no permite cabeceras HTTP en el handshake; el backend actual autentica ahí | ALTA | Backend mueve la validación al frame STOMP `CONNECT` (Opción B). Bloqueante para funcionar en cualquier entorno real. |
| R2 | Conectar STOMP en todas las secciones del panel abre una conexión aunque el usuario nunca use el chatbot | BAJA (decisión de producto asumida) | Aceptado explícitamente para poder emitir avisos/toasts generales por el mismo canal. |
| R3 | Duplicar lógica de backoff entre `WebSocketClient` (game) y `StompParentClient` (parent) | BAJA | Extraer `computeBackoffDelay()` compartido y reutilizar en ambos. |
| R4 | `shallowRef` mal aplicado puede causar pérdida de reactividad en componentes/tests | BAJA | Test unitario del store que verifique `parentChannelStatus` reactivo tras `connectParentChannel`. |

## Tareas del sprint

### Tarea 34.1: Añadir dependencia `@stomp/stompjs`

**Archivos:** `package.json`. **Criterios:** instalado; `npm run build` sin errores de tipos.

### Tarea 34.2: Implementar `StompParentClient`

**Archivo:** `services/stompParentClient.ts` (nuevo). **Criterios:** conecta con `connectHeaders.Authorization`; expone `subscribe/unsubscribe/publish/disconnect`; backoff exponencial con jitter en reconexión manual; sin cola de eventos offline (ADR-010).

### Tarea 34.3: Extender `useWSStore`

**Archivo:** `stores/ws.ts` (modificar). **Criterios:** `connectParentChannel`, `disconnectParentChannel`, `subscribeParentTopic`, `publishParentMessage`; `stompClient` como `shallowRef`; estado existente sin regresiones.

### Tarea 34.4: Vincular ciclo de vida en `ParentPanelLayout.vue`

**Archivo:** modificar. **Criterios:** conecta en `onMounted` si hay token; desconecta en `onUnmounted`; verificado manualmente que navegar a Documentación y volver reconecta.

### Tarea 34.5: Retirar `createParentWebSocket` sin uso

**Archivo:** `services/websocket.ts` (modificar). **Criterios:** eliminar la factory sin consumidores; `createGameWebSocket`/`WebSocketClient` intactos; `vue-tsc --noEmit` sin errores.

### Tarea 34.6: Coordinación backend — autenticación en CONNECT

No es una tarea de código frontend; es un handoff formal a backend. **Criterio de cierre:** confirmación explícita de backend de que la Opción B está implementada y desplegada en desarrollo antes de dar el sprint por verificable end-to-end.

## Archivos afectados

| Archivo | Tipo de cambio |
|---|---|
| `framework/frontend/app/package.json` | Modificación |
| `framework/frontend/app/src/services/stompParentClient.ts` | Nuevo |
| `framework/frontend/app/src/services/websocket.ts` | Modificación (retirar `createParentWebSocket`) |
| `framework/frontend/app/src/stores/ws.ts` | Modificación |
| `framework/frontend/app/src/layouts/ParentPanelLayout.vue` | Modificación |

## Estimación

- **Duración:** 2 días (+ tiempo de coordinación backend fuera de esta estimación)
- **Complejidad:** Media
- **Riesgo:** Alto (bloqueado por R1 hasta que backend confirme la Opción B)

## Criterios de aceptación del sprint

1. Al entrar en cualquier ruta bajo `/panel`, se abre una conexión STOMP autenticada contra `/ws/parent`.
2. La conexión se cierra al navegar a `/docs`, al pulsar "Salir" o al expirar la sesión por inactividad.
3. Navegar entre secciones del panel (Configuración, Niños, Chatbot, Lectura, Relajación) no reabre la conexión.
4. Ante caída de red, el cliente reintenta con backoff exponencial y expone el estado en `useWSStore.parentChannelStatus`.
5. No quedan referencias a `createParentWebSocket` en el código.
6. `vue-tsc --noEmit` sin errores.

## Evidencias esperadas

- Captura de DevTools → Network → WS mostrando el frame `CONNECT` con header `Authorization` y `CONNECTED` de respuesta.
- Prueba manual: entrar en `/panel/configuracion`, verificar conexión; navegar a `/panel/chatbot`, verificar que no se reconecta; ir a `/docs`, verificar desconexión; volver a `/panel`, verificar reconexión.
- Prueba manual de reconexión: cortar red unos segundos y verificar backoff + reconexión automática.

## Dependencias bloqueantes

- [ ] Backend implementa Opción B (autenticación en frame STOMP `CONNECT`) — sin esto, el cliente nativo no puede autenticarse.

## Handoffs a otras capas

### Backend

- Mover `WebSocketAuthInterceptor` de `HandshakeInterceptor` a un `ChannelInterceptor` que valide `Authorization` en `StompCommand.CONNECT`, almacenando `familyId`/`familySessionId` en los atributos de sesión STOMP igual que hoy.
- Valorar si el endpoint sigue necesitando `.withSockJS()` ahora que el frontend usa WebSocket puro (fuera de alcance de este sprint, solo señalarlo).

## Notas adicionales

- Este sprint no implementa nada específico del chatbot (topic, envío de mensajes) — solo la infraestructura de conexión genérica del panel. El consumo del topic `/topic/family/{familyId}/chatbot` se implementa en SPRINT-035.
- Se recomienda actualizar ADR-010 tras cerrar este sprint para reflejar STOMP como protocolo real de `ParentChannel` (hoy documentado de forma genérica como "WebSocket").
