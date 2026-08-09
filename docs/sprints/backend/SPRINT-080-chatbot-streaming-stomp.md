# SPRINT-080 — Infraestructura de streaming STOMP para chatbot

## Estado

- **Estado:** pending
- **Fecha de creación:** 2026-08-09
- **Responsable principal:** backend
- **Prioridad:** ALTA
- **Dependencias:** SPRINT-079 (historial), ADR-003 (aceptada), contratos AsyncAPI
- **Impacto estimado:** Streaming de respuestas del chatbot vía WebSocket STOMP. Deprecación del endpoint REST. Nuevos eventos y canales AsyncAPI. Integración con Spring AI streaming.

## Objetivo

Implementar la infraestructura de streaming para el chatbot parental que:
- Envíe tokens de respuesta en tiempo real vía WebSocket STOMP.
- Exponga tool calls del agente para transparencia (qué herramientas ejecuta, parámetros, resultados).
- Reutilice el canal STOMP existente `/ws/parent` para panel parental.
- Cree un nuevo topic separado `/topic/family/{familyId}/chatbot`.
- Depreque el endpoint REST `POST /api/v1/agents` inmediatamente.
- Persista conversaciones y mensajes usando SPRINT-079.
- Gestione ciclo de vida del modelo Ollama (start/stop).

## Contexto

**ADR-003** define el chatbot parental conversacional. Actualmente `AgentsService.sendMessage()` devuelve respuesta en bloque vía REST. Se requiere transformar a streaming para mejor experiencia de usuario.

**SPRINT-079** implementó la persistencia de historial de conversaciones. Este sprint consume esa infraestructura para persistir mensajes en tiempo real.

**Decisión de producto (2026-08-09):**
- Endpoint REST `POST /api/v1/agents`: DEPRECAR inmediatamente.
- Topic STOMP: Separado `/topic/family/{familyId}/chatbot`.
- Thinking/reasoning: DESCARTADO para esta versión.
- Timeout streaming: 60s por intento.

**Estado actual:**
- WebSocket STOMP existe en `/ws/parent` para panel parental.
- `WebSocketAuthInterceptor` ya valida `familyId` del token.
- `StompSubscribeInterceptor` valida suscripciones.
- Spring AI `ChatClient` soporta streaming vía `Flux<ChatResponse>`.

## Diseño funcional-técnico

### 1. Arquitectura de streaming

**Flujo completo:**

```
[Cliente STOMP] --CHATBOT_SEND--> /app/chatbot/send
                                        |
                                   [ChatbotStompController]
                                        |
                                   [ChatbotStreamService]
                                        |
                            ┌───────────┴───────────┐
                            │ 1. Validar + sanitizar │
                            │ 2. Crear conversación  │
                            │ 3. Start modelo si STOP│
                            │ 4. ChatClient.stream() │
                            │ 5. Publish TOKEN       │
                            │ 6. Persistir mensaje   │
                            │ 7. Publish COMPLETE    │
                            │ 8. Stop modelo         │
                            └───────────┬───────────┘
                                        |
[Cliente STOMP] <--eventos-- /topic/family/{familyId}/chatbot
```

**Integración Spring AI streaming + STOMP:**

- `ChatClient.prompt(sanitized).stream().chatResponse()` devuelve `Flux<ChatResponse>`.
- Se consume el `Flux` mediante `subscribe()` en hilo del task scheduler.
- Cada `ChatResponse` chunk extrae token: `Generation.getOutput().getText()`.
- Cada token se publica vía `SimpMessagingTemplate.convertAndSend()` al topic.
- Al completar, se publica `CHATBOT_COMPLETE` con respuesta acumulada.

### 2. Nuevos componentes backend

| Componente | Paquete | Responsabilidad |
|---|---|---|
| `ChatbotStompController` | `agents.infrastructure.web.stomp` | Recibe `CHATBOT_SEND` vía `@MessageMapping("/chatbot/send")` |
| `ChatbotStreamService` | `agents.service` | Orquesta streaming, persistencia, ciclo de vida |
| `ChatbotEventPublisher` | `agents.infrastructure.websocket` | Publica eventos en topic STOMP |
| `ChatbotStreamUseCase` | `agents.ports.in` | UseCase: `void sendMessageStreaming(String message, Long familyId)` |
| `ChatbotStreamEvent` | `agents.model` | Record con tipo de evento + payload |
| `ChatbotEventType` | `agents.model` | Enum: `TOKEN`, `COMPLETE`, `ERROR` |

### 3. Modelo de eventos — ChatbotStreamEvent

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/model/ChatbotEventType.java` (nuevo)

**Especificación:**
```java
package es.vargontoc.educational.framework.agents.model;

public enum ChatbotEventType {
    TOKEN,
    TOOL_CALL_START,
    TOOL_CALL_RESULT,
    COMPLETE,
    ERROR
}
```

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/model/ChatbotStreamEvent.java` (nuevo)

