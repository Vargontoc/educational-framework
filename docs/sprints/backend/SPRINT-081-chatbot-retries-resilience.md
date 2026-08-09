# SPRINT-081 — Reintentos y resiliencia en streaming del chatbot

## Estado

- **Estado:** pending
- **Fecha de creación:** 2026-08-09
- **Responsable principal:** backend
- **Prioridad:** ALTA
- **Dependencias:** SPRINT-080 (streaming STOMP), ADR-003 (aceptada)
- **Impacto estimado:** Estrategia de 3 reintentos con backoff exponencial. Timeout de 60s por intento. Manejo robusto de errores. Notificación al usuario tras agotar reintentos.

## Objetivo

Implementar la estrategia de reintentos y resiliencia para el streaming del chatbot que:
- Reintente hasta 3 veces ante errores transitorios.
- Use backoff exponencial (1s, 2s, 4s) entre reintentos.
- Aplique timeout de 60s por intento.
- No detenga el modelo entre reintentos.
- Notifique al usuario con `CHATBOT_ERROR` tras agotar reintentos.
- Maneje desconexiones del cliente mid-stream.

## Contexto

**SPRINT-080** implementó la infraestructura básica de streaming sin reintentos. Este sprint añade resiliencia para manejar errores transitorios de Ollama y mejorar la experiencia de usuario.

**Decisión de producto (2026-08-09):**
- 3 reintentos con notificación al usuario si todos fallan.
- Timeout de 60s por intento.
- Thinking/reasoning: DESCARTADO para esta versión.

**Tipos de errores:**
- **Transitorios** (reintentar): timeout de Ollama, conexión rechazada, modelo no disponible temporalmente.
- **No transitorios** (no reintentar): `ValidationException`, mensajes vacíos, modelo UNREACHABLE (no existe en servidor).

## Diseño funcional-técnico

### 1. Estrategia de reintentos

**Flujo con reintentos:**

```
Intento 1 → error transitorio → wait 1s → Intento 2 → error transitorio → wait 2s → Intento 3 → error → CHATBOT_ERROR(attempt=3)
```

**Backoff exponencial:**
- Intento 1 falla → esperar 1s
- Intento 2 falla → esperar 2s
- Intento 3 falla → publicar `CHATBOT_ERROR` con `attempt: 3`

**Ciclo de vida del modelo:**
- Arrancar modelo antes del primer intento (si está STOPPED).
- NO detener entre reintentos.
- Detener solo al completar exitosamente o agotar reintentos.

