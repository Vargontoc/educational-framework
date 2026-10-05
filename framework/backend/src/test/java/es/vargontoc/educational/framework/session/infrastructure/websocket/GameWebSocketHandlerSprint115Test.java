package es.vargontoc.educational.framework.session.infrastructure.websocket;

import tools.jackson.databind.ObjectMapper;
import es.vargontoc.educational.framework.avatar.infrastructure.service.AvatarService;
import es.vargontoc.educational.framework.content.ports.out.AccessibleColorPaletteRepository;
import es.vargontoc.educational.framework.content.ports.out.AccessibleColorRepository;
import es.vargontoc.educational.framework.content.ports.out.RecognitionElementRepository;
import es.vargontoc.educational.framework.family.ports.in.ChildProfileUseCase;
import es.vargontoc.educational.framework.game.ports.in.GameOrchestrator;
import es.vargontoc.educational.framework.game.ports.out.GameStateRegistry;
import es.vargontoc.educational.framework.session.model.ChildSession;
import es.vargontoc.educational.framework.session.model.ChildSessionStatus;
import es.vargontoc.educational.framework.session.ports.in.ChildSessionUseCase;
import es.vargontoc.educational.framework.shared.config.WebSocketGameProperties;
import es.vargontoc.educational.framework.shared.infrastructure.SqlStatementCounter;
import es.vargontoc.educational.framework.world.model.WorldDestinationSelectionResult;
import es.vargontoc.educational.framework.world.model.WorldRuntimeStatus;
import es.vargontoc.educational.framework.world.model.WorldState;
import es.vargontoc.educational.framework.world.ports.in.WorldGameStartUseCase;
import es.vargontoc.educational.framework.world.ports.in.WorldHeartbeatUseCase;
import es.vargontoc.educational.framework.world.ports.in.WorldOrchestrator;
import es.vargontoc.educational.framework.world.ports.out.WorldExplorationStateRepository;
import es.vargontoc.educational.framework.world.ports.out.WorldStateRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameWebSocketHandlerSprint115Test {

    @Mock
    private ChildSessionUseCase childSessionUseCase;

    @Mock
    private AvatarService avatarService;

    @Mock
    private GameOrchestrator gameOrchestrator;

    @Mock
    private GameStateRegistry gameStateRegistry;

    @Mock
    private WorldHeartbeatUseCase worldHeartbeatUseCase;

    @Mock
    private WorldGameStartUseCase worldGameStartUseCase;

    @Mock
    private WorldStateRegistry worldStateRegistry;

    @Mock
    private WorldOrchestrator worldOrchestrator;

    @Mock
    private RecognitionElementRepository recognitionElementRepository;

    @Mock
    private WorldExplorationStateRepository worldExplorationStateRepository;

    @Mock
    private ChildProfileUseCase childProfileUseCase;

    @Mock
    private AccessibleColorRepository accessibleColorRepository;

    @Mock
    private AccessibleColorPaletteRepository accessibleColorPaletteRepository;

    private GameWebSocketHandler handler;

    @BeforeEach
    void setUp() {
        WebSocketGameProperties wsProps = new WebSocketGameProperties();
        WebSocketMetrics wsMetrics = new WebSocketMetrics(new SimpleMeterRegistry());
        SynchronousSessionMessageDispatcher testDispatcher = new SynchronousSessionMessageDispatcher(wsProps.getMaxPendingMessagesPerSession(), wsMetrics);
        handler = new GameWebSocketHandler(childSessionUseCase, new ObjectMapper(), avatarService,
            gameOrchestrator, gameStateRegistry,
            worldHeartbeatUseCase, worldGameStartUseCase, worldStateRegistry, worldOrchestrator,
            recognitionElementRepository, worldExplorationStateRepository,
            childProfileUseCase, accessibleColorRepository, accessibleColorPaletteRepository,
            wsMetrics, SqlStatementCounter.NOOP, wsProps, testDispatcher,
            new es.vargontoc.educational.framework.world.service.ChildAgeResolver(),
            GameWebSocketHandlerTest.createNoopCacheStorage());
        lenient().when(worldOrchestrator.selectDestination(any(), any(), any(), any()))
            .thenReturn(new WorldDestinationSelectionResult());
    }

    @Test
    void reconnection_sameChild_closesPreviousSession() throws IOException {
        var childSession = childSession(50L, ChildSessionStatus.ACTIVE);
        when(childSessionUseCase.getSession(50L)).thenReturn(childSession);

        WebSocketSession firstSession = mockSession("first-session");
        when(firstSession.isOpen()).thenReturn(true);

        handler.afterConnectionEstablished(firstSession);
        handler.handleTextMessage(firstSession, new TextMessage("{\"type\":\"auth\",\"childSessionId\":50}"));
        assertTrue(handler.hasActiveSession(50L));

        WebSocketSession secondSession = mockSession("second-session");
        when(secondSession.isOpen()).thenReturn(true);

        handler.afterConnectionEstablished(secondSession);
        handler.handleTextMessage(secondSession, new TextMessage("{\"type\":\"auth\",\"childSessionId\":50}"));

        verify(firstSession).close(CloseStatus.NORMAL);
        assertTrue(handler.hasActiveSession(50L));
    }

    @Test
    void afterConnectionClosed_marksWorldAsClosed() throws IOException {
        var childSession = childSession(51L, ChildSessionStatus.ACTIVE);
        when(childSessionUseCase.getSession(51L)).thenReturn(childSession);

        WebSocketSession session = mockSession("session-51");
        when(session.isOpen()).thenReturn(true);

        WorldState worldState = new WorldState();
        worldState.setChildSessionId(51L);
        worldState.setStatus(WorldRuntimeStatus.ACTIVE);
        when(worldStateRegistry.findByChildSessionId(51L)).thenReturn(Optional.of(worldState));

        handler.afterConnectionEstablished(session);
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"auth\",\"childSessionId\":51}"));

        handler.afterConnectionClosed(session, CloseStatus.NORMAL);

        assertFalse(handler.hasActiveSession(51L));
        assertTrue(worldState.getStatus() == WorldRuntimeStatus.CLOSED);
    }

    @Test
    void textMessageOversized_closesWithPolicyViolation() throws IOException {
        WebSocketSession session = mockSession("session-oversized");

        handler.afterConnectionEstablished(session);

        StringBuilder largePayload = new StringBuilder("{\"type\":\"heartbeat\"}");
        while (largePayload.length() < 70000) {
            largePayload.append("x");
        }

        handler.handleTextMessage(session, new TextMessage(largePayload.toString()));

        verify(session).close(CloseStatus.POLICY_VIOLATION);
    }

    @Test
    void unknownMessageType_doesNotDumpContent() throws IOException {
        var childSession = childSession(52L, ChildSessionStatus.ACTIVE);
        when(childSessionUseCase.getSession(52L)).thenReturn(childSession);

        WebSocketSession session = mockSession("session-52");
        when(session.isOpen()).thenReturn(true);

        handler.afterConnectionEstablished(session);
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"auth\",\"childSessionId\":52}"));

        handler.handleTextMessage(session, new TextMessage("{\"type\":\"unknown_type\",\"secret\":\"data\"}"));

        assertTrue(handler.hasActiveSession(52L));
    }

    @Test
    void messageBeforeAuth_singleGuardClosesSession() throws IOException {
        WebSocketSession session = mockSession("session-unauth");

        handler.afterConnectionEstablished(session);
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"game_start\",\"activityId\":1}"));

        verify(session).close(CloseStatus.POLICY_VIOLATION);
    }

    private WebSocketSession mockSession(String sessionId) {
        try {
            WebSocketSession session = org.mockito.Mockito.mock(WebSocketSession.class);
            lenient().when(session.getId()).thenReturn(sessionId);
            lenient().when(session.getAttributes()).thenReturn(new HashMap<>());
            return session;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ChildSession childSession(Long id, ChildSessionStatus status) {
        var s = new ChildSession();
        s.setId(id);
        s.setStatus(status);
        s.setChildProfileId(100L);
        return s;
    }
}