**Especificación:**
```java
package es.vargontoc.educational.framework.agents.model;

import java.util.UUID;

public record ChatbotStreamEvent(
    ChatbotEventType event,
    UUID conversationId,
    String content,
    Integer attempt,
    ToolCallInfo toolCall
) {
    public static ChatbotStreamEvent token(UUID conversationId, String token) {
        return new ChatbotStreamEvent(ChatbotEventType.TOKEN, conversationId, token, null, null);
    }

    public static ChatbotStreamEvent complete(UUID conversationId, String fullResponse) {
        return new ChatbotStreamEvent(ChatbotEventType.COMPLETE, conversationId, fullResponse, null, null);
    }

    public static ChatbotStreamEvent error(UUID conversationId, String errorMessage, int attempt) {
        return new ChatbotStreamEvent(ChatbotEventType.ERROR, conversationId, errorMessage, attempt, null);
    }

    public static ChatbotStreamEvent toolCallStart(UUID conversationId, ToolCallInfo toolCall) {
        return new ChatbotStreamEvent(ChatbotEventType.TOOL_CALL_START, conversationId, null, null, toolCall);
    }

    public static ChatbotStreamEvent toolCallResult(UUID conversationId, ToolCallInfo toolCall) {
        return new ChatbotStreamEvent(ChatbotEventType.TOOL_CALL_RESULT, conversationId, null, null, toolCall);
    }
}
```

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/model/ToolCallInfo.java` (nuevo)

**Especificación:**
```java
package es.vargontoc.educational.framework.agents.model;

import java.time.OffsetDateTime;

public record ToolCallInfo(
    String toolName,
    String parameters,
    String status,
    String summary,
    Long durationMs,
    OffsetDateTime timestamp
) {
    public static ToolCallInfo start(String toolName, String parameters) {
        return new ToolCallInfo(toolName, parameters, "STARTED", null, null, OffsetDateTime.now());
    }

    public static ToolCallInfo result(String toolName, String status, String summary, Long durationMs) {
        return new ToolCallInfo(toolName, null, status, summary, durationMs, OffsetDateTime.now());
    }
}
```

### 4. Puerto de entrada — ChatbotStreamUseCase

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/ports/in/ChatbotStreamUseCase.java` (nuevo)

**Especificación:**
```java
package es.vargontoc.educational.framework.agents.ports.in;

import es.vargontoc.educational.framework.shared.exception.ValidationException;

public interface ChatbotStreamUseCase {

    void sendMessageStreaming(String message, Long familyId) throws ValidationException;
}
```

### 5. Publisher de eventos — ChatbotEventPublisher

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/websocket/ChatbotEventPublisher.java` (nuevo)

**Responsabilidad:** Publica eventos de chatbot en topic STOMP dedicado.

**Especificación:**
```java
package es.vargontoc.educational.framework.agents.infrastructure.websocket;

import es.vargontoc.educational.framework.agents.model.ChatbotStreamEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class ChatbotEventPublisher {

    private static final String TOPIC_PATTERN = "/topic/family/%d/chatbot";
    
    private final SimpMessagingTemplate messagingTemplate;

    public ChatbotEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publish(Long familyId, ChatbotStreamEvent event) {
        String destination = String.format(TOPIC_PATTERN, familyId);
        messagingTemplate.convertAndSend(destination, event);
    }
}
```

### 6. Servicio de streaming — ChatbotStreamService

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/service/ChatbotStreamService.java` (nuevo)

**Responsabilidad:** Orquesta streaming, persistencia, ciclo de vida del modelo.

