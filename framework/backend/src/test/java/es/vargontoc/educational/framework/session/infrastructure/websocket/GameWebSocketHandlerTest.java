package es.vargontoc.educational.framework.session.infrastructure.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.vargontoc.educational.framework.avatar.infrastructure.dto.GameAvatarEvent;
import es.vargontoc.educational.framework.avatar.service.AvatarLifecycleService;
import es.vargontoc.educational.framework.game.exception.EngineNotAvailableException;
import es.vargontoc.educational.framework.game.exception.InvalidStateTransitionException;
import es.vargontoc.educational.framework.game.model.ActionProcessingResult;
import es.vargontoc.educational.framework.game.model.ActionResultType;
import es.vargontoc.educational.framework.game.model.GameState;
import es.vargontoc.educational.framework.game.model.GameStatus;
import es.vargontoc.educational.framework.game.ports.in.GameOrchestrator;
import es.vargontoc.educational.framework.game.ports.out.GameStateRegistry;
import es.vargontoc.educational.framework.session.model.ChildSession;
import es.vargontoc.educational.framework.session.model.ChildSessionStatus;
import es.vargontoc.educational.framework.session.ports.in.ChildSessionUseCase;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

@ExtendWith(MockitoExtension.class)
class GameWebSocketHandlerTest {

    @Mock
    private ChildSessionUseCase childSessionUseCase;

    @Mock
    private AvatarLifecycleService avatarLifecycleService;

    @Mock
    private GameOrchestrator gameOrchestrator;

    @Mock
    private GameStateRegistry gameStateRegistry;

    @Mock
    private WebSocketSession session;

