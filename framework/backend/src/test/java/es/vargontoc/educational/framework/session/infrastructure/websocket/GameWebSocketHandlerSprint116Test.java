package es.vargontoc.educational.framework.session.infrastructure.websocket;

import tools.jackson.databind.ObjectMapper;
import es.vargontoc.educational.framework.avatar.infrastructure.service.AvatarService;
import es.vargontoc.educational.framework.content.ports.out.AccessibleColorPaletteRepository;
import es.vargontoc.educational.framework.content.ports.out.AccessibleColorRepository;
import es.vargontoc.educational.framework.content.ports.out.RecognitionElementRepository;
import es.vargontoc.educational.framework.family.ports.in.ChildProfileUseCase;
import es.vargontoc.educational.framework.game.model.ActionProcessingResult;
import es.vargontoc.educational.framework.game.model.ActionResultType;
import es.vargontoc.educational.framework.game.model.GameState;
import es.vargontoc.educational.framework.game.model.GameStatus;
import es.vargontoc.educational.framework.game.model.enums.EngineType;
import es.vargontoc.educational.framework.game.ports.in.GameOrchestrator;
import es.vargontoc.educational.framework.game.ports.out.GameStateRegistry;
import es.vargontoc.educational.framework.game.service.RoundAudioResult;
import es.vargontoc.educational.framework.session.model.ChildSession;
import es.vargontoc.educational.framework.session.model.ChildSessionStatus;
import es.vargontoc.educational.framework.session.ports.in.ChildSessionUseCase;
import es.vargontoc.educational.framework.shared.config.WebSocketGameProperties;
import es.vargontoc.educational.framework.shared.infrastructure.SqlStatementCounter;
import es.vargontoc.educational.framework.world.model.WorldDestinationSelectionResult;
import es.vargontoc.educational.framework.world.ports.in.WorldGameStartUseCase;
import es.vargontoc.educational.framework.world.ports.in.WorldHeartbeatUseCase;
import es.vargontoc.educational.framework.world.ports.in.WorldOrchestrator;
import es.vargontoc.educational.framework.world.ports.out.WorldExplorationStateRepository;
import es.vargontoc.educational.framework.world.ports.out.WorldStateRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameWebSocketHandlerSprint116Test {

    @Mock private ChildSessionUseCase childSessionUseCase;
    @Mock private AvatarService avatarService;
    @Mock private GameOrchestrator gameOrchestrator;
    @Mock private GameStateRegistry gameStateRegistry;
    @Mock private WorldHeartbeatUseCase worldHeartbeatUseCase;
    @Mock private WorldGameStartUseCase worldGameStartUseCase;
    @Mock private WorldStateRegistry worldStateRegistry;
    @Mock private WorldOrchestrator worldOrchestrator;
    @Mock private RecognitionElementRepository recognitionElementRepository;
    @Mock private WorldExplorationStateRepository worldExplorationStateRepository;
    @Mock private ChildProfileUseCase childProfileUseCase;
    @Mock private AccessibleColorRepository accessibleColorRepository;
    @Mock private AccessibleColorPaletteRepository accessibleColorPaletteRepository;

    private GameWebSocketHandler handler;
    private SessionMessageDispatcher dispatcher;
    private WebSocketMetrics wsMetrics;

    @BeforeEach
    void setUp() {
        WebSocketGameProperties wsProps = new WebSocketGameProperties();
        wsMetrics = new WebSocketMetrics(new SimpleMeterRegistry());
        dispatcher = new SessionMessageDispatcher(wsProps.getMaxPendingMessagesPerSession(), wsMetrics);
        handler = new GameWebSocketHandler(childSessionUseCase, new ObjectMapper(), avatarService,
            gameOrchestrator, gameStateRegistry,
            worldHeartbeatUseCase, worldGameStartUseCase, worldStateRegistry, worldOrchestrator,
            recognitionElementRepository, worldExplorationStateRepository,
            childProfileUseCase, accessibleColorRepository, accessibleColorPaletteRepository,
            wsMetrics, SqlStatementCounter.NOOP, wsProps, dispatcher,
            new es.vargontoc.educational.framework.world.service.ChildAgeResolver(),
            GameWebSocketHandlerTest.createNoopCacheStorage());
        lenient().when(worldOrchestrator.selectDestination(any(), any(), any(), any()))
            .thenReturn(new WorldDestinationSelectionResult());
    }

    @AfterEach
    void tearDown() {
        dispatcher.shutdown();
    }

    @Test
    void order_hundredGameActionsProcessedInOrder() throws Exception {
        WebSocketGameProperties wsProps = new WebSocketGameProperties();
        wsProps.setMaxPendingMessagesPerSession(500);
        SessionMessageDispatcher orderDispatcher = new SessionMessageDispatcher(500, wsMetrics);
        GameWebSocketHandler orderHandler = new GameWebSocketHandler(childSessionUseCase, new ObjectMapper(), avatarService,
            gameOrchestrator, gameStateRegistry,
            worldHeartbeatUseCase, worldGameStartUseCase, worldStateRegistry, worldOrchestrator,
            recognitionElementRepository, worldExplorationStateRepository,
            childProfileUseCase, accessibleColorRepository, accessibleColorPaletteRepository,
            wsMetrics, SqlStatementCounter.NOOP, wsProps, orderDispatcher,
            new es.vargontoc.educational.framework.world.service.ChildAgeResolver(),
            GameWebSocketHandlerTest.createNoopCacheStorage());

        Long childSessionId = 100L;
        WebSocketSession session = mockAuthenticatedSessionForHandler(orderHandler, childSessionId);

        GameState gameState = buildGameState(1L, childSessionId);
        lenient().when(gameStateRegistry.findByChildSessionId(childSessionId)).thenReturn(Optional.of(gameState));

        List<Integer> processingOrder = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch latch = new CountDownLatch(100);
        AtomicInteger counter = new AtomicInteger(0);

        lenient().when(gameOrchestrator.processAction(eq(1L), any(), any(), any())).thenAnswer(inv -> {
            int index = counter.getAndIncrement();
            processingOrder.add(index);
            latch.countDown();
            return buildActionResult(ActionResultType.CORRECT, false, gameState);
        });

        for (int i = 0; i < 100; i++) {
            orderHandler.handleTextMessage(session, new TextMessage("{\"type\":\"game_action\",\"gameId\":1,\"action\":\"tap\",\"topicId\":1}"));
            if (i % 10 == 0) Thread.sleep(10);
        }

        assertTrue(latch.await(30, TimeUnit.SECONDS), "Not all actions processed within timeout, only " + (100 - latch.getCount()) + " processed");
        orderDispatcher.awaitQuiescence(5000);

        assertEquals(100, processingOrder.size());
        for (int i = 0; i < 100; i++) {
            assertEquals(i, processingOrder.get(i), "Action " + i + " was not processed in order");
        }
        orderDispatcher.shutdown();
    }

    @Test
    void isolation_slowMessageFromOneSessionDoesNotDelayAnother() throws Exception {
        Long slowSession = 200L;
        Long fastSession = 201L;
        WebSocketSession slowWsSession = mockAuthenticatedSession(slowSession);
        WebSocketSession fastWsSession = mockAuthenticatedSession(fastSession);

        GameState slowGameState = buildGameState(2L, slowSession);
        GameState fastGameState = buildGameState(3L, fastSession);
        lenient().when(gameStateRegistry.findByChildSessionId(slowSession)).thenReturn(Optional.of(slowGameState));
        lenient().when(gameStateRegistry.findByChildSessionId(fastSession)).thenReturn(Optional.of(fastGameState));

        CountDownLatch fastCompleted = new CountDownLatch(1);
        CountDownLatch slowStarted = new CountDownLatch(1);

        lenient().when(gameOrchestrator.processAction(eq(2L), any(), any(), any())).thenAnswer(inv -> {
            slowStarted.countDown();
            Thread.sleep(2000);
            return buildActionResult(ActionResultType.CORRECT, false, slowGameState);
        });

        lenient().when(gameOrchestrator.processAction(eq(3L), any(), any(), any())).thenAnswer(inv -> {
            fastCompleted.countDown();
            return buildActionResult(ActionResultType.CORRECT, false, fastGameState);
        });

        handler.handleTextMessage(slowWsSession, new TextMessage("{\"type\":\"game_action\",\"gameId\":2,\"action\":\"tap\"}"));
        assertTrue(slowStarted.await(5, TimeUnit.SECONDS), "Slow action didn't start");

        long fastStart = System.currentTimeMillis();
        handler.handleTextMessage(fastWsSession, new TextMessage("{\"type\":\"game_action\",\"gameId\":3,\"action\":\"tap\"}"));
        assertTrue(fastCompleted.await(5, TimeUnit.SECONDS), "Fast action was delayed by slow session");
        long fastDuration = System.currentTimeMillis() - fastStart;

        assertTrue(fastDuration < 1000, "Fast session took " + fastDuration + "ms, should be < 1000ms");
    }

    @Test
    void failureInOneMessageDoesNotBlockFollowing() throws Exception {
        Long childSessionId = 300L;
        WebSocketSession session = mockAuthenticatedSession(childSessionId);

        GameState gameState = buildGameState(4L, childSessionId);
        lenient().when(gameStateRegistry.findByChildSessionId(childSessionId)).thenReturn(Optional.of(gameState));

        AtomicInteger callCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(3);

        lenient().when(gameOrchestrator.processAction(eq(4L), any(), any(), any())).thenAnswer(inv -> {
            int count = callCount.incrementAndGet();
            latch.countDown();
            if (count == 2) {
                throw new RuntimeException("Simulated failure");
            }
            return buildActionResult(ActionResultType.CORRECT, false, gameState);
        });

        for (int i = 0; i < 3; i++) {
            handler.handleTextMessage(session, new TextMessage("{\"type\":\"game_action\",\"gameId\":4,\"action\":\"tap\"}"));
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS), "Not all actions processed after failure");
        assertEquals(3, callCount.get());
    }

    @Test
    void sessionCloseDiscardsPendingAndFreesQueue() throws Exception {
        Long childSessionId = 400L;
        WebSocketSession session = mockAuthenticatedSession(childSessionId);

        GameState gameState = buildGameState(5L, childSessionId);
        lenient().when(gameStateRegistry.findByChildSessionId(childSessionId)).thenReturn(Optional.of(gameState));

        CountDownLatch firstStarted = new CountDownLatch(1);
        CountDownLatch releaseBlock = new CountDownLatch(1);

        lenient().when(gameOrchestrator.processAction(eq(5L), any(), any(), any())).thenAnswer(inv -> {
            firstStarted.countDown();
            releaseBlock.await(10, TimeUnit.SECONDS);
            return buildActionResult(ActionResultType.CORRECT, false, gameState);
        });

        handler.handleTextMessage(session, new TextMessage("{\"type\":\"game_action\",\"gameId\":5,\"action\":\"tap\"}"));
        assertTrue(firstStarted.await(5, TimeUnit.SECONDS));

        for (int i = 0; i < 5; i++) {
            handler.handleTextMessage(session, new TextMessage("{\"type\":\"game_action\",\"gameId\":5,\"action\":\"tap\"}"));
        }

        Thread.sleep(100);
        int pendingBefore = dispatcher.totalPending();
        assertTrue(pendingBefore > 0, "Should have pending messages, had " + pendingBefore);

        handler.afterConnectionClosed(session, CloseStatus.NORMAL);
        releaseBlock.countDown();
        Thread.sleep(500);

        assertEquals(0, dispatcher.totalPending(), "Pending should be 0 after close");
    }

    @Test
    void pendingLimitExceeded_closesSession() throws Exception {
        Long childSessionId = 500L;
        WebSocketSession session = mock(WebSocketSession.class);
        Map<String, Object> attrs = new HashMap<>();
        lenient().when(session.getId()).thenReturn("ws-" + childSessionId);
        lenient().when(session.getAttributes()).thenReturn(attrs);
        lenient().when(session.isOpen()).thenReturn(true);

        var childSession = new ChildSession();
        childSession.setId(childSessionId);
        childSession.setStatus(ChildSessionStatus.ACTIVE);
        childSession.setChildProfileId(1L);
        lenient().when(childSessionUseCase.getSession(childSessionId)).thenReturn(childSession);

        WebSocketGameProperties wsProps = new WebSocketGameProperties();
        wsProps.setMaxPendingMessagesPerSession(2);
        SessionMessageDispatcher limitedDispatcher = new SessionMessageDispatcher(2, wsMetrics);

        GameWebSocketHandler limitedHandler = new GameWebSocketHandler(childSessionUseCase, new ObjectMapper(), avatarService,
            gameOrchestrator, gameStateRegistry,
            worldHeartbeatUseCase, worldGameStartUseCase, worldStateRegistry, worldOrchestrator,
            recognitionElementRepository, worldExplorationStateRepository,
            childProfileUseCase, accessibleColorRepository, accessibleColorPaletteRepository,
            wsMetrics, SqlStatementCounter.NOOP, wsProps, limitedDispatcher,
            new es.vargontoc.educational.framework.world.service.ChildAgeResolver(),
            GameWebSocketHandlerTest.createNoopCacheStorage());

        GameState gameState = buildGameState(6L, childSessionId);
        lenient().when(gameStateRegistry.findByChildSessionId(childSessionId)).thenReturn(Optional.of(gameState));

        CountDownLatch block = new CountDownLatch(1);
        CountDownLatch firstStarted = new CountDownLatch(1);
        lenient().when(gameOrchestrator.processAction(eq(6L), any(), any(), any())).thenAnswer(inv -> {
            firstStarted.countDown();
            block.await(10, TimeUnit.SECONDS);
            return buildActionResult(ActionResultType.CORRECT, false, gameState);
        });

        limitedHandler.afterConnectionEstablished(session);
        limitedHandler.handleTextMessage(session, new TextMessage("{\"type\":\"auth\",\"childSessionId\":" + childSessionId + "}"));
        limitedDispatcher.awaitQuiescence(2000);

        limitedHandler.handleTextMessage(session, new TextMessage("{\"type\":\"game_action\",\"gameId\":6,\"action\":\"tap\"}"));
        assertTrue(firstStarted.await(5, TimeUnit.SECONDS));

        for (int i = 0; i < 10; i++) {
            limitedHandler.handleTextMessage(session, new TextMessage("{\"type\":\"game_action\",\"gameId\":6,\"action\":\"tap\"}"));
        }

        Thread.sleep(500);
        verify(session, org.mockito.Mockito.atLeastOnce()).close(CloseStatus.POLICY_VIOLATION);

        block.countDown();
        limitedDispatcher.shutdown();
    }

    @Test
    void audioNonBlocking_gameReadyRespondsWithoutWaitingForTts() throws Exception {
        Long childSessionId = 600L;
        WebSocketSession session = mockAuthenticatedSession(childSessionId);

        GameState gameState = buildGameState(7L, childSessionId);
        lenient().when(gameStateRegistry.findByChildSessionId(childSessionId)).thenReturn(Optional.of(gameState));
        lenient().when(gameOrchestrator.readyGame(eq(7L), eq(false))).thenReturn(gameState);
        lenient().when(gameOrchestrator.attachRoundAudio(7L)).thenReturn(gameState);

        long start = System.currentTimeMillis();
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"game_ready\"}"));
        dispatcher.awaitQuiescence(5000);
        long duration = System.currentTimeMillis() - start;

        assertTrue(duration < 2000, "game_ready took " + duration + "ms, should not wait for TTS");
    }

    @Test
    void heartbeatDoesNotBlockOnSlowGameAction() throws Exception {
        Long childSessionId = 700L;
        WebSocketSession session = mockAuthenticatedSession(childSessionId);

        GameState gameState = buildGameState(8L, childSessionId);
        lenient().when(gameStateRegistry.findByChildSessionId(childSessionId)).thenReturn(Optional.of(gameState));

        CountDownLatch actionStarted = new CountDownLatch(1);
        CountDownLatch heartbeatCompleted = new CountDownLatch(1);
        CountDownLatch releaseAction = new CountDownLatch(1);

        lenient().when(gameOrchestrator.processAction(eq(8L), any(), any(), any())).thenAnswer(inv -> {
            actionStarted.countDown();
            releaseAction.await(10, TimeUnit.SECONDS);
            return buildActionResult(ActionResultType.CORRECT, false, gameState);
        });

        lenient().doAnswer(inv -> {
            heartbeatCompleted.countDown();
            return null;
        }).when(childSessionUseCase).recordHeartbeat(childSessionId);

        handler.handleTextMessage(session, new TextMessage("{\"type\":\"game_action\",\"gameId\":8,\"action\":\"tap\"}"));
        assertTrue(actionStarted.await(5, TimeUnit.SECONDS));

        handler.handleTextMessage(session, new TextMessage("{\"type\":\"heartbeat\"}"));

        assertTrue(heartbeatCompleted.await(5, TimeUnit.SECONDS),
            "Heartbeat was blocked by slow game_action");

        releaseAction.countDown();
    }

    private WebSocketSession mockAuthenticatedSession(Long childSessionId) throws IOException {
        return mockAuthenticatedSessionForHandler(handler, childSessionId);
    }

    private WebSocketSession mockAuthenticatedSessionForHandler(GameWebSocketHandler h, Long childSessionId) throws IOException {
        WebSocketSession session = mock(WebSocketSession.class);
        Map<String, Object> attrs = new HashMap<>();
        lenient().when(session.getId()).thenReturn("ws-" + childSessionId);
        lenient().when(session.getAttributes()).thenReturn(attrs);
        lenient().when(session.isOpen()).thenReturn(true);

        var childSession = new ChildSession();
        childSession.setId(childSessionId);
        childSession.setStatus(ChildSessionStatus.ACTIVE);
        childSession.setChildProfileId(1L);
        lenient().when(childSessionUseCase.getSession(childSessionId)).thenReturn(childSession);

        h.afterConnectionEstablished(session);
        h.handleTextMessage(session, new TextMessage("{\"type\":\"auth\",\"childSessionId\":" + childSessionId + "}"));
        try { dispatcher.awaitQuiescence(2000); } catch (InterruptedException ignored) {}

        return session;
    }

    private GameState buildGameState(Long gameId, Long childSessionId) {
        GameState state = new GameState();
        state.setGameId(gameId);
        state.setChildSessionId(childSessionId);
        state.setActivityId(1L);
        state.setDifficultyLevelId(1L);
        state.setStatus(GameStatus.IN_PROGRESS);
        state.setEngine(EngineType.RECOGNITION);
        return state;
    }

    private ActionProcessingResult buildActionResult(ActionResultType resultType, boolean completed, GameState state) {
        state.setRoundAudioResult(RoundAudioResult.noAudio("test"));
        return new ActionProcessingResult(resultType, 1000, state, false, null, completed, List.of(), null);
    }
}