**Especificación:**
```java
package es.vargontoc.educational.framework.agents.service;

import es.vargontoc.educational.framework.agents.infrastructure.websocket.ChatbotEventPublisher;
import es.vargontoc.educational.framework.agents.model.AgentStatusType;
import es.vargontoc.educational.framework.agents.model.ChatbotConversation;
import es.vargontoc.educational.framework.agents.model.ChatbotStreamEvent;
import es.vargontoc.educational.framework.agents.ports.in.ChatbotHistoryUseCase;
import es.vargontoc.educational.framework.agents.ports.in.ChatbotStreamUseCase;
import es.vargontoc.educational.framework.agents.ports.out.OllamaManagementPort;
import es.vargontoc.educational.framework.agents.utils.AgentsConstants;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatbotStreamService implements ChatbotStreamUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(ChatbotStreamService.class);
    private static final int STREAM_TIMEOUT_SECONDS = 60;

    private final Map<String, ChatClient> agents;
    private final OllamaManagementPort ollamaPort;
    private final ChatbotEventPublisher eventPublisher;
    private final ChatbotHistoryUseCase historyUseCase;
    private final ConcurrentHashMap<Long, UUID> activeStreams = new ConcurrentHashMap<>();

    public ChatbotStreamService(
            Map<String, ChatClient> agents,
            OllamaManagementPort ollamaPort,
            ChatbotEventPublisher eventPublisher,
            ChatbotHistoryUseCase historyUseCase) {
        this.agents = agents;
        this.ollamaPort = ollamaPort;
        this.eventPublisher = eventPublisher;
        this.historyUseCase = historyUseCase;
    }

    @Override
    public void sendMessageStreaming(String message, Long familyId) throws ValidationException {
        // Validación
        if (message == null || message.trim().isEmpty()) {
            throw new ValidationException("El mensaje no puede estar vacío");
        }

        // Protección contra múltiples streams
        if (activeStreams.containsKey(familyId)) {
            UUID existingConversationId = activeStreams.get(familyId);
            eventPublisher.publish(familyId, 
                ChatbotStreamEvent.error(existingConversationId, "Conversación en progreso", 0));
            return;
        }

        // Sanitizar
        String sanitized = sanitize(message);

        // Crear conversación
        ChatbotConversation conversation = historyUseCase.createConversation(familyId);
        UUID conversationId = conversation.getConversationId();

        // Registrar stream activo
        activeStreams.put(familyId, conversationId);

        try {
            // Arrancar modelo si está STOPPED
            if (checkStatus(AgentsConstants.CHATBOT_MODEL).status() == AgentStatusType.STOPPED) {
                ollamaPort.run(AgentsConstants.CHATBOT_MODEL, false, null);
            }

            // Guardar mensaje del usuario
            historyUseCase.addMessage(conversation.getId(), "USER", sanitized);

            // Streaming
            StringBuilder fullResponse = new StringBuilder();
            
            Flux<org.springframework.ai.chat.prompt.ChatResponse> flux = 
                agents.get(AgentsConstants.CHATBOT_MODEL)
                    .prompt(sanitized)
                    .stream()
                    .chatResponse();

            flux.timeout(Duration.ofSeconds(STREAM_TIMEOUT_SECONDS))
                .doOnNext(chatResponse -> {
                    // 1. Extraer y publicar tool calls (si existen)
                    extractAndPublishToolCalls(chatResponse, familyId, conversationId);
                    
                    // 2. Extraer tokens de texto
                    String token = chatResponse.getResult().getOutput().getText();
                    if (token != null && !token.isEmpty()) {
                        fullResponse.append(token);
                        eventPublisher.publish(familyId, ChatbotStreamEvent.token(conversationId, token));
                    }
                })
                .doOnComplete(() -> {
                    // Guardar respuesta completa
                    historyUseCase.addMessage(conversation.getId(), "ASSISTANT", fullResponse.toString());
                    
                    // Publicar COMPLETE
                    eventPublisher.publish(familyId, 
                        ChatbotStreamEvent.complete(conversationId, fullResponse.toString()));
                    
                    // Detener modelo
                    ollamaPort.stop(AgentsConstants.CHATBOT_MODEL);
                    
                    // Limpiar stream activo
                    activeStreams.remove(familyId);
                    
                    LOG.info("Streaming completado para familyId={}, conversationId={}", familyId, conversationId);
                })
                .doOnError(error -> {
                    LOG.error("Error en streaming para familyId={}: {}", familyId, error.getMessage());
                    eventPublisher.publish(familyId, 
                        ChatbotStreamEvent.error(conversationId, "Error al generar respuesta", 1));
                    
                    // Detener modelo en caso de error
                    ollamaPort.stop(AgentsConstants.CHATBOT_MODEL);
                    
                    // Limpiar stream activo
                    activeStreams.remove(familyId);
                })
                .subscribe();

        } catch (Exception e) {
            LOG.error("Error iniciando streaming para familyId={}: {}", familyId, e.getMessage());
            eventPublisher.publish(familyId, 
                ChatbotStreamEvent.error(conversationId, "Error al iniciar conversación", 1));
            activeStreams.remove(familyId);
        }
    }

    private void extractAndPublishToolCalls(
            org.springframework.ai.chat.prompt.ChatResponse chatResponse,
            Long familyId,
            UUID conversationId) {
        try {
            var generation = chatResponse.getResult();
            if (generation == null || generation.getOutput() == null) {
                return;
            }

            // Spring AI 2.0.0 expone tool calls en Generation.getOutput().getToolCalls()
            var toolCalls = generation.getOutput().getToolCalls();
            if (toolCalls == null || toolCalls.isEmpty()) {
                return;
            }

            for (var toolCall : toolCalls) {
                String toolName = toolCall.name();
                String arguments = toolCall.arguments();
                
                // Publicar TOOL_CALL_START
                var toolCallInfo = es.vargontoc.educational.framework.agents.model.ToolCallInfo.start(
                    toolName, arguments
                );
                eventPublisher.publish(familyId, 
                    ChatbotStreamEvent.toolCallStart(conversationId, toolCallInfo));
                
                LOG.info("Tool call iniciada: {} con argumentos: {}", toolName, arguments);
            }
        } catch (Exception e) {
            LOG.warn("Error extrayendo tool calls: {}", e.getMessage());
        }
    }

    private String sanitize(String input) {
        return Jsoup.clean(input, Safelist.none());
    }

    private es.vargontoc.educational.framework.agents.model.AgentStatus checkStatus(String model) {
        try {
            if (ollamaPort.isRunning(model)) {
                return new es.vargontoc.educational.framework.agents.model.AgentStatus(model, AgentStatusType.RUNNING);
            }
            if (ollamaPort.isPulled(model)) {
                return new es.vargontoc.educational.framework.agents.model.AgentStatus(model, AgentStatusType.STOPPED);
            }
            return new es.vargontoc.educational.framework.agents.model.AgentStatus(model, AgentStatusType.UNREACHABLE);
        } catch (Exception e) {
            return new es.vargontoc.educational.framework.agents.model.AgentStatus(model, AgentStatusType.UNREACHABLE);
        }
    }
}
```

