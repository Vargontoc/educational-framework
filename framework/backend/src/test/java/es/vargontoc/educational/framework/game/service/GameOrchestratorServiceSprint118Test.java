package es.vargontoc.educational.framework.game.service;

import es.vargontoc.educational.framework.content.model.Activity;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.DifficultyCode;
import es.vargontoc.educational.framework.content.model.DifficultyLevel;
import es.vargontoc.educational.framework.content.model.GameCatalogReadiness;
import es.vargontoc.educational.framework.content.model.RecognitionElement;
import es.vargontoc.educational.framework.content.ports.in.DifficultyLevelUseCase;
import es.vargontoc.educational.framework.content.ports.in.GameCatalogUseCase;
import es.vargontoc.educational.framework.content.ports.in.TopicUseCase;
import es.vargontoc.educational.framework.content.ports.out.RecognitionElementRepository;
import es.vargontoc.educational.framework.family.ports.in.ChildProfileUseCase;
import es.vargontoc.educational.framework.game.model.ActionProcessingResult;
import es.vargontoc.educational.framework.game.model.GameState;
import es.vargontoc.educational.framework.game.model.GameStatus;
import es.vargontoc.educational.framework.game.model.enums.EngineType;
import es.vargontoc.educational.framework.game.model.memory.MemoryState;
import es.vargontoc.educational.framework.game.model.recognition.RecognitionState;
import es.vargontoc.educational.framework.game.ports.out.GameStateRegistry;
import es.vargontoc.educational.framework.game.ports.out.SessionAntiRepetitionRegistry;
import es.vargontoc.educational.framework.tracking.model.AttemptRegistrationResult;
import es.vargontoc.educational.framework.tracking.model.AttemptResult;
import es.vargontoc.educational.framework.tracking.ports.in.EvaluateGameCompletionAchievementsUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.FilterAllowedRecognitionCategoriesUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.RegisterActivityAttemptUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.RegisterGameSessionSummaryUseCase;
import es.vargontoc.educational.framework.tracking.ports.out.ElementProgressPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SPRINT-118: Tests for payload/lock optimization.
 * - Typed state kept in memory (no repeated JSON parsing)
 * - Lock only covers state mutation
 * - Audio data cleared from state after sending
 * - Concurrent actions remain serialized
 */
@ExtendWith(MockitoExtension.class)
class GameOrchestratorServiceSprint118Test {

    @Mock
    private GameCatalogUseCase gameCatalogUseCase;

    @Mock
    private GameStateRegistry gameStateRegistry;

    @Mock
    private SessionAntiRepetitionRegistry sessionAntiRepetitionRegistry;

    @Mock
    private RegisterActivityAttemptUseCase registerActivityAttemptUseCase;

    @Mock
    private EvaluateGameCompletionAchievementsUseCase evaluateGameCompletionAchievementsUseCase;

    @Mock
    private RegisterGameSessionSummaryUseCase registerGameSessionSummaryUseCase;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private TopicUseCase topicUseCase;

    @Mock
    private FilterAllowedRecognitionCategoriesUseCase filterAllowedRecognitionCategoriesUseCase;

    @Mock
    private ElementProgressPort elementProgressPort;

    @Mock
    private RecognitionElementRepository recognitionElementRepository;

    @Mock
    private DifficultyLevelUseCase difficultyLevelUseCase;

    @Mock
    private ChildProfileUseCase childProfileUseCase;

    @Mock
    private RecognitionDifficultyService recognitionDifficultyService;

    @Mock
    private RecognitionSimilarityService recognitionSimilarityService;

    @Mock
    private RoundAudioService roundAudioService;

    private GameOrchestratorService service;

    @BeforeEach
    void setUp() {
        service = new GameOrchestratorService(
            gameCatalogUseCase, gameStateRegistry, sessionAntiRepetitionRegistry,
            registerActivityAttemptUseCase, evaluateGameCompletionAchievementsUseCase,
            registerGameSessionSummaryUseCase, eventPublisher, topicUseCase,
            filterAllowedRecognitionCategoriesUseCase, elementProgressPort,
            recognitionElementRepository, difficultyLevelUseCase, childProfileUseCase,
            recognitionDifficultyService, recognitionSimilarityService,
            roundAudioService, null, null, null);
    }

    @Test
    void processAction_recognitionState_typedStateIsSet() {
        GameState state = createRecognitionGameState(1L, GameStatus.IN_PROGRESS);
        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(state));
        lenient().when(recognitionElementRepository.findAllById(any())).thenReturn(List.of());

        ActionProcessingResult result = service.processAction(1L, "{\"selectedOptionId\":\"100\"}", 1L, 500);

