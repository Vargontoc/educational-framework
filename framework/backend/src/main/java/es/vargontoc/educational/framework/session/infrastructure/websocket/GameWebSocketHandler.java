package es.vargontoc.educational.framework.session.infrastructure.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.vargontoc.educational.framework.avatar.service.AvatarLifecycleService;
import es.vargontoc.educational.framework.session.model.ChildSessionStatus;
import es.vargontoc.educational.framework.session.ports.in.ChildSessionUseCase;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class GameWebSocketHandler extends TextWebSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameWebSocketHandler.class);
    private static final String ATTR_CHILD_SESSION_ID = "childSessionId";
    private static final int AUTH_TIMEOUT_SECONDS = 15;

    private final ChildSessionUseCase childSessionUseCase;
    private final ObjectMapper objectMapper;
    private final AvatarLifecycleService avatarLifecycleService;

    private final Map<Long, WebSocketSession> sessionsByChildSessionId = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> pendingAuthTimeouts = new ConcurrentHashMap<>();
    private final ScheduledExecutorService authTimeoutScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        var t = new Thread(r, "ws-auth-timeout");
        t.setDaemon(true);
        return t;
    });

    public GameWebSocketHandler(ChildSessionUseCase childSessionUseCase, ObjectMapper objectMapper,
                             AvatarLifecycleService avatarLifecycleService) {
        this.childSessionUseCase = childSessionUseCase;
        this.objectMapper = objectMapper;
        this.avatarLifecycleService = avatarLifecycleService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        LOGGER.debug("Game WebSocket connected: sessionId={}, awaiting auth", session.getId());
        var timeout = authTimeoutScheduler.schedule(() -> {
            if (session.isOpen() && getChildSessionId(session) == null) {
                LOGGER.warn("Auth timeout for WebSocket session {}", session.getId());
                closeSessionQuietly(session, CloseStatus.POLICY_VIOLATION);
            }
        }, AUTH_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        pendingAuthTimeouts.put(session.getId(), timeout);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            JsonNode root = objectMapper.readTree(message.getPayload());
            String type = root.has("type") ? root.get("type").asText() : "";

            switch (type) {
                case "auth" -> handleAuth(session, root);
                case "heartbeat" -> {
                    if (!isAuthenticated(session)) {
                        LOGGER.warn("Heartbeat received before auth from session {}", session.getId());
                        closeSessionQuietly(session, CloseStatus.POLICY_VIOLATION);
                        return;
                    }
                    handleHeartbeat(getChildSessionId(session));
                }
                case "game_action" -> {
                    if (!isAuthenticated(session)) {
                        LOGGER.warn("game_action received before auth from session {}", session.getId());
                        closeSessionQuietly(session, CloseStatus.POLICY_VIOLATION);
                        return;
                    }
                    handleGameAction(session, getChildSessionId(session), root);
                }
                default -> LOGGER.warn("Unknown game message type: {} from session={}", type, session.getId());
            }
        } catch (Exception exception) {
            LOGGER.error("Error processing game message from session={}: {}", session.getId(), exception.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        cancelPendingTimeout(session);
        Long childSessionId = getChildSessionId(session);
        if (childSessionId != null) {
            sessionsByChildSessionId.remove(childSessionId);
            LOGGER.info("Game WebSocket disconnected: childSessionId={}, status={}", childSessionId, status);
        } else {
            LOGGER.debug("Game WebSocket closed before auth: sessionId={}, status={}", session.getId(), status);
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

    public void sendFarewellAndClose(Long childSessionId) {
        WebSocketSession session = sessionsByChildSessionId.get(childSessionId);
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            var result = avatarLifecycleService.farewell(childSessionId);
            if (result.isPresent()) {
                sendToSession(childSessionId, objectMapper.writeValueAsString(result.event()));
                if (result.audioData() != null && result.event().audioId() != null) {
                    sendBinaryFrame(session, result.event().audioId(), result.audioData());
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Could not send farewell to childSessionId={}: {}", childSessionId, e.getMessage());
        } finally {
            closeSessionQuietly(session, CloseStatus.NORMAL);
        }
    }

    private void sendWelcomeAvatar(Long childSessionId) {
        try {
            var result = avatarLifecycleService.welcome(childSessionId);
            if (result.isPresent()) {
                sendToSession(childSessionId, objectMapper.writeValueAsString(result.event()));
                if (result.audioData() != null && result.event().audioId() != null) {
                    WebSocketSession session = sessionsByChildSessionId.get(childSessionId);
                    if (session != null && session.isOpen()) {
                        sendBinaryFrame(session, result.event().audioId(), result.audioData());
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Could not send welcome avatar to childSessionId={}: {}", childSessionId, e.getMessage());
        }
    }

    public boolean sendBinaryFrame(WebSocketSession session, String audioId, byte[] audioData) {
        try {
            byte[] audioIdBytes = audioId.getBytes(StandardCharsets.UTF_8);
            ByteBuffer buffer = ByteBuffer.allocate(4 + audioIdBytes.length + audioData.length);
            buffer.putInt(audioIdBytes.length);
            buffer.put(audioIdBytes);
            buffer.put(audioData);
            synchronized (session) {
                session.sendMessage(new BinaryMessage(buffer.array()));
            }
            return true;
        } catch (IOException e) {
            LOGGER.error("Failed to send binary frame: {}", e.getMessage());
            return false;
        }
    }

    @PreDestroy
    public void destroy() {
        authTimeoutScheduler.shutdownNow();
    }

    private void handleAuth(WebSocketSession session, JsonNode root) {
        if (!root.has("childSessionId") || root.get("childSessionId").isNull()) {
            LOGGER.warn("Auth message missing childSessionId from session {}", session.getId());
            closeSessionQuietly(session, CloseStatus.POLICY_VIOLATION);
            return;
        }
        long childSessionId = root.get("childSessionId").asLong();
        try {
            var childSession = childSessionUseCase.getSession(childSessionId);
            if (childSession.getStatus() != ChildSessionStatus.ACTIVE) {
                LOGGER.warn("Auth rejected: child session {} is not ACTIVE", childSessionId);
                closeSessionQuietly(session, CloseStatus.POLICY_VIOLATION);
                return;
            }
            session.getAttributes().put(ATTR_CHILD_SESSION_ID, childSessionId);
            sessionsByChildSessionId.put(childSessionId, session);
            cancelPendingTimeout(session);
            LOGGER.info("Game WebSocket authenticated: childSessionId={}", childSessionId);
            SessionEvent ack = SessionEvent.of(SessionEventType.AUTH_ACK, childSessionId);
            sendToSession(childSessionId, objectMapper.writeValueAsString(ack));
            sendWelcomeAvatar(childSessionId);
        } catch (ResourceNotFoundException e) {
            LOGGER.warn("Auth rejected: child session {} not found", childSessionId);
            closeSessionQuietly(session, CloseStatus.POLICY_VIOLATION);
        } catch (Exception e) {
            LOGGER.error("Auth error for session {}", session.getId(), e);
            closeSessionQuietly(session, CloseStatus.SERVER_ERROR);
        }
    }

    private void handleHeartbeat(Long childSessionId) {
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

    private boolean isAuthenticated(WebSocketSession session) {
        return getChildSessionId(session) != null;
    }

    private void cancelPendingTimeout(WebSocketSession session) {
        ScheduledFuture<?> future = pendingAuthTimeouts.remove(session.getId());
        if (future != null) {
            future.cancel(false);
        }
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