**Nota:** Este servicio NO incluye reintentos (SPRINT-081). Solo manejo básico de errores.

### 7. Controlador STOMP — ChatbotStompController

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/web/stomp/ChatbotStompController.java` (nuevo)

**Responsabilidad:** Recibe mensajes del chatbot vía STOMP.

**Especificación:**
```java
package es.vargontoc.educational.framework.agents.infrastructure.web.stomp;

import es.vargontoc.educational.framework.agents.infrastructure.dto.AgentRequestDto;
import es.vargontoc.educational.framework.agents.ports.in.ChatbotStreamUseCase;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatbotStompController {

    private static final Logger LOG = LoggerFactory.getLogger(ChatbotStompController.class);
    private static final String ATTR_FAMILY_ID = "familyId";

    private final ChatbotStreamUseCase streamUseCase;

    public ChatbotStompController(ChatbotStreamUseCase streamUseCase) {
        this.streamUseCase = streamUseCase;
    }

    @MessageMapping("/chatbot/send")
    public void handleChatbotMessage(
            AgentRequestDto request,
            org.springframework.messaging.simp.SimpMessageHeaderAccessor accessor) {
        
        try {
            Long familyId = (Long) accessor.getSessionAttributes().get(ATTR_FAMILY_ID);
            
            if (familyId == null) {
                LOG.warn("familyId no encontrado en sesión STOMP");
                return;
            }

            LOG.info("Mensaje de chatbot recibido de familyId={}", familyId);
            streamUseCase.sendMessageStreaming(request.message(), familyId);

        } catch (ValidationException e) {
            LOG.warn("Validación fallida: {}", e.getMessage());
            // El error se publica vía eventPublisher en el servicio
        } catch (Exception e) {
            LOG.error("Error procesando mensaje de chatbot: {}", e.getMessage(), e);
        }
    }
}
```

**Nota:** El método es `void` porque la respuesta viaja por el topic STOMP, no como reply directo.

### 8. Actualización de StompSubscribeInterceptor

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/session/infrastructure/websocket/stomp/StompSubscribeInterceptor.java` (actualizar)

**Responsabilidad:** Ampliar para permitir suscripción a `/topic/family/{familyId}/chatbot`.

**Cambio requerido:**
```java
// Actualizar patrón regex para incluir /chatbot
private static final Pattern VALID_DESTINATION_PATTERN = 
    Pattern.compile("^/topic/family/(\\d+)/(sessions|chatbot)$");
```

### 9. Deprecación del endpoint REST

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/web/AgentsController.java` (actualizar)

**Cambio requerido:**
```java
@PostMapping
@Deprecated(since = "2026-08-09", forRemoval = true)
@Operation(
    summary = "Enviar mensaje al chatbot (DEPRECADO)",
    description = "Este endpoint está deprecado. Use WebSocket STOMP /app/chatbot/send en su lugar."
)
public ResponseEntity<ApiResponse<String>> responseChatbot(@RequestBody AgentRequestDto request){
    return ResponseEntity.ok(ApiResponse.ok(send.sendMessage(request.message())));
}
```

### 10. Contratos AsyncAPI

**Archivo:** `docs/contracts/api/asyncapi/channels/chatbot.yaml` (nuevo)

```yaml
protocol: stomp
description: Chatbot streaming channel for parental panel. Subscriptions require Authorization: Bearer <token> during handshake.
subscribe:
  message:
    $ref: "../messages/chatbot-stream-event.yaml"
publish:
  message:
    $ref: "../messages/chatbot-send-message.yaml"
```

**Archivo:** `docs/contracts/api/asyncapi/messages/chatbot-stream-event.yaml` (nuevo)

```yaml
payload:
  $ref: "../schemas/chatbot-stream-payload.yaml"
```

**Archivo:** `docs/contracts/api/asyncapi/messages/chatbot-send-message.yaml` (nuevo)

```yaml
payload:
  $ref: "../schemas/chatbot-send-payload.yaml"
```

**Archivo:** `docs/contracts/api/asyncapi/schemas/chatbot-stream-payload.yaml` (nuevo)

```yaml
type: object
required:
  - event
  - conversationId