    private GameWebSocketHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GameWebSocketHandler(childSessionUseCase, new ObjectMapper(), avatarLifecycleService,
            gameOrchestrator, gameStateRegistry);
        lenient().when(session.getId()).thenReturn("test-session-id");
        lenient().when(session.getAttributes()).thenReturn(new HashMap<>());
    }

    @Test
    void auth_validActiveSession_bindsSessionAndSendsAuthAck() throws IOException {
        var childSession = childSession(1L, ChildSessionStatus.ACTIVE);
        when(childSessionUseCase.getSession(1L)).thenReturn(childSession);
        when(session.isOpen()).thenReturn(true);

        handler.afterConnectionEstablished(session);
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"auth\",\"childSessionId\":1}"));

        assertEquals(1L, session.getAttributes().get("childSessionId"));
        assertTrue(handler.hasActiveSession(1L));
        verify(session).sendMessage(argThat(msg ->
            ((TextMessage) msg).getPayload().contains("AUTH_ACK")));
    }

    @Test
    void auth_sessionNotFound_closesWithPolicyViolation() throws IOException {
        when(childSessionUseCase.getSession(99L)).thenThrow(new ResourceNotFoundException("not found"));

        handler.afterConnectionEstablished(session);
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"auth\",\"childSessionId\":99}"));

        verify(session).close(CloseStatus.POLICY_VIOLATION);
        assertFalse(handler.hasActiveSession(99L));
    }

    @Test
    void auth_sessionNotActive_closesWithPolicyViolation() throws IOException {
        var childSession = childSession(2L, ChildSessionStatus.EXPIRED);
        when(childSessionUseCase.getSession(2L)).thenReturn(childSession);

        handler.afterConnectionEstablished(session);
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"auth\",\"childSessionId\":2}"));

        verify(session).close(CloseStatus.POLICY_VIOLATION);
        assertFalse(handler.hasActiveSession(2L));
    }

    @Test
    void auth_missingChildSessionId_closesWithPolicyViolation() throws IOException {
        handler.afterConnectionEstablished(session);
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"auth\"}"));

        verify(session).close(CloseStatus.POLICY_VIOLATION);
    }

    @Test
    void heartbeat_beforeAuth_closesWithPolicyViolation() throws IOException {
        handler.afterConnectionEstablished(session);
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"heartbeat\"}"));

        verify(session).close(CloseStatus.POLICY_VIOLATION);
        verify(childSessionUseCase, never()).recordHeartbeat(any());
    }

    @Test
    void heartbeat_afterAuth_callsRecordHeartbeatAndSendsAck() throws IOException {
        var childSession = childSession(3L, ChildSessionStatus.ACTIVE);
        when(childSessionUseCase.getSession(3L)).thenReturn(childSession);
        when(session.isOpen()).thenReturn(true);

        handler.afterConnectionEstablished(session);
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"auth\",\"childSessionId\":3}"));
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"heartbeat\"}"));

        verify(childSessionUseCase).recordHeartbeat(3L);
        var captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session, org.mockito.Mockito.atLeast(2)).sendMessage(captor.capture());
        assertTrue(captor.getAllValues().stream()
            .anyMatch(m -> m.getPayload().contains("HEARTBEAT_ACK")));
    }

    @Test
    void afterConnectionClosed_unauthSession_doesNotThrow() {
        handler.afterConnectionEstablished(session);
        handler.afterConnectionClosed(session, CloseStatus.NORMAL);

        assertFalse(handler.hasActiveSession(1L));
    }

    @Test
    void afterConnectionClosed_authSession_removesFromMap() throws IOException {
        var childSession = childSession(4L, ChildSessionStatus.ACTIVE);
        when(childSessionUseCase.getSession(4L)).thenReturn(childSession);
        when(session.isOpen()).thenReturn(true);

        handler.afterConnectionEstablished(session);
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"auth\",\"childSessionId\":4}"));
        assertTrue(handler.hasActiveSession(4L));

        handler.afterConnectionClosed(session, CloseStatus.NORMAL);

        assertFalse(handler.hasActiveSession(4L));
    }

    @Test
    void auth_validActiveSession_triggersWelcomeAvatar() throws IOException {
        var childSession = childSession(5L, ChildSessionStatus.ACTIVE);
        when(childSessionUseCase.getSession(5L)).thenReturn(childSession);
        when(session.isOpen()).thenReturn(true);
        var avatarResult = new AvatarLifecycleService.AvatarLifecycleResult(
            GameAvatarEvent.welcome(5L, true, "audio-id-123", "Hola, vamos a jugar!"),
            "mp3-data".getBytes()
        );
        when(avatarLifecycleService.welcome(5L)).thenReturn(avatarResult);

        handler.afterConnectionEstablished(session);
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"auth\",\"childSessionId\":5}"));

        verify(avatarLifecycleService).welcome(5L);
        var captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session, org.mockito.Mockito.atLeast(2)).sendMessage(captor.capture());
        assertTrue(captor.getAllValues().stream()
            .anyMatch(m -> m.getPayload().contains("GAME_AVATAR_EVENT")));
        assertTrue(captor.getAllValues().stream()
            .anyMatch(m -> m.getPayload().contains("SESSION_CONNECTED")));
    }

    @Test
    void sendFarewellAndClose_sendsFarewellAndCloses() throws IOException {
        var childSession = childSession(6L, ChildSessionStatus.ACTIVE);
        when(childSessionUseCase.getSession(6L)).thenReturn(childSession);
        when(session.isOpen()).thenReturn(true);

        handler.afterConnectionEstablished(session);
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"auth\",\"childSessionId\":6}"));
        assertTrue(handler.hasActiveSession(6L));

        var avatarResult = new AvatarLifecycleService.AvatarLifecycleResult(
            GameAvatarEvent.farewell(6L, true, "audio-id-456", "Vaya, parece que es hora de despedirnos. Hasta la proxima."),
            "mp3-data".getBytes()
        );
        when(avatarLifecycleService.farewell(6L)).thenReturn(avatarResult);

        handler.sendFarewellAndClose(6L);

        verify(avatarLifecycleService).farewell(6L);
        verify(session).close(CloseStatus.NORMAL);
    }

    @Test
    void afterConnectionClosed_abruptDisconnect_doesNotThrow() {
        handler.afterConnectionEstablished(session);
        handler.afterConnectionClosed(session, CloseStatus.SERVER_ERROR);

        assertFalse(handler.hasActiveSession(1L));
    }

    @Test
    void auth_validActiveSession_welcomeEventHasCorrectEventField() throws IOException {
        var childSession = childSession(7L, ChildSessionStatus.ACTIVE);
        when(childSessionUseCase.getSession(7L)).thenReturn(childSession);
        when(session.isOpen()).thenReturn(true);
        var avatarResult = new AvatarLifecycleService.AvatarLifecycleResult(
            GameAvatarEvent.welcome(7L, false, null, "Hola, vamos a jugar!"),
            null
        );
        when(avatarLifecycleService.welcome(7L)).thenReturn(avatarResult);

        handler.afterConnectionEstablished(session);
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"auth\",\"childSessionId\":7}"));

        var textCaptor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session, org.mockito.Mockito.atLeast(2)).sendMessage(textCaptor.capture());
        var avatarMessage = textCaptor.getAllValues().stream()
            .filter(m -> m.getPayload().contains("GAME_AVATAR_EVENT"))
            .findFirst()
            .orElseThrow();
        assertTrue(avatarMessage.getPayload().contains("\"event\":\"GAME_AVATAR_EVENT\""));
    }

    @Test
    void sendBinaryFrame_containsAudioIdCorrelation() throws Exception {
        byte[] audioData = "mp3-test-data".getBytes();
        String audioId = "test-audio-id-123";

        handler.sendBinaryFrame(session, audioId, audioData);

        var binaryCaptor = ArgumentCaptor.forClass(BinaryMessage.class);
        verify(session).sendMessage(binaryCaptor.capture());
        byte[] payload = binaryCaptor.getValue().getPayload().array();

        int audioIdLength = (payload[0] & 0xFF) << 24 | (payload[1] & 0xFF) << 16
            | (payload[2] & 0xFF) << 8 | (payload[3] & 0xFF);
        byte[] extractedAudioId = new byte[audioIdLength];
        System.arraycopy(payload, 4, extractedAudioId, 0, audioIdLength);
        assertEquals(audioId, new String(extractedAudioId));

        byte[] extractedAudio = new byte[payload.length - 4 - audioIdLength];
        System.arraycopy(payload, 4 + audioIdLength, extractedAudio, 0, extractedAudio.length);
        assertEquals("mp3-test-data", new String(extractedAudio));
    }

    @Test
    void gameAction_beforeAuth_closesWithPolicyViolation() throws IOException {
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"game_action\",\"action\":\"CORRECT:1\"}"));

        verify(session).close(CloseStatus.POLICY_VIOLATION);
        verify(gameOrchestrator, never()).processAction(any(), any(), any(), any());
    }

    @Test
    void gameAction_validAction_returnsResult() throws IOException {
        var childSession = childSession(10L, ChildSessionStatus.ACTIVE);
        when(childSessionUseCase.getSession(10L)).thenReturn(childSession);
        when(session.isOpen()).thenReturn(true);

        handler.afterConnectionEstablished(session);
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"auth\",\"childSessionId\":10}"));

        var gameState = createGameState(1L, 10L, 100L, 1L, GameStatus.IN_PROGRESS);
        when(gameOrchestrator.processAction(eq(1L), eq("CORRECT:1"), isNull(), eq(2000)))
            .thenReturn(new ActionProcessingResult(
                ActionResultType.CORRECT, 2000, gameState, false, null, false, "streak:1"
            ));

        handler.handleTextMessage(session, new TextMessage(
            "{\"type\":\"game_action\",\"gameId\":1,\"action\":\"CORRECT:1\",\"responseTimeMs\":2000}"));

        var captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session, org.mockito.Mockito.atLeast(1)).sendMessage(captor.capture());
        assertTrue(captor.getAllValues().stream()
            .anyMatch(m -> m.getPayload().contains("GAME_ACTION_RESULT")));
        assertTrue(captor.getAllValues().stream()
            .anyMatch(m -> m.getPayload().contains("CORRECT")));
    }

    @Test
    void gameAction_gameNotFound_returnsError() throws IOException {
        var childSession = childSession(11L, ChildSessionStatus.ACTIVE);
        when(childSessionUseCase.getSession(11L)).thenReturn(childSession);
        when(session.isOpen()).thenReturn(true);
        when(gameStateRegistry.findByChildSessionId(11L)).thenReturn(Optional.empty());

        handler.afterConnectionEstablished(session);
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"auth\",\"childSessionId\":11}"));

        handler.handleTextMessage(session, new TextMessage(
            "{\"type\":\"game_action\",\"action\":\"CORRECT:1\"}"));

        var captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session, org.mockito.Mockito.atLeast(1)).sendMessage(captor.capture());
        assertTrue(captor.getAllValues().stream()
            .anyMatch(m -> m.getPayload().contains("GAME_ERROR")));
        assertTrue(captor.getAllValues().stream()
            .anyMatch(m -> m.getPayload().contains("GAME_NOT_FOUND")));
    }

    @Test
    void gameAction_gameNotInProgress_returnsError() throws IOException {
        var childSession = childSession(12L, ChildSessionStatus.ACTIVE);
        when(childSessionUseCase.getSession(12L)).thenReturn(childSession);
        when(session.isOpen()).thenReturn(true);

        handler.afterConnectionEstablished(session);
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"auth\",\"childSessionId\":12}"));

        when(gameOrchestrator.processAction(eq(1L), eq("CORRECT:1"), isNull(), isNull()))
            .thenThrow(new InvalidStateTransitionException(GameStatus.WAITING, GameStatus.IN_PROGRESS));

        handler.handleTextMessage(session, new TextMessage(
            "{\"type\":\"game_action\",\"gameId\":1,\"action\":\"CORRECT:1\"}"));

        var captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session, org.mockito.Mockito.atLeast(1)).sendMessage(captor.capture());
        assertTrue(captor.getAllValues().stream()
            .anyMatch(m -> m.getPayload().contains("GAME_ERROR")));
        assertTrue(captor.getAllValues().stream()
            .anyMatch(m -> m.getPayload().contains("GAME_NOT_IN_PROGRESS")));
    }

    @Test
    void gameAction_engineError_returnsError() throws IOException {
        var childSession = childSession(13L, ChildSessionStatus.ACTIVE);
        when(childSessionUseCase.getSession(13L)).thenReturn(childSession);
        when(session.isOpen()).thenReturn(true);

        handler.afterConnectionEstablished(session);
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"auth\",\"childSessionId\":13}"));

        when(gameOrchestrator.processAction(eq(1L), any(), any(), any()))
            .thenThrow(new EngineNotAvailableException("fake"));

        handler.handleTextMessage(session, new TextMessage(
            "{\"type\":\"game_action\",\"gameId\":1,\"action\":\"CORRECT:1\"}"));

        var captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session, org.mockito.Mockito.atLeast(1)).sendMessage(captor.capture());
        assertTrue(captor.getAllValues().stream()
            .anyMatch(m -> m.getPayload().contains("GAME_ERROR")));
        assertTrue(captor.getAllValues().stream()
            .anyMatch(m -> m.getPayload().contains("ENGINE_ERROR")));
    }

    @Test
    void gameAction_gameCompleted_sendsGameCompleted() throws IOException {
        var childSession = childSession(14L, ChildSessionStatus.ACTIVE);
        when(childSessionUseCase.getSession(14L)).thenReturn(childSession);
        when(session.isOpen()).thenReturn(true);

        handler.afterConnectionEstablished(session);
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"auth\",\"childSessionId\":14}"));

        var gameState = createGameState(1L, 14L, 100L, 1L, GameStatus.COMPLETED);
        when(gameOrchestrator.processAction(eq(1L), eq("CORRECT:1"), isNull(), isNull()))
            .thenReturn(new ActionProcessingResult(
                ActionResultType.CORRECT, 2000, gameState, false, null, true, "completed"
            ));

        handler.handleTextMessage(session, new TextMessage(
            "{\"type\":\"game_action\",\"gameId\":1,\"action\":\"CORRECT:1\"}"));

        var captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session, org.mockito.Mockito.atLeast(1)).sendMessage(captor.capture());
        assertTrue(captor.getAllValues().stream()
            .anyMatch(m -> m.getPayload().contains("GAME_COMPLETED")));
    }

    @Test
    void gameStart_beforeAuth_closesWithPolicyViolation() throws IOException {
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"game_start\",\"activityId\":1}"));

        verify(session).close(CloseStatus.POLICY_VIOLATION);
        verify(gameOrchestrator, never()).startGame(any(), any());
    }

    @Test
    void gameStart_validActivityId_returnsGAME_STARTED() throws IOException {
        var childSession = childSession(10L, ChildSessionStatus.ACTIVE);
        when(childSessionUseCase.getSession(10L)).thenReturn(childSession);
        when(session.isOpen()).thenReturn(true);

        handler.afterConnectionEstablished(session);
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"auth\",\"childSessionId\":10}"));

        var gameState = createGameState(1L, 10L, 100L, 1L, GameStatus.WAITING);
        when(gameOrchestrator.startGame(100L, 1L)).thenReturn(gameState);

        handler.handleTextMessage(session, new TextMessage(
            "{\"type\":\"game_start\",\"activityId\":1}"));

        var captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session, org.mockito.Mockito.atLeast(1)).sendMessage(captor.capture());
        assertTrue(captor.getAllValues().stream()
            .anyMatch(m -> m.getPayload().contains("GAME_STARTED")));
        assertTrue(captor.getAllValues().stream()
            .anyMatch(m -> m.getPayload().contains("WAITING")));
    }

    @Test
    void gameStart_missingActivityId_returnsGAME_ERROR() throws IOException {
        var childSession = childSession(11L, ChildSessionStatus.ACTIVE);
        when(childSessionUseCase.getSession(11L)).thenReturn(childSession);
        when(session.isOpen()).thenReturn(true);

        handler.afterConnectionEstablished(session);
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"auth\",\"childSessionId\":11}"));

        handler.handleTextMessage(session, new TextMessage(
            "{\"type\":\"game_start\"}"));

        var captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session, org.mockito.Mockito.atLeast(1)).sendMessage(captor.capture());
        assertTrue(captor.getAllValues().stream()
            .anyMatch(m -> m.getPayload().contains("GAME_ERROR")));
        assertTrue(captor.getAllValues().stream()
            .anyMatch(m -> m.getPayload().contains("PARSING_ERROR")));
    }

    @Test
    void gameStart_withExistingGame_abandonsPrevious() throws IOException {
        var childSession = childSession(12L, ChildSessionStatus.ACTIVE);
        when(childSessionUseCase.getSession(12L)).thenReturn(childSession);
        when(session.isOpen()).thenReturn(true);

        handler.afterConnectionEstablished(session);
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"auth\",\"childSessionId\":12}"));

        var existingGame = createGameState(99L, 12L, 100L, 1L, GameStatus.IN_PROGRESS);
        when(gameStateRegistry.findByChildSessionId(12L)).thenReturn(Optional.of(existingGame));

        var newGameState = createGameState(100L, 12L, 100L, 2L, GameStatus.WAITING);
        when(gameOrchestrator.startGame(100L, 2L)).thenReturn(newGameState);

        handler.handleTextMessage(session, new TextMessage(
            "{\"type\":\"game_start\",\"activityId\":2}"));

        verify(gameOrchestrator).abandonGame(99L);
        verify(gameOrchestrator).startGame(100L, 2L);
    }

    @Test
    void gameReady_beforeAuth_closesWithPolicyViolation() throws IOException {
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"game_ready\"}"));

        verify(session).close(CloseStatus.POLICY_VIOLATION);
        verify(gameOrchestrator, never()).readyGame(any());
    }

    @Test
    void gameReady_noActiveGame_returnsGAME_ERROR() throws IOException {
        var childSession = childSession(13L, ChildSessionStatus.ACTIVE);
        when(childSessionUseCase.getSession(13L)).thenReturn(childSession);
        when(session.isOpen()).thenReturn(true);
        when(gameStateRegistry.findByChildSessionId(13L)).thenReturn(Optional.empty());

        handler.afterConnectionEstablished(session);
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"auth\",\"childSessionId\":13}"));

        handler.handleTextMessage(session, new TextMessage(
            "{\"type\":\"game_ready\"}"));

        var captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session, org.mockito.Mockito.atLeast(1)).sendMessage(captor.capture());
        assertTrue(captor.getAllValues().stream()
            .anyMatch(m -> m.getPayload().contains("GAME_ERROR")));
        assertTrue(captor.getAllValues().stream()
            .anyMatch(m -> m.getPayload().contains("NO_ACTIVE_GAME")));
    }

    @Test
    void gameReady_validGame_returnsGAME_READY() throws IOException {
        var childSession = childSession(14L, ChildSessionStatus.ACTIVE);
        when(childSessionUseCase.getSession(14L)).thenReturn(childSession);
        when(session.isOpen()).thenReturn(true);

        handler.afterConnectionEstablished(session);
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"auth\",\"childSessionId\":14}"));

        var existingGame = createGameState(1L, 14L, 100L, 5L, GameStatus.WAITING);
        when(gameStateRegistry.findByChildSessionId(14L)).thenReturn(Optional.of(existingGame));

        var readyGame = createGameState(1L, 14L, 100L, 5L, GameStatus.IN_PROGRESS);
        when(gameOrchestrator.readyGame(1L)).thenReturn(readyGame);

        handler.handleTextMessage(session, new TextMessage(
            "{\"type\":\"game_ready\"}"));

        var captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session, org.mockito.Mockito.atLeast(1)).sendMessage(captor.capture());
        assertTrue(captor.getAllValues().stream()
            .anyMatch(m -> m.getPayload().contains("GAME_READY")));
        assertTrue(captor.getAllValues().stream()
            .anyMatch(m -> m.getPayload().contains("IN_PROGRESS")));
    }

    @Test
    void gameAbandon_beforeAuth_closesWithPolicyViolation() throws IOException {
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"game_abandon\"}"));

        verify(session).close(CloseStatus.POLICY_VIOLATION);
        verify(gameOrchestrator, never()).abandonGame(any());
    }

    @Test
    void gameAbandon_noActiveGame_returnsGAME_ERROR() throws IOException {
        var childSession = childSession(15L, ChildSessionStatus.ACTIVE);
        when(childSessionUseCase.getSession(15L)).thenReturn(childSession);
        when(session.isOpen()).thenReturn(true);
        when(gameStateRegistry.findByChildSessionId(15L)).thenReturn(Optional.empty());

        handler.afterConnectionEstablished(session);
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"auth\",\"childSessionId\":15}"));

        handler.handleTextMessage(session, new TextMessage(
            "{\"type\":\"game_abandon\"}"));

        var captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session, org.mockito.Mockito.atLeast(1)).sendMessage(captor.capture());
        assertTrue(captor.getAllValues().stream()
            .anyMatch(m -> m.getPayload().contains("GAME_ERROR")));
        assertTrue(captor.getAllValues().stream()
            .anyMatch(m -> m.getPayload().contains("NO_ACTIVE_GAME")));
    }

    @Test
    void gameAbandon_validGame_returnsGAME_ABANDONED() throws IOException {
        var childSession = childSession(16L, ChildSessionStatus.ACTIVE);
        when(childSessionUseCase.getSession(16L)).thenReturn(childSession);
        when(session.isOpen()).thenReturn(true);

        handler.afterConnectionEstablished(session);
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"auth\",\"childSessionId\":16}"));

        var existingGame = createGameState(1L, 16L, 100L, 5L, GameStatus.IN_PROGRESS);
        when(gameStateRegistry.findByChildSessionId(16L)).thenReturn(Optional.of(existingGame));

        var abandonedGame = createGameState(1L, 16L, 100L, 5L, GameStatus.ABANDONED);
        when(gameOrchestrator.abandonGame(1L)).thenReturn(abandonedGame);

        handler.handleTextMessage(session, new TextMessage(
            "{\"type\":\"game_abandon\"}"));

        var captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session, org.mockito.Mockito.atLeast(1)).sendMessage(captor.capture());
        assertTrue(captor.getAllValues().stream()
            .anyMatch(m -> m.getPayload().contains("GAME_ABANDONED")));
        assertTrue(captor.getAllValues().stream()
            .anyMatch(m -> m.getPayload().contains("ABANDONED")));
    }

    private ChildSession childSession(Long id, ChildSessionStatus status) {
        var s = new ChildSession();
        s.setId(id);
        s.setStatus(status);
        s.setChildProfileId(100L);
        return s;
    }

    private GameState createGameState(Long gameId, Long childSessionId, Long activityId, Long difficultyLevelId, GameStatus status) {
        var state = new GameState();
        state.setGameId(gameId);
        state.setChildSessionId(childSessionId);
        state.setActivityId(activityId);
        state.setDifficultyLevelId(difficultyLevelId);
        state.setStatus(status);
        state.setSequenceNumber(0);
        state.setAttempts(0);
        state.setCorrectAttempts(0);
        state.setIncorrectAttempts(0);
        state.setTimeoutAttempts(0);
        state.setCurrentScore(BigDecimal.ZERO);
        state.setCurrentStreak(0);
        state.setStarsEarned(0);
        return state;
    }
}