### 2. Extensión de ChatbotStreamService

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/service/ChatbotStreamService.java` (actualizar)

**Cambio requerido:** Reemplazar lógica actual de streaming con bucle de reintentos.

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
    private static final int MAX_RETRIES = 3;
    private static final int STREAM_TIMEOUT_SECONDS = 60;
    private static final long[] BACKOFF_DELAYS = {1000L, 2000L, 4000L}; // 1s, 2s, 4s

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
            // Arrancar modelo si está STOPPED (solo una vez)
            if (checkStatus(AgentsConstants.CHATBOT_MODEL).status() == AgentStatusType.STOPPED) {
                ollamaPort.run(AgentsConstants.CHATBOT_MODEL, false, null);
            }

            // Guardar mensaje del usuario
            historyUseCase.addMessage(conversation.getId(), "USER", sanitized);

            // Bucle de reintentos
            boolean success = false;
            int attempt = 0;
            
            while (attempt < MAX_RETRIES && !success) {
                attempt++;
                LOG.info("Intento {} de {} para familyId={}", attempt, MAX_RETRIES, familyId);
                
                try {
                    success = executeStreamingWithTimeout(familyId, conversationId, conversation.getId(), sanitized);
                    
                    if (!success && attempt < MAX_RETRIES) {
                        // Backoff antes del siguiente intento
                        long delay = BACKOFF_DELAYS[attempt - 1];
                        LOG.warn("Intento {} falló. Reintentando en {}ms", attempt, delay);
                        Thread.sleep(delay);
                    }
                    
                } catch (Exception e) {
                    LOG.error("Error en intento {} para familyId={}: {}", attempt, familyId, e.getMessage());
                    
                    if (attempt < MAX_RETRIES) {
                        long delay = BACKOFF_DELAYS[attempt - 1];
                        LOG.warn("Intento {} falló con excepción. Reintentando en {}ms", attempt, delay);
                        Thread.sleep(delay);
                    }
                }
            }
            
            if (!success) {
                // Todos los reintentos fallaron
                LOG.error("Todos los reintentos fallaron para familyId={}", familyId);
                eventPublisher.publish(familyId, 
                    ChatbotStreamEvent.error(conversationId, "No se pudo generar respuesta después de varios intentos", MAX_RETRIES));
            }
            
        } catch (Exception e) {
            LOG.error("Error iniciando streaming para familyId={}: {}", familyId, e.getMessage());
            eventPublisher.publish(familyId, 
                ChatbotStreamEvent.error(conversationId, "Error al iniciar conversación", 1));
            
        } finally {
            // Detener modelo y limpiar stream activo
            ollamaPort.stop(AgentsConstants.CHATBOT_MODEL);
            activeStreams.remove(familyId);
        }
    }

    private boolean executeStreamingWithTimeout(Long familyId, UUID conversationId, Long conversationDbId, String sanitized) {
        try {
            StringBuilder fullResponse = new StringBuilder();
            
            Flux<org.springframework.ai.chat.prompt.ChatResponse> flux = 
                agents.get(AgentsConstants.CHATBOT_MODEL)
                    .prompt(sanitized)
                    .stream()
                    .chatResponse();

            // Usar blocking operations para controlar el flujo en el bucle de reintentos
            flux.timeout(Duration.ofSeconds(STREAM_TIMEOUT_SECONDS))
                .doOnNext(chatResponse -> {
                    String token = chatResponse.getResult().getOutput().getText();
                    if (token != null && !token.isEmpty()) {
                        fullResponse.append(token);
                        eventPublisher.publish(familyId, ChatbotStreamEvent.token(conversationId, token));
                    }
                })
                .doOnComplete(() -> {
                    // Guardar respuesta completa
                    historyUseCase.addMessage(conversationDbId, "ASSISTANT", fullResponse.toString());
                    
                    // Publicar COMPLETE
                    eventPublisher.publish(familyId, 
                        ChatbotStreamEvent.complete(conversationId, fullResponse.toString()));
                    
                    LOG.info("Streaming completado para familyId={}, conversationId={}", familyId, conversationId);
                })
                .doOnError(error -> {
                    LOG.error("Error en streaming para familyId={}: {}", familyId, error.getMessage());
                })
                .blockLast(); // Blocking para controlar el flujo en el bucle

            return true; // Éxito
            
        } catch (Exception e) {
            LOG.warn("Streaming falló para familyId={}: {}", familyId, e.getMessage());
            return false; // Fallo
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

**Nota sobre implementación:**
- Se usa `blockLast()` para convertir el `Flux` en operación blocking.
- Esto permite controlar el flujo en el bucle de reintentos de forma síncrona.
- Alternativa: usar `CompletableFuture` o programación reactiva completa, pero añade complejidad innecesaria para este caso.

### 3. Clasificación de errores

**Errores transitorios (reintentar):**
- `TimeoutException`: Ollama no respondió dentro del timeout.
- `ConnectException`: Conexión rechazada por Ollama.
- `IOException`: Errores de red temporales.
- Respuestas vacías o malformadas del modelo.

**Errores no transitorios (no reintentar):**
- `ValidationException`: Mensaje vacío o inválido (ya validado antes del bucle).
- Modelo `UNREACHABLE`: No existe en servidor Ollama.
- `InterruptedException`: Hilo interrumpido durante sleep.

**Implementación:**
- El bucle de reintentos captura todas las excepciones.
- Si el error es `InterruptedException`, se interrumpe el bucle inmediatamente.
- Para otros errores, se reintenta hasta `MAX_RETRIES`.

### 4. Manejo de desconexiones mid-stream

**Escenario:** Cliente se desconecta mientras se publican tokens.

**Comportamiento:**
- El stream continúa en el servidor (publica tokens aunque no haya suscriptores).
- `SimpMessagingTemplate.convertAndSend()` no falla si no hay suscriptores.
- Al completar, se publica `CHATBOT_COMPLETE` (aunque el cliente no lo reciba).
- El cliente puede recuperar la conversación completa vía API REST (`GET /api/v1/agents/conversations/{conversationId}`).

**Mejora futura (fuera de este sprint):**
- Detectar desconexión del cliente y cancelar el `Flux` para ahorrar recursos.
- Requiere integración con `SimpMessageHeaderAccessor` para verificar suscriptores activos.

## Contratos y dependencias externas

### Contratos

Sin cambios en contratos. Los eventos `CHATBOT_ERROR` ya incluyen campo `attempt` definido en SPRINT-080.

### Dependencias externas

| Capa | Dependencia | Estado |
|------|-------------|--------|
| Frontend | Manejar eventos `CHATBOT_ERROR` con campo `attempt` | ⏳ Pendiente (SPRINT-080) |
| Frontend | Mostrar mensaje de error tras agotar reintentos | ⏳ Pendiente (SPRINT-080) |
| Agents | Ninguna | ✅ Sin dependencia |
| TTS | Ninguna | ✅ Sin dependencia |

## Riesgos y mitigaciones

| # | Riesgo | Severidad | Mitigación |
|---|--------|-----------|------------|
| R1 | Múltiples reintentos saturan Ollama | MEDIA | Backoff exponencial (1s, 2s, 4s) evita sobrecarga. Máximo 3 reintentos. |
| R2 | Timeout de 60s muy largo para usuarios | MEDIA | Aceptable para aplicación monofamiliar (5-6 usuarios). Futuro: reducir a 30s si se detecta latencia excesiva. |
| R3 | Modelo se detiene inesperadamente durante streaming | MEDIA | Se trata como error transitorio → reintenta. Si agota reintentos, `CHATBOT_ERROR`. |
| R4 | Thread.sleep bloquea hilo del task scheduler | BAJA | Para 5-6 usuarios concurrentes, el impacto es mínimo. Futuro: usar programación reactiva completa si se escala. |
| R5 | Cliente no recibe tokens si se desconecta mid-stream | BAJA | Cliente puede recuperar conversación vía API REST. Mejora futura: detectar desconexión y cancelar stream. |

---

## Tareas del sprint

### Tarea 81.1: Extender ChatbotStreamService con bucle de reintentos

**Descripción:** Reemplazar lógica actual de streaming con bucle de hasta 3 reintentos.

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/service/ChatbotStreamService.java` (actualizar)

