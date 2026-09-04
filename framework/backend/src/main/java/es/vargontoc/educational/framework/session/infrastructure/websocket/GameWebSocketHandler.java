package es.vargontoc.educational.framework.session.infrastructure.websocket;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import es.vargontoc.educational.framework.avatar.service.AvatarLifecycleService;
import es.vargontoc.educational.framework.game.exception.EngineNotAvailableException;
import es.vargontoc.educational.framework.game.exception.GameNotFoundException;
import es.vargontoc.educational.framework.game.exception.InvalidStateTransitionException;
import es.vargontoc.educational.framework.game.infrastructure.websocket.GameErrorCode;
import es.vargontoc.educational.framework.game.infrastructure.websocket.dto.GameActionRequest;
import es.vargontoc.educational.framework.game.infrastructure.websocket.dto.GameActionResponse;
import es.vargontoc.educational.framework.game.model.ActionProcessingResult;
import es.vargontoc.educational.framework.game.ports.in.GameOrchestrator;
import es.vargontoc.educational.framework.game.ports.out.GameStateRegistry;
import es.vargontoc.educational.framework.session.model.ChildSessionStatus;
import es.vargontoc.educational.framework.world.infrastructure.websocket.dto.WorldActivityStartedPayload;
import es.vargontoc.educational.framework.world.infrastructure.websocket.dto.WorldDestinationPayload;
import es.vargontoc.educational.framework.world.infrastructure.websocket.dto.WorldDiscoveryElementPayload;
import es.vargontoc.educational.framework.world.infrastructure.websocket.dto.WorldHostPayload;
import es.vargontoc.educational.framework.world.infrastructure.websocket.dto.WorldNarrativeSituationPayload;
import es.vargontoc.educational.framework.world.infrastructure.websocket.dto.WorldStateSyncPayload;
import es.vargontoc.educational.framework.world.model.WorldDestination;
import es.vargontoc.educational.framework.world.model.WorldDiscoveryProposal;
import es.vargontoc.educational.framework.world.model.WorldInactivityStatus;
import es.vargontoc.educational.framework.world.model.WorldRuntimeStatus;
import es.vargontoc.educational.framework.world.model.WorldState;
import es.vargontoc.educational.framework.world.ports.in.WorldGameStartUseCase;
import es.vargontoc.educational.framework.world.ports.in.WorldOrchestrator;
import es.vargontoc.educational.framework.world.ports.in.WorldHeartbeatUseCase;
import es.vargontoc.educational.framework.world.ports.out.WorldStateRegistry;
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
import java.util.List;
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
    private final GameOrchestrator gameOrchestrator;
    private final GameStateRegistry gameStateRegistry;
    private final WorldHeartbeatUseCase worldHeartbeatUseCase;
    private final WorldGameStartUseCase worldGameStartUseCase;
    private final WorldStateRegistry worldStateRegistry;
    private final WorldOrchestrator worldOrchestrator;

    private final Map<Long, WebSocketSession> sessionsByChildSessionId = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> pendingAuthTimeouts = new ConcurrentHashMap<>();
    private final ScheduledExecutorService authTimeoutScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        var t = new Thread(r, "ws-auth-timeout");
        t.setDaemon(true);
        return t;
    });

    public GameWebSocketHandler(ChildSessionUseCase childSessionUseCase, ObjectMapper objectMapper,
                             AvatarLifecycleService avatarLifecycleService,
                             GameOrchestrator gameOrchestrator,
                             GameStateRegistry gameStateRegistry,
                             WorldHeartbeatUseCase worldHeartbeatUseCase,
                             WorldGameStartUseCase worldGameStartUseCase,
                             WorldStateRegistry worldStateRegistry,
                             WorldOrchestrator worldOrchestrator) {
        this.childSessionUseCase = childSessionUseCase;
        this.objectMapper = objectMapper;
        this.avatarLifecycleService = avatarLifecycleService;
        this.gameOrchestrator = gameOrchestrator;
        this.gameStateRegistry = gameStateRegistry;
        this.worldHeartbeatUseCase = worldHeartbeatUseCase;
        this.worldGameStartUseCase = worldGameStartUseCase;
        this.worldStateRegistry = worldStateRegistry;
        this.worldOrchestrator = worldOrchestrator;
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
            String type = root.has("type") ? root.get("type").asString() : "";

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
                case "game_start" -> {
                    if (!isAuthenticated(session)) {
                        LOGGER.warn("game_start received before auth from session {}", session.getId());
                        closeSessionQuietly(session, CloseStatus.POLICY_VIOLATION);
                        return;
                    }
                    handleGameStart(session, getChildSessionId(session), root);
                }
                case "game_ready" -> {
                    if (!isAuthenticated(session)) {
                        LOGGER.warn("game_ready received before auth from session {}", session.getId());
                        closeSessionQuietly(session, CloseStatus.POLICY_VIOLATION);
                        return;
                    }
                    handleGameReady(session, getChildSessionId(session), root);
                }
                case "game_abandon" -> {
                    if (!isAuthenticated(session)) {
                        LOGGER.warn("game_abandon received before auth from session {}", session.getId());
                        closeSessionQuietly(session, CloseStatus.POLICY_VIOLATION);
                        return;
                    }
                    handleGameAbandon(session, getChildSessionId(session), root);
                }
                case "game_action" -> {
                    if (!isAuthenticated(session)) {
                        LOGGER.warn("game_action received before auth from session {}", session.getId());
                        closeSessionQuietly(session, CloseStatus.POLICY_VIOLATION);
                        return;
                    }
                    handleGameAction(session, getChildSessionId(session), root);
                }
                case "world_heartbeat" -> {
                    if (!isAuthenticated(session)) {
                        LOGGER.warn("world_heartbeat received before auth from session {}", session.getId());
                        closeSessionQuietly(session, CloseStatus.POLICY_VIOLATION);
                        return;
                    }
                    handleWorldHeartbeat(session, getChildSessionId(session));
                }
                case "world_discovery_interacted" -> {
                    if (!isAuthenticated(session)) {
                        LOGGER.warn("world_discovery_interacted received before auth from session {}", session.getId());
                        closeSessionQuietly(session, CloseStatus.POLICY_VIOLATION);
                        return;
                    }
                    handleWorldDiscoveryInteracted(session, getChildSessionId(session), root);
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



    private WorldState getNewWorld(Long childSessionId) {
        Long profileId = childSessionUseCase.getSession(childSessionId).getChildProfileId();
        WorldState ws = new WorldState();
        ws.setChildSessionId(childSessionId);
        ws.setChildProfileId(profileId);
        ws.setStatus(WorldRuntimeStatus.ACTIVE);
        var select = worldOrchestrator.selectDestination(childSessionId, profileId, null, 3);
        ws.setCurrentDestination(select.getDestination());
        return ws;
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
            worldStateRegistry.save(getNewWorld(childSessionId));

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

        GameActionRequest request;
        try {
            Long gameId = root.has("gameId") && !root.get("gameId").isNull() ? root.get("gameId").asLong() : null;
            String action = root.has("action") && !root.get("action").isNull() ? root.get("action").asString() : null;
            Long topicId = root.has("topicId") && !root.get("topicId").isNull() ? root.get("topicId").asLong() : null;
            Integer responseTimeMs = root.has("responseTimeMs") && !root.get("responseTimeMs").isNull()
                ? root.get("responseTimeMs").asInt() : null;

            if (gameId == null) {
                var gameState = gameStateRegistry.findByChildSessionId(childSessionId).orElse(null);
                if (gameState == null) {
                    sendGameError(childSessionId, GameErrorCode.GAME_NOT_FOUND, null);
                    return;
                }
                gameId = gameState.getGameId();
            }

            request = new GameActionRequest(gameId, action, topicId, responseTimeMs);
        } catch (Exception e) {
            LOGGER.warn("Failed to parse game action from childSessionId={}: {}", childSessionId, e.getMessage());
            sendGameError(childSessionId, GameErrorCode.PARSING_ERROR, null);
            return;
        }

        try {
            ActionProcessingResult result = gameOrchestrator.processAction(
                request.gameId(),
                request.action(),
                request.topicId(),
                request.responseTimeMs()
            );

            GameActionResponse response = GameActionResponse.fromProcessingResult(result);
            SessionEvent event = SessionEvent.of(SessionEventType.GAME_ACTION_RESULT, childSessionId, toPayload(response));
            sendToSession(childSessionId, objectMapper.writeValueAsString(event));

            if (result.gameCompleted()) {
                SessionEvent completedEvent = SessionEvent.of(SessionEventType.GAME_COMPLETED, childSessionId);
                sendToSession(childSessionId, objectMapper.writeValueAsString(completedEvent));
            }

        } catch (InvalidStateTransitionException e) {
            LOGGER.debug("Game action rejected - invalid state: childSessionId={}, gameId={}",
                childSessionId, request.gameId());
            sendGameError(childSessionId, GameErrorCode.GAME_NOT_IN_PROGRESS, request.gameId());

        } catch (EngineNotAvailableException e) {
            LOGGER.error("Engine not available for game action: childSessionId={}, gameId={}",
                childSessionId, request.gameId());
            sendGameError(childSessionId, GameErrorCode.ENGINE_ERROR, request.gameId());

        } catch (GameNotFoundException e) {
            LOGGER.debug("Game not found: childSessionId={}, gameId={}", childSessionId, request.gameId());
            sendGameError(childSessionId, GameErrorCode.GAME_NOT_FOUND, request.gameId());

        } catch (Exception e) {
            LOGGER.error("Unexpected error processing game action: childSessionId={}, gameId={}, error={}",
                childSessionId, request.gameId(), e.getMessage());
            sendGameError(childSessionId, GameErrorCode.ENGINE_ERROR, request.gameId());
        }
    }

    private void handleGameStart(WebSocketSession session, Long childSessionId, JsonNode root) {
        LOGGER.debug("Game start received from childSessionId={}: {}", childSessionId, root);

        try {
            Long activityId = root.has("activityId") && !root.get("activityId").isNull()
                ? root.get("activityId").asLong() : null;

            if (activityId == null) {
                sendGameError(childSessionId, GameErrorCode.PARSING_ERROR, null);
                return;
            }

            var existingGame = gameStateRegistry.findByChildSessionId(childSessionId).orElse(null);
            if (existingGame != null) {
                try {
                    gameOrchestrator.abandonGame(existingGame.getGameId());
                    LOGGER.debug("Abandoned existing game {} before starting new one", existingGame.getGameId());
                } catch (Exception e) {
                    LOGGER.warn("Failed to abandon existing game before start: {}", e.getMessage());
                }
            }

            var childSession = childSessionUseCase.getSession(childSessionId);
            Long childProfileId = childSession.getChildProfileId();

            var gameState = gameOrchestrator.startGame(childProfileId, activityId);

            SessionEvent event = SessionEvent.of(SessionEventType.GAME_STARTED, childSessionId, gameStateToPayload(gameState));
            sendToSession(childSessionId, objectMapper.writeValueAsString(event));

        } catch (InvalidStateTransitionException e) {
            LOGGER.debug("Game start rejected - invalid state: childSessionId={}", childSessionId);
            sendGameError(childSessionId, GameErrorCode.INVALID_STATE_TRANSITION, null);

        } catch (EngineNotAvailableException e) {
            LOGGER.error("Engine not available for game start: childSessionId={}", childSessionId);
            sendGameError(childSessionId, GameErrorCode.ENGINE_ERROR, null);

        } catch (Exception e) {
            LOGGER.error("Unexpected error starting game: childSessionId={}, error={}", childSessionId, e.getMessage());
            sendGameError(childSessionId, GameErrorCode.ENGINE_ERROR, null);
        }
    }

    private void handleGameReady(WebSocketSession session, Long childSessionId, JsonNode root) {
        LOGGER.debug("Game ready received from childSessionId={}: {}", childSessionId, root);

        try {
            var gameState = gameStateRegistry.findByChildSessionId(childSessionId).orElse(null);
            if (gameState == null) {
                sendGameError(childSessionId, GameErrorCode.NO_ACTIVE_GAME, null);
                return;
            }

            var updatedState = gameOrchestrator.readyGame(gameState.getGameId());

            SessionEvent event = SessionEvent.of(SessionEventType.GAME_READY, childSessionId, gameStateToPayload(updatedState));
            sendToSession(childSessionId, objectMapper.writeValueAsString(event));

        } catch (InvalidStateTransitionException e) {
            LOGGER.debug("Game ready rejected - invalid state: childSessionId={}", childSessionId);
            sendGameError(childSessionId, GameErrorCode.INVALID_STATE_TRANSITION, null);

        } catch (EngineNotAvailableException e) {
            LOGGER.error("Engine not available for game ready: childSessionId={}", childSessionId);
            sendGameError(childSessionId, GameErrorCode.ENGINE_ERROR, null);

        } catch (Exception e) {
            LOGGER.error("Unexpected error readying game: childSessionId={}, error={}", childSessionId, e.getMessage());
            sendGameError(childSessionId, GameErrorCode.ENGINE_ERROR, null);
        }
    }

    private void handleGameAbandon(WebSocketSession session, Long childSessionId, JsonNode root) {
        LOGGER.debug("Game abandon received from childSessionId={}: {}", childSessionId, root);

        try {
            var gameState = gameStateRegistry.findByChildSessionId(childSessionId).orElse(null);
            if (gameState == null) {
                sendGameError(childSessionId, GameErrorCode.NO_ACTIVE_GAME, null);
                return;
            }

            var abandonedState = gameOrchestrator.abandonGame(gameState.getGameId());

            SessionEvent event = SessionEvent.of(SessionEventType.GAME_ABANDONED, childSessionId, gameStateToPayload(abandonedState));
            sendToSession(childSessionId, objectMapper.writeValueAsString(event));

        } catch (InvalidStateTransitionException e) {
            LOGGER.debug("Game abandon rejected - invalid state: childSessionId={}", childSessionId);
            sendGameError(childSessionId, GameErrorCode.INVALID_STATE_TRANSITION, null);

        } catch (GameNotFoundException e) {
            LOGGER.debug("Game not found for abandon: childSessionId={}", childSessionId);
            sendGameError(childSessionId, GameErrorCode.GAME_NOT_FOUND, null);

        } catch (Exception e) {
            LOGGER.error("Unexpected error abandoning game: childSessionId={}, error={}", childSessionId, e.getMessage());
            sendGameError(childSessionId, GameErrorCode.ENGINE_ERROR, null);
        }
    }

    private void sendGameError(Long childSessionId, GameErrorCode code, Long gameId) {
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("code", code.name());
        if (gameId != null) {
            payload.put("gameId", gameId);
        }
        SessionEvent event = SessionEvent.of(SessionEventType.GAME_ERROR, childSessionId, payload);
        try {
            sendToSession(childSessionId, objectMapper.writeValueAsString(event));
        } catch (Exception e) {
            LOGGER.error("Failed to send game error to childSessionId={}: {}", childSessionId, e.getMessage());
        }
    }

    private Map<String, Object> toPayload(GameActionResponse response) {
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("resultType", response.resultType().name());
        payload.put("updatedState", gameStateToPayload(response.updatedState()));
        payload.put("difficultyChanged", response.difficultyChanged());
        if (response.newDifficultyLevelId() != null) {
            payload.put("newDifficultyLevelId", response.newDifficultyLevelId());
        }
        payload.put("gameCompleted", response.gameCompleted());
        if (response.unlockedAchievements() != null && !response.unlockedAchievements().isEmpty()) {
            payload.put("unlockedAchievements", response.unlockedAchievements());
        }
        if (response.attemptContext() != null) {
            payload.put("attemptContext", response.attemptContext());
        }
        return payload;
    }

    private Map<String, Object> gameStateToPayload(es.vargontoc.educational.framework.game.model.GameState state) {
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("gameId", state.getGameId());
        payload.put("status", state.getStatus().name());
        payload.put("activityId", state.getActivityId());
        payload.put("difficultyLevelId", state.getDifficultyLevelId());
        if (state.getCurrentScore() != null) {
            payload.put("currentScore", state.getCurrentScore());
        }
        if (state.getCurrentStreak() != null) {
            payload.put("currentStreak", state.getCurrentStreak());
        }
        if (state.getAttempts() != null) {
            payload.put("attempts", state.getAttempts());
        }
        if (state.getStartedAt() != null) {
            payload.put("startedAt", state.getStartedAt());
        }
        if (state.getCompletedAt() != null) {
            payload.put("completedAt", state.getCompletedAt());
        }
        return payload;
    }

    private void handleWorldHeartbeat(WebSocketSession session, Long childSessionId) {
        try {
            var heartbeatResult = worldHeartbeatUseCase.recordHeartbeat(childSessionId);
            String status = heartbeatResult.getStatus().name();
            WorldDestinationPayload destinationPayload = null;
            if (heartbeatResult.getStatus() == WorldInactivityStatus.ACTIVE) {
                var worldState = worldStateRegistry.findByChildSessionId(childSessionId).orElse(null);
                if (worldState != null && worldState.getCurrentDestination() != null) {
                    destinationPayload = toDestinationPayload(worldState.getCurrentDestination());
                }
            }
            WorldStateSyncPayload syncPayload = new WorldStateSyncPayload(status, destinationPayload);
            SessionEvent event = SessionEvent.of(SessionEventType.WORLD_STATE_SYNC, childSessionId, toPayload(syncPayload));
            sendToSession(childSessionId, objectMapper.writeValueAsString(event));
        } catch (Exception exception) {
            LOGGER.error("World heartbeat failed for childSessionId={}: {}", childSessionId, exception.getMessage());
        }
    }

    private void handleWorldDiscoveryInteracted(WebSocketSession session, Long childSessionId, JsonNode root) {
        try {
            String proposalRuntimeId = root.has("proposalRuntimeId") && !root.get("proposalRuntimeId").isNull()
                ? root.get("proposalRuntimeId").asString() : null;
            Long discoveryElementId = root.has("discoveryElementId") && !root.get("discoveryElementId").isNull()
                ? root.get("discoveryElementId").asLong() : null;

            if (proposalRuntimeId == null || discoveryElementId == null) {
                sendWorldError(childSessionId, "INVALID_REQUEST", null);
                return;
            }

            var worldState = worldStateRegistry.findByChildSessionId(childSessionId).orElse(null);
            if (worldState == null) {
                sendWorldError(childSessionId, "NO_WORLD_STATE", null);
                return;
            }

            var currentDestination = worldState.getCurrentDestination();
            if (currentDestination == null) {
                sendWorldError(childSessionId, "NO_DESTINATION", null);
                return;
            }

            Long activityId = findActivityIdByProposal(currentDestination, proposalRuntimeId, discoveryElementId);
            if (activityId == null) {
                sendWorldError(childSessionId, "ELEMENT_NOT_FOUND", null);
                return;
            }

            var startResult = worldGameStartUseCase.startGameFromProposal(childSessionId, activityId);

            if (startResult.getStatus() == es.vargontoc.educational.framework.world.model.WorldGameStartStatus.STARTED) {
                WorldActivityStartedPayload activityPayload = new WorldActivityStartedPayload(
                    startResult.getGameId(),
                    startResult.getActivityId(),
                    "START_GAME"
                );
                Map<String, Object> payload = new java.util.HashMap<>();
                payload.put("gameId", activityPayload.gameId());
                payload.put("activityId", activityPayload.activityId());
                payload.put("transition", activityPayload.transition());
                SessionEvent event = SessionEvent.of(SessionEventType.WORLD_ACTIVITY_STARTED, childSessionId, payload);
                sendToSession(childSessionId, objectMapper.writeValueAsString(event));
            } else {
                sendWorldError(childSessionId, "GAME_START_FAILED", null);
            }
        } catch (Exception exception) {
            LOGGER.error("World discovery interaction failed for childSessionId={}: {}", childSessionId, exception.getMessage());
            sendWorldError(childSessionId, "INTERNAL_ERROR", null);
        }
    }

    private Long findActivityIdByProposal(WorldDestination destination, String proposalRuntimeId, Long discoveryElementId) {
        if (destination.getDiscoveryProposals() == null) {
            return null;
        }
        for (WorldDiscoveryProposal proposal : destination.getDiscoveryProposals()) {
            if (proposal.getProposalRuntimeId() != null && proposal.getProposalRuntimeId().equals(proposalRuntimeId)) {
                return proposal.getActivityId();
            }
        }
        return null;
    }

    private WorldDestinationPayload toDestinationPayload(WorldDestination destination) {
        WorldHostPayload hostPayload = new WorldHostPayload(
            destination.getHostId(),
            destination.getHostCode(),
            destination.getHostDisplayName(),
            null
        );
        WorldNarrativeSituationPayload situationPayload = new WorldNarrativeSituationPayload(
            destination.getNarrativeSituationId(),
            destination.getNarrativeSituationCode(),
            destination.getDisplayText(),
            null
        );
        List<WorldDiscoveryElementPayload> elementPayloads = destination.getDiscoveryProposals() != null
            ? destination.getDiscoveryProposals().stream()
                .map(this::toDiscoveryElementPayload)
                .toList()
            : List.of();
        return new WorldDestinationPayload(
            destination.getDestinationId(),
            hostPayload,
            situationPayload,
            destination.getBiome(),
            elementPayloads
        );
    }

    private WorldDiscoveryElementPayload toDiscoveryElementPayload(WorldDiscoveryProposal proposal) {
        return new WorldDiscoveryElementPayload(
            proposal.getProposalRuntimeId(),
            proposal.getDiscoveryElementId(),
            proposal.getDiscoveryElementCode(),
            proposal.getDisplayName(),
            proposal.getElementType(),
            proposal.getVisualAssetKey(),
            proposal.getInteractionCueType(),
            proposal.getActivityId() != null
        );
    }

    private Map<String, Object> toPayload(WorldStateSyncPayload payload) {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("status", payload.status());
        if (payload.destination() != null) {
            result.put("destination", toPayload(payload.destination()));
        }
        return result;
    }

    private Map<String, Object> toPayload(WorldDestinationPayload destination) {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("destinationId", destination.destinationId());
        result.put("host", toPayload(destination.host()));
        result.put("narrativeSituation", toPayload(destination.narrativeSituation()));
        result.put("biome", destination.biome());
        result.put("discoveryElements", destination.discoveryElements().stream()
            .map(this::toPayload)
            .toList());
        return result;
    }

    private Map<String, Object> toPayload(WorldHostPayload host) {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", host.id());
        result.put("code", host.code());
        result.put("displayName", host.displayName());
        if (host.visualAssetKey() != null) {
            result.put("visualAssetKey", host.visualAssetKey());
        }
        return result;
    }

    private Map<String, Object> toPayload(WorldNarrativeSituationPayload situation) {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", situation.id());
        result.put("code", situation.code());
        if (situation.displayText() != null) {
            result.put("displayText", situation.displayText());
        }
        if (situation.tone() != null) {
            result.put("tone", situation.tone());
        }
        return result;
    }

    private Map<String, Object> toPayload(WorldDiscoveryElementPayload element) {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("proposalRuntimeId", element.proposalRuntimeId());
        result.put("discoveryElementId", element.discoveryElementId());
        result.put("code", element.code());
        result.put("displayName", element.displayName());
        result.put("elementType", element.elementType());
        if (element.visualAssetKey() != null) {
            result.put("visualAssetKey", element.visualAssetKey());
        }
        if (element.interactionCueType() != null) {
            result.put("interactionCueType", element.interactionCueType());
        }
        result.put("hasActivity", element.hasActivity());
        return result;
    }

    private void sendWorldError(Long childSessionId, String code, String message) {
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("code", code);
        if (message != null) {
            payload.put("message", message);
        }
        SessionEvent event = SessionEvent.of(SessionEventType.WORLD_STATE_SYNC, childSessionId, payload);
        try {
            sendToSession(childSessionId, objectMapper.writeValueAsString(event));
        } catch (Exception e) {
            LOGGER.error("Failed to send world error to childSessionId={}: {}", childSessionId, e.getMessage());
        }
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