properties:
  event:
    type: string
    enum: [TOKEN, TOOL_CALL_START, TOOL_CALL_RESULT, COMPLETE, ERROR]
    description: Tipo de evento de streaming
  conversationId:
    type: string
    format: uuid
    description: Identificador de la conversación para correlacionar eventos
  content:
    type: string
    description: Token de respuesta (TOKEN), respuesta completa (COMPLETE), o mensaje de error (ERROR)
  attempt:
    type: integer
    description: Número de intento (solo presente en ERROR)
  toolCall:
    $ref: "./tool-call-info-payload.yaml"
    description: Información de tool call (solo presente en TOOL_CALL_START y TOOL_CALL_RESULT)
```

**Archivo:** `docs/contracts/api/asyncapi/schemas/tool-call-info-payload.yaml` (nuevo)

```yaml
type: object
properties:
  toolName:
    type: string
    description: Nombre de la herramienta invocada (ej. "getChild", "getAllChilds")
  parameters:
    type: string
    description: Parámetros de la tool call en formato JSON (solo en TOOL_CALL_START)
  status:
    type: string
    enum: [STARTED, SUCCESS, ERROR]
    description: Estado de la tool call
  summary:
    type: string
    description: Resumen del resultado (solo en TOOL_CALL_RESULT, sin datos sensibles)
  durationMs:
    type: integer
    description: Duración de la tool call en milisegundos (solo en TOOL_CALL_RESULT)
  timestamp:
    type: string
    format: date-time
    description: Timestamp de cuando ocurrió el evento
```

**Archivo:** `docs/contracts/api/asyncapi/schemas/chatbot-send-payload.yaml` (nuevo)

```yaml
type: object
additionalProperties: false
required:
  - message
properties:
  message:
    type: string
    description: Mensaje a enviar al chatbot
```

**Archivo:** `docs/contracts/api/asyncapi/websocket.yaml` (actualizar)

Añadir referencia al nuevo canal:
```yaml
channels:
  /ws/parent:
    $ref: "./channels/parent.yaml"
  /topic/family/{familyId}/chatbot:
    $ref: "./channels/chatbot.yaml"
  /topic/family/{familyId}/sessions:
    $ref: "./channels/family-sessions.yaml"
  /ws/game:
    $ref: "./channels/game.yaml"
```

Añadir nuevos eventos:
```yaml
components:
  events:
    CHATBOT_TOKEN:
      direction: server_to_client
      description: Token de respuesta del chatbot en streaming.
    CHATBOT_TOOL_CALL_START:
      direction: server_to_client
      description: El agente ha invocado una herramienta. Contiene nombre de la herramienta y parámetros.
    CHATBOT_TOOL_CALL_RESULT:
      direction: server_to_client
      description: Resultado de la tool call. Contiene nombre, estado, resumen y duración.
    CHATBOT_COMPLETE:
      direction: server_to_client
      description: Respuesta del chatbot completada. Contiene respuesta completa acumulada.
    CHATBOT_ERROR:
      direction: server_to_client
      description: Error en streaming del chatbot. Contiene mensaje de error y número de intento.
```

## Contratos y dependencias externas

### Contratos AsyncAPI

Nuevos archivos en `docs/contracts/api/asyncapi/`:
- `channels/chatbot.yaml`
- `messages/chatbot-stream-event.yaml`
- `messages/chatbot-send-message.yaml`
- `schemas/chatbot-stream-payload.yaml`
- `schemas/chatbot-send-payload.yaml`
- `schemas/tool-call-info-payload.yaml`

Actualización de `websocket.yaml` para incluir nuevo canal y eventos.

### Dependencias externas

| Capa | Dependencia | Estado |
|------|-------------|--------|
| Frontend | Consumir eventos STOMP en `/topic/family/{familyId}/chatbot` | ⏳ Pendiente |
| Frontend | Enviar mensajes vía STOMP SEND a `/app/chatbot/send` | ⏳ Pendiente |
| Frontend | Deprecar uso de `POST /api/v1/agents` | ⏳ Pendiente |
| Agents | Ninguna | ✅ Sin dependencia |
| TTS | Ninguna | ✅ Sin dependencia |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | Latencia de arranque del modelo (5-30s) | MEDIA | Publicar evento de "preparando" (fuera de este sprint). Streaming comienza inmediatamente después. |
| R2 | Múltiples streams concurrentes por familia | ALTA | `ConcurrentHashMap` protege contra múltiples streams. Rechazo con `CHATBOT_ERROR`. |
| R3 | Timeout de streaming (60s) | MEDIA | `Flux.timeout()` cancela stream si excede timeout. Publicar `CHATBOT_ERROR`. |
| R4 | Desconexión del cliente mid-stream | MEDIA | Stream continúa en servidor. Tokens se pierden si cliente no reconecta. Futuro: persistencia de tokens. |
| R5 | Backpressure en WebSocket | BAJA | Para 5-6 usuarios concurrentes no es crítico. Optimización futura si needed. |

---

## Tareas del sprint

### Tarea 80.1: Crear modelo de eventos ChatbotStreamEvent y ChatbotEventType

**Descripción:** Enum y record para eventos de streaming.

**Archivos:**
- `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/model/ChatbotEventType.java` (nuevo)
- `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/model/ChatbotStreamEvent.java` (nuevo)

**Criterios de aceptación:**
- Enum con valores `TOKEN`, `COMPLETE`, `ERROR`.
- Record con campos `event`, `conversationId`, `content`, `attempt`.
- Métodos estáticos `token()`, `complete()`, `error()`.
- Compilación sin errores.

---

### Tarea 80.2: Implementar puerto ChatbotStreamUseCase

**Descripción:** Interface del caso de uso para streaming.

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/ports/in/ChatbotStreamUseCase.java` (nuevo)