**Especificación completa:** Ver sección 2 del diseño funcional-técnico.

**Criterios de aceptación:**
- Bucle de hasta 3 reintentos.
- Backoff exponencial: 1s, 2s, 4s entre reintentos.
- Timeout de 60s por intento con `Flux.timeout()`.
- No detener modelo entre reintentos.
- Detener modelo solo al completar exitosamente o agotar reintentos.
- Publicar `CHATBOT_ERROR` con `attempt: 3` si todos fallan.
- Compilación sin errores.

---

### Tarea 81.2: Implementar método executeStreamingWithTimeout

**Descripción:** Método auxiliar que ejecuta streaming con timeout y retorna éxito/fallo.

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/service/ChatbotStreamService.java` (actualizar)

**Criterios de aceptación:**
- Usa `Flux.timeout(Duration.ofSeconds(60))`.
- Usa `blockLast()` para operación blocking.
- Publica `CHATBOT_TOKEN` por cada token.
- Publica `CHATBOT_COMPLETE` al completar.
- Retorna `true` si éxito, `false` si fallo.
- Captura excepciones sin propagarlas (el bucle las maneja).

---

### Tarea 81.3: Implementar manejo de errores y clasificación

**Descripción:** Distinguir entre errores transitorios y no transitorios.

**Archivo:** `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/service/ChatbotStreamService.java` (actualizar)

**Criterios de aceptación:**
- `InterruptedException` interrumpe el bucle inmediatamente.
- Otros errores se reintentan hasta `MAX_RETRIES`.
- Logs detallados de cada intento y motivo de fallo.
- Compilación sin errores.

---

### Tarea 81.4: Pruebas de integración de reintentos

**Descripción:** Tests de integración para validar estrategia de reintentos.

**Archivo:** `framework/backend/src/test/java/es/vargontoc/educational/framework/agents/ChatbotStreamRetryIntegrationTest.java` (nuevo)

**Criterios de aceptación:**
- Test: error transitorio en intento 1 → reintenta y completa en intento 2.
- Test: error transitorio en intentos 1 y 2 → reintenta y completa en intento 3.
- Test: error transitorio en los 3 intentos → publica `CHATBOT_ERROR` con `attempt: 3`.
- Test: timeout de 60s cancela stream y reintenta.
- Test: backoff exponencial respeta delays (1s, 2s, 4s).
- Test: modelo se detiene solo al finalizar (no entre reintentos).
- Tests pasan con Testcontainers.

---

### Tarea 81.5: Pruebas de estrés y concurrencia

**Descripción:** Tests para validar comportamiento bajo carga.

**Archivo:** `framework/backend/src/test/java/es/vargontoc/educational/framework/agents/ChatbotStreamStressTest.java` (nuevo)

**Criterios de aceptación:**
- Test: 5 familias concurrentes, cada una con streaming.
- Test: verificar aislamiento entre familias.
- Test: verificar que no hay deadlocks o race conditions.
- Test: verificar consumo de memoria aceptable (<100MB para 5 streams).
- Tests pasan con Testcontainers.

---

## Archivos afectados

| Archivo | Tipo de cambio |
|---------|---------------|
| `framework/backend/src/main/java/es/vargontoc/educational/framework/agents/service/ChatbotStreamService.java` | Actualizar |
| `framework/backend/src/test/java/es/vargontoc/educational/framework/agents/ChatbotStreamRetryIntegrationTest.java` | Nuevo |
| `framework/backend/src/test/java/es/vargontoc/educational/framework/agents/ChatbotStreamStressTest.java` | Nuevo |

## Estimación

- **Duración:** 2 días
- **Complejidad:** Media-Alta
- **Riesgo:** Medio (reintentos, timeouts, concurrencia)

## Criterios de aceptación del sprint

1. Ante un error transitorio, se reintenta hasta 3 veces con backoff exponencial (1s, 2s, 4s). *(Resiliencia)*
2. Cada intento tiene timeout de 60s. *(Resiliencia)*
3. El modelo no se detiene entre reintentos. *(Ciclo de vida)*
4. El modelo se detiene al completar exitosamente o agotar reintentos. *(Ciclo de vida)*
5. Si todos los reintentos fallan, se publica `CHATBOT_ERROR` con `attempt: 3`. *(Contrato)*
6. `InterruptedException` interrumpe el bucle inmediatamente. *(Robustez)*
7. Logs detallados de cada intento y motivo de fallo. *(Observabilidad)*
8. 5 familias concurrentes no causan deadlocks o race conditions. *(Concurrencia)*
9. Consumo de memoria aceptable (<100MB para 5 streams). *(Rendimiento)*
10. Compilación sin errores. *(Calidad)*
11. Tests de integración pasando. *(Calidad)*

## Dependencias bloqueantes

- [x] SPRINT-080 completado (streaming STOMP).
- [x] ADR-003 aceptada.
- [x] Decisiones de producto confirmadas.

## Handoffs a otras capas

### Frontend:
- Manejar eventos `CHATBOT_ERROR` con campo `attempt`.
- Mostrar mensaje de error amigable tras agotar reintentos.
- Opción de reintentar manualmente (futuro).

### Agents/TTS:
- Sin dependencia.

## Notas adicionales

### Privacidad infantil

- Reintentos no exponen datos adicionales.
- Mensajes ya sanitizados antes del bucle de reintentos.
- Aislamiento entre familias se mantiene.

### Observabilidad

**Logs recomendados:**
```
INFO  - Intento 1 de 3 para familyId=123
WARN  - Intento 1 falló. Reintentando en 1000ms
INFO  - Intento 2 de 3 para familyId=123
WARN  - Intento 2 falló. Reintentando en 2000ms
INFO  - Intento 3 de 3 para familyId=123
ERROR - Todos los reintentos fallaron para familyId=123
```

**Métricas futuras (fuera de este sprint):**
- Tasa de éxito en primer intento vs reintentos.
- Tiempo promedio de respuesta con reintentos.
- Distribución de errores por tipo.

### Mejoras futuras (fuera de este sprint)

1. **Detección de desconexión del cliente:** Cancelar stream si cliente se desconecta.
2. **Programación reactiva completa:** Reemplazar `blockLast()` con `CompletableFuture` o `Mono`.
3. **Circuit breaker:** Si Ollama falla repetidamente, dejar de intentar por un tiempo.
4. **Métricas Prometheus:** Exponer métricas de reintentos y latencia.
5. **Reintentos configurables:** Permitir ajustar `MAX_RETRIES` y `BACKOFF_DELAYS` vía configuración.

### Relación con SPRINT-080

Este sprint complementa SPRINT-080:
- SPRINT-080: streaming básico sin reintentos.
- SPRINT-081: añade resiliencia con reintentos.

Ambos sprints pueden validarse juntos en integración.