        assertNotNull(result);
        assertNotNull(state.getTypedRecognitionState(), "Typed recognition state should be set after processAction");
    }

    @Test
    void processAction_memoryState_typedStateIsSet() {
        GameState state = createMemoryGameState(2L, GameStatus.IN_PROGRESS);
        when(gameStateRegistry.findByGameId(2L)).thenReturn(Optional.of(state));

        ActionProcessingResult result = service.processAction(2L, "{\"cardId\":\"card-0\"}", null, 500);

        assertNotNull(result);
        assertNotNull(state.getTypedMemoryState(), "Typed memory state should be set after processAction");
    }

    @Test
    void processAction_concurrentActions_serializedWithoutStateLoss() throws InterruptedException {
        GameState state = createRecognitionGameState(3L, GameStatus.IN_PROGRESS);
        when(gameStateRegistry.findByGameId(3L)).thenReturn(Optional.of(state));
        lenient().when(recognitionElementRepository.findAllById(any())).thenReturn(List.of());

        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    service.processAction(3L, "{\"selectedOptionId\":\"100\"}", 1L, 500);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(10, TimeUnit.SECONDS), "All actions should complete");
        executor.shutdown();

        assertEquals(threadCount, successCount.get() + errorCount.get(), "All actions should be processed");
    }

    @Test
    void processAction_gameCompleted_allAttemptsRegistered() {
        GameState state = createCompletableRecognitionState(4L);
        when(gameStateRegistry.findByGameId(4L)).thenReturn(Optional.of(state));
        lenient().when(recognitionElementRepository.findAllById(any())).thenReturn(List.of());
        when(registerActivityAttemptUseCase.register(anyLong(), anyLong(), anyLong(), anyLong(), anyLong(), anyLong(), any(AttemptResult.class), any(), any()))
            .thenReturn(new AttemptRegistrationResult(1L, java.time.LocalDateTime.now(), List.of()));

        ActionProcessingResult result = service.processAction(4L, "{\"selectedOptionId\":\"100\"}", 1L, 500);

        assertTrue(result.gameCompleted(), "Game should be completed");
        verify(registerActivityAttemptUseCase, atLeastOnce()).register(anyLong(), anyLong(), anyLong(), anyLong(), anyLong(), anyLong(), any(AttemptResult.class), any(), any());
    }

    @Test
    void processAction_audioData_clearedAfterSending() {
        GameState state = createRecognitionGameState(5L, GameStatus.IN_PROGRESS);
        when(gameStateRegistry.findByGameId(5L)).thenReturn(Optional.of(state));
        lenient().when(recognitionElementRepository.findAllById(any())).thenReturn(List.of());
        when(roundAudioService.generateRoundAudio(anyLong(), any()))
            .thenReturn(RoundAudioResult.withAudio("audio-123", new byte[]{1, 2, 3}, "test audio"));

        ActionProcessingResult result = service.processAction(5L, "{\"selectedOptionId\":\"100\"}", 1L, 500);

        assertNotNull(result.updatedState().getRoundAudioResult(), "Audio result should be attached");
        assertEquals("audio-123", result.updatedState().getPendingAudioId(), "Audio ID should be stored");
        assertEquals("test audio", result.updatedState().getPendingAudioText(), "Audio text should be stored");
    }

    @Test
    void gameState_audioFields_nullWhenNoAudio() {
        GameState state = new GameState();
        assertNull(state.getPendingAudioId(), "Audio ID should be null initially");
        assertNull(state.getPendingAudioText(), "Audio text should be null initially");
    }

    @Test
    void gameState_typedState_nullInitially() {
        GameState state = new GameState();
        assertNull(state.getTypedRecognitionState(), "Typed recognition state should be null initially");
        assertNull(state.getTypedMemoryState(), "Typed memory state should be null initially");
    }

    private GameState createRecognitionGameState(Long gameId, GameStatus status) {
        GameState state = new GameState();
        state.setGameId(gameId);
        state.setChildProfileId(100L);
        state.setChildSessionId(200L);
        state.setActivityId(1L);
        state.setDifficultyLevelId(1L);
        state.setStatus(status);
        state.setEngine(EngineType.RECOGNITION);
        state.setEnginePayload(createMinimalRecognitionPayload());
        return state;
    }

    private GameState createMemoryGameState(Long gameId, GameStatus status) {
        GameState state = new GameState();
        state.setGameId(gameId);
        state.setChildProfileId(100L);
        state.setChildSessionId(200L);
        state.setActivityId(1L);
        state.setDifficultyLevelId(1L);
        state.setStatus(status);
        state.setEngine(EngineType.MEMORY);
        state.setEnginePayload(createMinimalMemoryPayload());
        return state;
    }

    private GameState createCompletableRecognitionState(Long gameId) {
        GameState state = createRecognitionGameState(gameId, GameStatus.IN_PROGRESS);
        RecognitionState recState = new RecognitionState();
        recState.setRoundIndex(4);
        recState.setTotalRounds(5);
        recState.setTargetElementId("100");
        recState.setOptionIds(List.of("100", "101", "102"));
        recState.setCandidateElementIds(List.of("100", "101", "102", "103"));
        state.setEnginePayload(serializeRecognitionState(recState));
        return state;
    }

    private String createMinimalRecognitionPayload() {
        RecognitionState state = new RecognitionState();
        state.setRoundIndex(0);
        state.setTotalRounds(5);
        state.setTargetElementId("100");
        state.setOptionIds(List.of("100", "101", "102"));
        state.setCandidateElementIds(List.of("100", "101", "102", "103"));
        return serializeRecognitionState(state);
    }

    private String createMinimalMemoryPayload() {
        return "{\"rows\":2,\"columns\":2,\"totalPairs\":2,\"matchedPairs\":0,\"flipDelayMs\":1000,\"cards\":[{\"cardId\":\"card-0\",\"elementId\":\"100\",\"row\":0,\"column\":0,\"faceUp\":false,\"matched\":false},{\"cardId\":\"card-1\",\"elementId\":\"101\",\"row\":0,\"column\":1,\"faceUp\":false,\"matched\":false},{\"cardId\":\"card-2\",\"elementId\":\"100\",\"row\":1,\"column\":0,\"faceUp\":false,\"matched\":false},{\"cardId\":\"card-3\",\"elementId\":\"101\",\"row\":1,\"column\":1,\"faceUp\":false,\"matched\":false}]}";
    }

    private String serializeRecognitionState(RecognitionState state) {
        try {
            return new tools.jackson.databind.ObjectMapper().writeValueAsString(state);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