**Criterios de aceptación:**
- Define `void sendMessageStreaming(String message, Long familyId)`.
- Compilación sin errores.

---

### Tarea 80.3: Implementar ChatbotEventPublisher

**Descripción:** Componente para publicar eventos en topic STOMP.

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/websocket/ChatbotEventPublisher.java` (nuevo)

**Criterios de aceptación:**
- Inyecta `SimpMessagingTemplate`.
- Método `publish(Long familyId, ChatbotStreamEvent event)`.
- Publica en `/topic/family/{familyId}/chatbot`.
- `@Component`.
- Compilación sin errores.

---

### Tarea 80.4: Implementar ChatbotStreamService

**Descripción:** Servicio que orquesta streaming, persistencia, ciclo de vida y extracción de tool calls.

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/service/ChatbotStreamService.java` (nuevo)

**Criterios de aceptación:**
- Implementa `ChatbotStreamUseCase`.
- Valida y sanitiza mensaje.
- Crea conversación vía `ChatbotHistoryUseCase`.
- Arranca modelo si está STOPPED.
- Usa `ChatClient.prompt().stream().chatResponse()`.
- Publica `CHATBOT_TOKEN` por cada token.
- **Extrae tool calls de `ChatResponse` y publica `CHATBOT_TOOL_CALL_START`.**
- Publica `CHATBOT_COMPLETE` al finalizar.
- Persiste mensajes USER y ASSISTANT.
- Detiene modelo al completar.
- Protege contra múltiples streams con `ConcurrentHashMap`.
- Timeout de 60s con `Flux.timeout()`.
- `@Service`.
- Compilación sin errores.

---

### Tarea 80.5: Implementar ChatbotStompController

**Descripción:** Controlador STOMP para recibir mensajes del chatbot.

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/web/stomp/ChatbotStompController.java` (nuevo)

**Criterios de aceptación:**
- `@Controller`.
- `@MessageMapping("/chatbot/send")`.
- Extrae `familyId` de `SimpMessageHeaderAccessor.getSessionAttributes()`.
- Delega a `ChatbotStreamUseCase`.
- Método `void` (respuesta viaja por topic).
- Compilación sin errores.

---

### Tarea 80.6: Actualizar StompSubscribeInterceptor

**Descripción:** Ampliar patrón regex para permitir suscripción a `/topic/family/{familyId}/chatbot`.

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/session/infrastructure/websocket/stomp/StompSubscribeInterceptor.java` (actualizar)

**Criterios de aceptación:**
- Patrón regex acepta `/topic/family/{familyId}/chatbot`.
- Valida `familyId` contra sesión autenticada.
- Tests de integración verifican autorización.

---

### Tarea 80.7: Deprecar endpoint REST POST /api/v1/agents

