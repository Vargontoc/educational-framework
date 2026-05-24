package es.vargontoc.educational.framework.session.infrastructure.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.vargontoc.educational.framework.session.ports.in.ChildSessionUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameWebSocketHandler extends TextWebSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameWebSocketHandler.class);
    private static final String ATTR_CHILD_SESSION_ID = "childSessionId";

    private final ChildSessionUseCase childSessionUseCase;
    private final ObjectMapper objectMapper;

    private final Map<Long, WebSocketSession> sessionsByChildSessionId = new ConcurrentHashMap<>();

    public GameWebSocketHandler(ChildSessionUseCase childSessionUseCase, ObjectMapper objectMapper) {
        this.childSessionUseCase = childSessionUseCase;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long childSessionId = getChildSessionId(session);
        if (childSessionId == null) {
            LOGGER.warn("Game WebSocket connected without childSessionId, closing");
            closeSessionQuietly(session, CloseStatus.POLICY_VIOLATION);
            return;
        }
        sessionsByChildSessionId.put(childSessionId, session);
        LOGGER.info("Game WebSocket connected: childSessionId={}", childSessionId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        Long childSessionId = getChildSessionId(session);
        if (childSessionId == null) {
            return;
        }

        try {
            JsonNode root = objectMapper.readTree(message.getPayload());
            String type = root.has("type") ? root.get("type").asText() : "";

            switch (type) {
                case "heartbeat" -> handleHeartbeat(session, childSessionId);
                case "game_action" -> handleGameAction(session, childSessionId, root);
                default -> LOGGER.warn("Unknown game message type: {} from childSessionId={}", type, childSessionId);
            }
        } catch (Exception exception) {
            LOGGER.error("Error processing game message from childSessionId={}: {}",
                childSessionId, exception.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long childSessionId = getChildSessionId(session);
        if (childSessionId != null) {
            sessionsByChildSessionId.remove(childSessionId);
            LOGGER.info("Game WebSocket disconnected: childSessionId={}, status={}", childSessionId, status);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        Long childSessionId = getChildSessionId(session);
        LOGGER.error("Game WebSocket transport error: childSessionId={}", childSessionId, exception);
        closeSessionQuietly(session, CloseStatus.SERVER_ERROR);
    }

    public boolean sendToSession(Long childSessionId, String payload) {
        WebSocketSession session = sessionsByChildSessionId.get(childSessionId);
        if (session == null || !session.isOpen()) {
            LOGGER.debug("No active game WebSocket for childSessionId={}", childSessionId);
            return false;
        }
        try {
            synchronized (session) {
                session.sendMessage(new TextMessage(payload));
            }
            return true;
        } catch (IOException exception) {
            LOGGER.error("Failed to send message to childSessionId={}: {}", childSessionId, exception.getMessage());
            return false;
        }
    }

    public boolean hasActiveSession(Long childSessionId) {
        WebSocketSession session = sessionsByChildSessionId.get(childSessionId);
        return session != null && session.isOpen();
    }

    private void handleHeartbeat(WebSocketSession session, Long childSessionId) {
        try {
            childSessionUseCase.recordHeartbeat(childSessionId);
            SessionEvent ack = SessionEvent.of(SessionEventType.HEARTBEAT_ACK, childSessionId);
            String json = objectMapper.writeValueAsString(ack);
            sendToSession(childSessionId, json);
        } catch (Exception exception) {
            LOGGER.error("Heartbeat failed for childSessionId={}: {}", childSessionId, exception.getMessage());
        }
    }

    private void handleGameAction(WebSocketSession session, Long childSessionId, JsonNode root) {
        LOGGER.debug("Game action received from childSessionId={}: {}", childSessionId, root);
        // Placeholder: game module will process actions via GameOrchestrator
    }

    private Long getChildSessionId(WebSocketSession session) {
        Object attr = session.getAttributes().get(ATTR_CHILD_SESSION_ID);
        return attr instanceof Long id ? id : null;
    }

    private static void closeSessionQuietly(WebSocketSession session, CloseStatus status) {
        try {
            session.close(status);
        } catch (IOException ignored) {
            // Best-effort close
        }
    }
}