**Descripción:** Marcar endpoint como deprecado con anotaciones y documentación.

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/web/AgentsController.java` (actualizar)

**Criterios de aceptación:**
- Anotación `@Deprecated(since = "2026-08-09", forRemoval = true)`.
- `@Operation` con descripción de deprecación.
- Endpoint sigue funcionando (no se elimina).
- Swagger muestra advertencia de deprecación.

---

### Tarea 80.8: Crear contratos AsyncAPI

**Descripción:** Definir canales, mensajes y schemas para streaming de chatbot incluyendo tool calls.

**Archivos:**
- `docs/contracts/api/asyncapi/channels/chatbot.yaml` (nuevo)
- `docs/contracts/api/asyncapi/messages/chatbot-stream-event.yaml` (nuevo)
- `docs/contracts/api/asyncapi/messages/chatbot-send-message.yaml` (nuevo)
- `docs/contracts/api/asyncapi/schemas/chatbot-stream-payload.yaml` (nuevo)
- `docs/contracts/api/asyncapi/schemas/chatbot-send-payload.yaml` (nuevo)
- `docs/contracts/api/asyncapi/schemas/tool-call-info-payload.yaml` (nuevo)
- `docs/contracts/api/asyncapi/websocket.yaml` (actualizar)

**Criterios de aceptación:**
- Canal `/topic/family/{familyId}/chatbot` definido.
- Mensajes de evento y envío definidos.
- Schemas definen estructura de payloads incluyendo toolCall.
- Schema `tool-call-info-payload.yaml` define estructura de tool calls.
- `websocket.yaml` incluye nuevo canal y eventos (CHATBOT_TOOL_CALL_START, CHATBOT_TOOL_CALL_RESULT).
- Coherencia con implementación.

---

### Tarea 80.9: Pruebas de integración

**Descripción:** Tests de integración para validar streaming y aislamiento.

**Archivo:** `framework/backend/src/test/java/es/vargontoc/educational/framework/agents/ChatbotStreamIntegrationTest.java` (nuevo)

**Criterios de aceptación:**
- Test: enviar mensaje STOMP y recibir eventos `TOKEN` + `COMPLETE`.
- Test: cada evento incluye `conversationId` válido.
- Test: familia A no recibe eventos de familia B.
- Test: segundo mensaje mientras stream activo produce `CHATBOT_ERROR`.
- Test: timeout de 60s cancela stream y publica `CHATBOT_ERROR`.
- Tests pasan con Testcontainers.

---

### Tarea 80.10: Implementar extracción y publicación de tool calls

**Descripción:** Extraer tool calls de `ChatResponse` de Spring AI y publicar eventos `CHATBOT_TOOL_CALL_START` en el topic STOMP para transparencia del agente.

**Archivos:**
- `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/model/ToolCallInfo.java` (nuevo)
- `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/service/ChatbotStreamService.java` (actualizar)

**Responsabilidad:**
- Extraer tool calls de `Generation.getOutput().getToolCalls()` en cada `ChatResponse`.
- Publicar evento `CHATBOT_TOOL_CALL_START` con nombre de herramienta y parámetros.
- Manejar errores de extracción sin interrumpir el streaming.

**Especificación técnica:**
```java
private void extractAndPublishToolCalls(
        org.springframework.ai.chat.prompt.ChatResponse chatResponse,
        Long familyId,
        UUID conversationId) {
    try {
        var generation = chatResponse.getResult();
        if (generation == null || generation.getOutput() == null) {
            return;
        }

        // Spring AI 2.0.0 expone tool calls en Generation.getOutput().getToolCalls()
        var toolCalls = generation.getOutput().getToolCalls();
        if (toolCalls == null || toolCalls.isEmpty()) {
            return;
        }

        for (var toolCall : toolCalls) {
            String toolName = toolCall.name();
            String arguments = toolCall.arguments();
            
            // Publicar TOOL_CALL_START
            var toolCallInfo = ToolCallInfo.start(toolName, arguments);
            eventPublisher.publish(familyId, 
                ChatbotStreamEvent.toolCallStart(conversationId, toolCallInfo));
            
            LOG.info("Tool call iniciada: {} con argumentos: {}", toolName, arguments);
        }
    } catch (Exception e) {
        LOG.warn("Error extrayendo tool calls: {}", e.getMessage());
    }
}
```

**Criterios de aceptación:**
- Se extraen tool calls de `ChatResponse` cuando están presentes.
- Se publica `CHATBOT_TOOL_CALL_START` con `toolName` y `parameters`.
- Los errores de extracción no interrumpen el streaming.
- Logs informativos de tool calls ejecutadas.
- Compilación sin errores.

**Nota:** Las herramientas actuales del chatbot son `getAllChilds` y `getChild` (definidas en `ChildTools.java`). El modelo decidirá cuándo invocarlas según el contexto de la pregunta del usuario.

---

## Archivos afectados

| Archivo | Tipo de cambio |
|---------|---------------|
| `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/model/ChatbotEventType.java` | Nuevo |
| `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/model/ChatbotStreamEvent.java` | Nuevo |
| `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/model/ToolCallInfo.java` | Nuevo |
| `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/ports/in/ChatbotStreamUseCase.java` | Nuevo |
| `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/websocket/ChatbotEventPublisher.java` | Nuevo |
| `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/service/ChatbotStreamService.java` | Nuevo |
| `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/web/stomp/ChatbotStompController.java` | Nuevo |
| `framework/backend/src/main/java/es/vargontoc/educational/framework/session/infrastructure/websocket/stomp/StompSubscribeInterceptor.java` | Actualizar |
| `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/infrastructure/web/AgentsController.java` | Actualizar |
| `framework/backend/src/test/java/es/vargontoc/educational/framework/agents/ChatbotStreamIntegrationTest.java` | Nuevo |
| `docs/contracts/api/asyncapi/channels/chatbot.yaml` | Nuevo |
| `docs/contracts/api/asyncapi/messages/chatbot-stream-event.yaml` | Nuevo |
| `docs/contracts/api/asyncapi/messages/chatbot-send-message.yaml` | Nuevo |
| `docs/contracts/api/asyncapi/schemas/chatbot-stream-payload.yaml` | Nuevo |
| `docs/contracts/api/asyncapi/schemas/chatbot-send-payload.yaml` | Nuevo |
| `docs/contracts/api/asyncapi/schemas/tool-call-info-payload.yaml` | Nuevo |
| `docs/contracts/api/asyncapi/websocket.yaml` | Actualizar |

## Estimación

- **Duración:** 4 días (3 días base + 1 día tool calling)
- **Complejidad:** Alta
- **Riesgo:** Alto (streaming en tiempo real, integración WebSocket, ciclo de vida del modelo, extracción de tool calls)

## Criterios de aceptación del sprint

1. Un mensaje STOMP a `/app/chatbot/send` produce eventos `TOKEN` + `COMPLETE` en `/topic/family/{familyId}/chatbot`. *(Funcionalidad)*
2. Cada evento incluye `conversationId` válido (UUID). *(Contrato)*
3. Familia A no recibe eventos de familia B. *(Seguridad)*
4. Si hay un stream activo para una familia, un nuevo mensaje produce `CHATBOT_ERROR`. *(Funcionalidad)*
5. El modelo se arranca si estaba STOPPED y se detiene tras completar. *(Ciclo de vida)*
6. Los mensajes USER y ASSISTANT se persisten en la conversación. *(Persistencia)*
7. Timeout de 60s cancela stream y publica `CHATBOT_ERROR`. *(Resiliencia)*
8. Endpoint REST `POST /api/v1/agents` está marcado como deprecado. *(Deprecación)*
9. Contratos AsyncAPI están actualizados y son coherentes con la implementación. *(Contratos)*
10. **Cuando el chatbot invoca herramientas, se publican eventos `CHATBOT_TOOL_CALL_START` con `toolName` y `parameters`.** *(Transparencia)*
11. **Los errores de extracción de tool calls no interrumpen el streaming.** *(Robustez)*
12. Compilación sin errores. *(Calidad)*
13. Tests de integración pasando. *(Calidad)*

## Dependencias bloqueantes

- [x] SPRINT-079 completado (persistencia de historial).
- [x] ADR-003 aceptada.
- [x] Decisiones de producto confirmadas.

## Handoffs a otras capas

### Frontend:
- Consumir eventos STOMP en `/topic/family/{familyId}/chatbot`.
- Renderizar `TOKEN` acumulando texto progresivamente.
- **Renderizar `TOOL_CALL_START` mostrando qué herramientas ejecuta el agente (panel colapsable o sección de transparencia).**
- `COMPLETE`: marcar respuesta como finalizada.
- `ERROR`: mostrar mensaje de error.
- Enviar mensajes vía STOMP SEND a `/app/chatbot/send`.
- Correlacionar eventos por `conversationId`.
- Deprecar uso de `POST /api/v1/agents`.

### Agents/TTS:
- Sin dependencia.

## Notas adicionales

### Privacidad infantil

- Solo adultos autenticados acceden al chatbot.
- Conversaciones aisladas entre familias.
- No se almacenan datos de menores.
- Contenido sanitizado antes de persistir.

### Preparación para SPRINT-081

Este sprint prepara para reintentos en SPRINT-081:
- Estructura de eventos `CHATBOT_ERROR` con campo `attempt`.
- Lógica de ciclo de vida del modelo (no detener entre reintentos).
- Manejo básico de errores (sin reintentos aún).

### Transparencia del agente (Tool Calling)

El chatbot parental usa herramientas para consultar información de la familia:
- `getAllChilds`: Obtiene todos los perfiles infantiles registrados.
- `getChild`: Obtiene un perfil infantil específico por nombre.

**Flujo con transparencia:**
```
Usuario: "¿Cómo va María en matemáticas?"
  ↓
[CHATBOT_TOKEN] "Voy a consultar el progreso de María..."
  ↓
[CHATBOT_TOOL_CALL_START] tool: "getChild", params: { name: "María" }
  ↓
[CHATBOT_TOKEN] "Según el progreso orientativo, María..."
[CHATBOT_TOKEN] "ha completado 5 actividades de matemáticas..."
  ↓
[CHATBOT_COMPLETE]
```

**Beneficios:**
- El usuario ve qué herramientas ejecuta el agente.
- Transparencia sobre qué datos consulta el chatbot.
- Cumple ADR-003: "límites explícitos cuando una pregunta queda fuera de alcance".

**Consideraciones de privacidad:**
- Los `parameters` de tool calls NO deben exponer datos sensibles.
- El `summary` de tool calls es un resumen, no el resultado completo.
- Frontend debe renderizar tool calls de forma clara pero no técnica.

### Migración frontend

El frontend debe migrar de REST a STOMP en este sprint:
- Reemplazar llamadas a `POST /api/v1/agents` con STOMP SEND.
- Implementar suscripción a `/topic/family/{familyId}/chatbot`.
- Renderizar tokens en tiempo real.
- Manejar eventos de error.

El endpoint REST se mantiene funcional pero deprecado para permitir transición gradual.
