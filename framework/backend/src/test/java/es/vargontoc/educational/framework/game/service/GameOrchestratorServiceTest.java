package es.vargontoc.educational.framework.game.service;

import es.vargontoc.educational.framework.game.service.RecognitionSimilarityService;
import es.vargontoc.educational.framework.content.model.Activity;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.DifficultyCode;
import es.vargontoc.educational.framework.content.model.DifficultyLevel;
import es.vargontoc.educational.framework.content.model.GameCatalogReadiness;
import es.vargontoc.educational.framework.content.ports.in.GameCatalogUseCase;
import es.vargontoc.educational.framework.content.ports.in.TopicUseCase;
import es.vargontoc.educational.framework.game.exception.GameNotFoundException;
import es.vargontoc.educational.framework.game.exception.InvalidStateTransitionException;
import es.vargontoc.educational.framework.game.model.ActionProcessingResult;
import es.vargontoc.educational.framework.game.model.ActionResultType;
import es.vargontoc.educational.framework.game.model.GameState;
import es.vargontoc.educational.framework.game.model.GameStatus;
import es.vargontoc.educational.framework.game.model.enums.EngineType;
import es.vargontoc.educational.framework.game.model.event.GameSessionCompletedEvent;
import es.vargontoc.educational.framework.game.model.event.GameSessionDiscardedEvent;
import es.vargontoc.educational.framework.game.ports.out.GameStateRegistry;
import es.vargontoc.educational.framework.game.ports.out.SessionAntiRepetitionRegistry;
import es.vargontoc.educational.framework.tracking.model.AttemptRegistrationResult;
import es.vargontoc.educational.framework.tracking.model.GameSessionAbandonReason;
import es.vargontoc.educational.framework.tracking.model.UnlockedAchievement;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameOrchestratorServiceTest {

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
    private es.vargontoc.educational.framework.content.ports.out.RecognitionElementRepository recognitionElementRepository;

    @Mock
    private es.vargontoc.educational.framework.content.ports.in.DifficultyLevelUseCase difficultyLevelUseCase;

    @Mock
    private es.vargontoc.educational.framework.family.ports.in.ChildProfileUseCase childProfileUseCase;

    @Mock
    private RecognitionDifficultyService recognitionDifficultyService;

    @Mock
    private RecognitionSimilarityService recognitionSimilarityService;

    @Mock
    private RoundAudioService roundAudioService;

    private GameOrchestratorService orchestratorService;

    @BeforeEach
    void setUp() {
        orchestratorService = new GameOrchestratorService(
            gameCatalogUseCase,
            gameStateRegistry,
            sessionAntiRepetitionRegistry,
            registerActivityAttemptUseCase,
            evaluateGameCompletionAchievementsUseCase,
            registerGameSessionSummaryUseCase,
            eventPublisher,
            topicUseCase,
            filterAllowedRecognitionCategoriesUseCase,
            elementProgressPort,
            recognitionElementRepository,
            difficultyLevelUseCase,
            childProfileUseCase,
            recognitionDifficultyService,
            recognitionSimilarityService,
            roundAudioService
        );
    }

    private Activity createActivity(Long id) {
        Activity activity = new Activity();
        activity.setId(id);
        activity.setName("Test Activity");
        activity.setStatus(ContentStatus.ACTIVE);
        activity.setGameEngineType(EngineType.RECOGNITION.name());
        return activity;
    }

    private DifficultyLevel createDifficultyLevel(Long id) {
        DifficultyLevel dl = new DifficultyLevel();
        dl.setId(id);
        dl.setDifficultyCode(DifficultyCode.EASY);
        dl.setEngineParams("{\"speed\":1}");
        return dl;
    }

    private GameState createRealGameState(Long gameId, Long childSessionId, Long childProfileId, Long activityId, Long difficultyLevelId, GameStatus status) {
        GameState state = new GameState();
        state.setGameId(gameId);
        state.setChildSessionId(childSessionId);
        state.setChildProfileId(childProfileId);
        state.setActivityId(activityId);
        state.setDifficultyLevelId(difficultyLevelId);
        state.setEngine(EngineType.RECOGNITION);
        state.setStatus(status);
        state.setSequenceNumber(0);
        state.setAttempts(0);
        state.setCorrectAttempts(0);
        state.setIncorrectAttempts(0);
        state.setTimeoutAttempts(0);
        state.setCurrentScore(BigDecimal.ZERO);
        state.setCurrentStreak(0);
        state.setStarsEarned(0);
        state.setCandidates(List.of("elem-1"));
        if (status == GameStatus.IN_PROGRESS || status == GameStatus.STARTING) {
            state.setEnginePayload(buildTestEnginePayload());
        }
        return state;
    }

    private String buildTestEnginePayload() {
        return "{\"roundIndex\":0,\"totalRounds\":3," +
               "\"candidateElementIds\":[\"elem-1\"]," +
               "\"targetElementId\":\"elem-1\",\"optionIds\":[\"elem-1\"]," +
               "\"roundsShownElementIds\":[],\"currentRoundAttemptCount\":0," +
               "\"currentRoundConsecutiveFailures\":0,\"totalIncorrectAttempts\":0," +
               "\"totalCorrectFirstTry\":0,\"hintActive\":false,\"totalResponseTimeMs\":0}";
    }

    @Test
    void startGame_createsWaitingState() {
        Activity activity = createActivity(1L);
        DifficultyLevel difficultyLevel = createDifficultyLevel(5L);
        GameCatalogReadiness readiness = new GameCatalogReadiness(activity, difficultyLevel, true);

        when(gameCatalogUseCase.getGameReadiness(100L, 1L)).thenReturn(readiness);
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));

        GameState result = orchestratorService.startGame(100L, 1L);

        assertNotNull(result);
        assertEquals(GameStatus.WAITING, result.getStatus());
        assertEquals(100L, result.getChildProfileId());
        assertEquals(1L, result.getActivityId());
        assertEquals(5L, result.getDifficultyLevelId());
        assertNotNull(result.getStartedAt());
        verify(gameStateRegistry).save(any(GameState.class));
    }

    @Test
    void readyGame_transitionsToStartingThenInProgress() {
        GameState storedState = createRealGameState(1L, 100L, 200L, 1L, 5L, GameStatus.WAITING);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));

        GameState result = orchestratorService.readyGame(1L);

        assertEquals(GameStatus.IN_PROGRESS, result.getStatus());
        verify(gameStateRegistry, times(2)).save(any(GameState.class));
    }

    @Test
    void readyGame_invalidTransition_throwsException() {
        GameState storedState = createRealGameState(1L, 100L, 200L, 1L, 5L, GameStatus.IN_PROGRESS);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));

        assertThrows(InvalidStateTransitionException.class, () -> orchestratorService.readyGame(1L));
    }

    @Test
    void processAction_correctAction_buffersAttemptWithoutImmediateTracking() {
        GameState storedState = createRealGameState(1L, 100L, 200L, 1L, 5L, GameStatus.IN_PROGRESS);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));

        ActionProcessingResult result = orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);

        assertEquals(es.vargontoc.educational.framework.game.model.ActionResultType.CORRECT, result.resultType());
        assertNotNull(result.updatedState());
        verify(registerActivityAttemptUseCase, never()).register(any(), any(), any(), any(), any(), any(), any(), any(), any());
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    void processAction_intermediateRounds_doNotTriggerAttemptRegistration() {
        GameState storedState = createRealGameState(1L, 100L, 200L, 1L, 5L, GameStatus.IN_PROGRESS);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));
        doAnswer(invocation -> null).when(gameStateRegistry).remove(anyLong());
        when(registerActivityAttemptUseCase.register(anyLong(), anyLong(), anyLong(), isNull(), isNull(), anyLong(), any(), any(), any()))
            .thenReturn(new AttemptRegistrationResult(1L, LocalDateTime.now(), List.of()));
        when(evaluateGameCompletionAchievementsUseCase.evaluate(anyLong(), anyLong(), isNull()))
            .thenReturn(List.of());

        orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);
        verify(registerActivityAttemptUseCase, never()).register(any(), any(), any(), any(), any(), any(), any(), any(), any());

        orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);
        verify(registerActivityAttemptUseCase, never()).register(any(), any(), any(), any(), any(), any(), any(), any(), any());

        orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);
        verify(registerActivityAttemptUseCase, times(3)).register(anyLong(), anyLong(), anyLong(), isNull(), isNull(), anyLong(), any(), any(), any());
    }

    @Test
    void processAction_completedAction_calls_game_completion_and_summary() {
        GameState storedState = createRealGameState(1L, 100L, 200L, 1L, 5L, GameStatus.IN_PROGRESS);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));
        doAnswer(invocation -> null).when(gameStateRegistry).remove(anyLong());
        when(registerActivityAttemptUseCase.register(anyLong(), anyLong(), anyLong(), isNull(), isNull(), anyLong(), any(), any(), any()))
            .thenReturn(new AttemptRegistrationResult(1L, LocalDateTime.now(), List.of()));
        when(evaluateGameCompletionAchievementsUseCase.evaluate(anyLong(), anyLong(), isNull()))
            .thenReturn(List.of());

        orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);
        orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);
        ActionProcessingResult finalResult = orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);

        assertTrue(finalResult.gameCompleted());
        verify(registerActivityAttemptUseCase, times(3)).register(anyLong(), anyLong(), anyLong(), isNull(), isNull(), anyLong(), any(), any(), any());
        verify(evaluateGameCompletionAchievementsUseCase).evaluate(anyLong(), anyLong(), isNull());
        verify(registerGameSessionSummaryUseCase).registerGameSessionSummary(
            anyLong(), anyLong(), anyLong(), anyLong(), anyLong(),
            any(), any(), any(), any(), any(), any(), any(), isNull()
        );
        verify(gameStateRegistry).remove(anyLong());
    }

    @Test
    void processAction_gameNotInProgress_throwsException() {
        GameState storedState = createRealGameState(1L, 100L, 200L, 1L, 5L, GameStatus.WAITING);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));

        assertThrows(InvalidStateTransitionException.class,
            () -> orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000));
    }

    @Test
    void abandonGame_removesFromRegistryAndRegistersSummary() {
        GameState storedState = createRealGameState(1L, 100L, 200L, 1L, 5L, GameStatus.IN_PROGRESS);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));

        GameState result = orchestratorService.abandonGame(1L);

        assertEquals(GameStatus.ABANDONED, result.getStatus());
        verify(gameStateRegistry).remove(1L);
        verify(registerGameSessionSummaryUseCase).registerGameSessionSummary(
            eq(200L), eq(100L), eq(1L), eq(5L), eq(5L), anyInt(), anyInt(), anyInt(), anyInt(),
            any(), any(), eq(es.vargontoc.educational.framework.tracking.model.GameSessionFinalStatus.ABANDONED),
            eq(GameSessionAbandonReason.CLIENT_REQUESTED)
        );
    }

    @Test
    void abandonGame_beforeCompletion_discardsBufferedAttemptsWithoutRegistering() {
        GameState storedState = createRealGameState(1L, 100L, 200L, 1L, 5L, GameStatus.IN_PROGRESS);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));

        orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);

        orchestratorService.abandonGame(1L);

        verify(registerActivityAttemptUseCase, never()).register(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void abandonGame_alreadyCompleted_throwsException() {
        GameState storedState = createRealGameState(1L, 100L, 200L, 1L, 5L, GameStatus.COMPLETED);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));

        assertThrows(InvalidStateTransitionException.class,
            () -> orchestratorService.abandonGame(1L));
    }

    @Test
    void readyGame_notDevProfile_throwsEngineNotAvailable() {
        orchestratorService = new GameOrchestratorService(
            gameCatalogUseCase,
            gameStateRegistry,
            sessionAntiRepetitionRegistry,
            registerActivityAttemptUseCase,
            evaluateGameCompletionAchievementsUseCase,
            registerGameSessionSummaryUseCase,
            eventPublisher,
            topicUseCase,
            filterAllowedRecognitionCategoriesUseCase,
            elementProgressPort,
            recognitionElementRepository,
            difficultyLevelUseCase,
            childProfileUseCase,
            recognitionDifficultyService,
            recognitionSimilarityService,
            roundAudioService
        );

        GameState storedState = createRealGameState(1L, 100L, 200L, 1L, 5L, GameStatus.WAITING);
        storedState.setEngine(EngineType.MEMORY);
        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));

        assertThrows(es.vargontoc.educational.framework.game.exception.EngineNotAvailableException.class,
            () -> orchestratorService.readyGame(1L));
    }

    @Test
    void readyGame_gameNotFound_throwsException() {
        when(gameStateRegistry.findByGameId(999L)).thenReturn(Optional.empty());

        assertThrows(GameNotFoundException.class, () -> orchestratorService.readyGame(999L));
    }

    @Test
    void processAction_unlockedAchievements_areReturnedOnCompletion() {
        GameState storedState = createRealGameState(1L, 100L, 200L, 1L, 5L, GameStatus.IN_PROGRESS);

        UnlockedAchievement achievement = new UnlockedAchievement("FIRST_CORRECT_STREAK", 1L, null);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));
        doAnswer(invocation -> null).when(gameStateRegistry).remove(anyLong());
        when(registerActivityAttemptUseCase.register(anyLong(), anyLong(), anyLong(), isNull(), isNull(), anyLong(), any(), any(), any()))
            .thenReturn(new AttemptRegistrationResult(1L, LocalDateTime.now(), List.of(achievement)));
        when(evaluateGameCompletionAchievementsUseCase.evaluate(anyLong(), anyLong(), isNull()))
            .thenReturn(List.of());

        orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);
        orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);
        ActionProcessingResult result = orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);

        assertTrue(result.gameCompleted());
        assertFalse(result.unlockedAchievements().isEmpty());
        assertEquals("FIRST_CORRECT_STREAK", result.unlockedAchievements().get(0).achievementCode());
    }

    @Test
    void processAction_difficultyChange_appliedOnCompletion() {
        GameState storedState = createRealGameState(1L, 100L, 200L, 1L, 5L, GameStatus.IN_PROGRESS);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));
        doAnswer(invocation -> null).when(gameStateRegistry).remove(anyLong());
        when(registerActivityAttemptUseCase.register(anyLong(), anyLong(), anyLong(), isNull(), isNull(), anyLong(), any(), any(), any()))
            .thenReturn(new AttemptRegistrationResult(1L, LocalDateTime.now(), List.of(), true, 10L));
        when(evaluateGameCompletionAchievementsUseCase.evaluate(anyLong(), anyLong(), isNull()))
            .thenReturn(List.of());

        orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);
        orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);
        ActionProcessingResult result = orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);

        assertTrue(result.difficultyChanged());
        assertEquals(10L, result.newDifficultyLevelId());
        assertEquals(10L, result.updatedState().getDifficultyLevelId());
    }

    @Test
    void discardGameForSession_withActiveGame_discardsWithoutRegisteringSummary() {
        GameState storedState = createRealGameState(1L, 100L, 200L, 1L, 5L, GameStatus.IN_PROGRESS);

        when(gameStateRegistry.findByChildSessionId(100L)).thenReturn(Optional.of(storedState));
        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));

        orchestratorService.discardGameForSession(100L);

        assertTrue(storedState.isSystemEventPending());
        assertEquals(GameStatus.ABANDONED, storedState.getStatus());
        verify(gameStateRegistry).remove(1L);
        verify(registerGameSessionSummaryUseCase, never()).registerGameSessionSummary(
            any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        );
        verify(eventPublisher).publishEvent(any(GameSessionDiscardedEvent.class));
        verify(eventPublisher, never()).publishEvent(any(GameSessionCompletedEvent.class));
    }

    @Test
    void discardGameForSession_withoutActiveGame_doesNothing() {
        when(gameStateRegistry.findByChildSessionId(100L)).thenReturn(Optional.empty());

        orchestratorService.discardGameForSession(100L);

        verify(gameStateRegistry, never()).remove(anyLong());
        verify(registerGameSessionSummaryUseCase, never()).registerGameSessionSummary(
            any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        );
    }

    @Test
    void processAction_withSystemEventPending_discardsActionAndDoesNotRegisterAttempt() {
        GameState storedState = createRealGameState(1L, 100L, 200L, 1L, 5L, GameStatus.IN_PROGRESS);
        storedState.setSystemEventPending(true);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));

        ActionProcessingResult result = orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);

        assertEquals(ActionResultType.CORRECT, result.resultType());
        assertEquals("discarded_system_event_pending", result.attemptContext());
        verify(registerActivityAttemptUseCase, never()).register(
            anyLong(), anyLong(), anyLong(), any(), any(), anyLong(), any(), any(), any()
        );
    }

    @Test
    void processAction_flushTrackingFails_continuesWithoutTracking() {
        GameState storedState = createRealGameState(1L, 100L, 200L, 1L, 5L, GameStatus.IN_PROGRESS);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));
        doAnswer(invocation -> null).when(gameStateRegistry).remove(anyLong());
        when(registerActivityAttemptUseCase.register(anyLong(), anyLong(), anyLong(), any(), any(), anyLong(), any(), any(), any()))
            .thenThrow(new RuntimeException("Tracking service unavailable"));

        orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);
        orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);
        ActionProcessingResult result = orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);

        assertTrue(result.gameCompleted());
        assertTrue(result.unlockedAchievements().isEmpty());
    }

    @Test
    void processAction_withDifferentGameIds_runConcurrently() {
        GameState state1 = createRealGameState(1L, 100L, 200L, 1L, 5L, GameStatus.IN_PROGRESS);
        GameState state2 = createRealGameState(2L, 101L, 201L, 1L, 5L, GameStatus.IN_PROGRESS);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(state1));
        when(gameStateRegistry.findByGameId(2L)).thenReturn(Optional.of(state2));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));

        ActionProcessingResult result1 = orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);
        ActionProcessingResult result2 = orchestratorService.processAction(2L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);

        assertEquals(ActionResultType.CORRECT, result1.resultType());
        assertEquals(ActionResultType.CORRECT, result2.resultType());
    }

    @Test
    void processAction_completedAction_publishesGameSessionCompletedEvent() {
        GameState storedState = createRealGameState(1L, 100L, 200L, 1L, 5L, GameStatus.IN_PROGRESS);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));
        doAnswer(invocation -> null).when(gameStateRegistry).remove(anyLong());
        when(registerActivityAttemptUseCase.register(anyLong(), anyLong(), anyLong(), isNull(), isNull(), anyLong(), any(), any(), any()))
            .thenReturn(new AttemptRegistrationResult(1L, LocalDateTime.now(), List.of()));
        when(evaluateGameCompletionAchievementsUseCase.evaluate(anyLong(), anyLong(), isNull()))
            .thenReturn(List.of());

        orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);
        orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);
        orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);

        verify(eventPublisher).publishEvent(any(GameSessionCompletedEvent.class));
    }

    @Test
    void abandonGame_publishesGameSessionCompletedEvent() {
        GameState storedState = createRealGameState(1L, 100L, 200L, 1L, 5L, GameStatus.IN_PROGRESS);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));

        orchestratorService.abandonGame(1L);

        verify(eventPublisher).publishEvent(any(GameSessionCompletedEvent.class));
    }

    @Test
    void discardGameForSession_publishesGameSessionDiscardedEvent() {
        GameState storedState = createRealGameState(1L, 100L, 200L, 1L, 5L, GameStatus.IN_PROGRESS);

        when(gameStateRegistry.findByChildSessionId(100L)).thenReturn(Optional.of(storedState));
        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));

        orchestratorService.discardGameForSession(100L);

        verify(eventPublisher).publishEvent(any(GameSessionDiscardedEvent.class));
        verify(eventPublisher, never()).publishEvent(any(GameSessionCompletedEvent.class));
    }

    @Test
    void readyGame_repeatedActivityInSameSession_marksStateAsRepetition() {
        GameState firstGame = createRealGameState(1L, 100L, 200L, 1L, 5L, GameStatus.IN_PROGRESS);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(firstGame));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));
        doAnswer(invocation -> null).when(gameStateRegistry).remove(anyLong());
        when(registerActivityAttemptUseCase.register(anyLong(), anyLong(), anyLong(), isNull(), isNull(), anyLong(), any(), any(), any()))
            .thenReturn(new AttemptRegistrationResult(1L, LocalDateTime.now(), List.of()));
        when(evaluateGameCompletionAchievementsUseCase.evaluate(anyLong(), anyLong(), isNull()))
            .thenReturn(List.of());

        orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);
        orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);
        orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);

        GameState secondGame = createRealGameState(2L, 100L, 200L, 1L, 5L, GameStatus.WAITING);
        when(gameStateRegistry.findByGameId(2L)).thenReturn(Optional.of(secondGame));

        GameState result = orchestratorService.readyGame(2L);

        assertTrue(result.isRepetition());
    }

    @Test
    void processAction_repetitionGame_neverRegistersAttemptsOrSummary() {
        GameState storedState = createRealGameState(1L, 100L, 200L, 1L, 5L, GameStatus.IN_PROGRESS);
        storedState.setRepetition(true);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));
        doAnswer(invocation -> null).when(gameStateRegistry).remove(anyLong());

        orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);
        orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);
        ActionProcessingResult result = orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);

        assertTrue(result.gameCompleted());
        verify(registerActivityAttemptUseCase, never()).register(any(), any(), any(), any(), any(), any(), any(), any(), any());
        verify(registerGameSessionSummaryUseCase, never()).registerGameSessionSummary(
            any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        );
        verify(evaluateGameCompletionAchievementsUseCase, never()).evaluate(any(), any(), any());
    }

    @Test
    void abandonGame_repetitionGame_doesNotRegisterSummary() {
        GameState storedState = createRealGameState(1L, 100L, 200L, 1L, 5L, GameStatus.IN_PROGRESS);
        storedState.setRepetition(true);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));

        GameState result = orchestratorService.abandonGame(1L);

        assertEquals(GameStatus.ABANDONED, result.getStatus());
        verify(registerGameSessionSummaryUseCase, never()).registerGameSessionSummary(
            any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        );
    }

    @Test
    void clearSessionData_clearsCompletedActivitiesForSession() {
        GameState firstGame = createRealGameState(1L, 100L, 200L, 1L, 5L, GameStatus.IN_PROGRESS);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(firstGame));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));
        doAnswer(invocation -> null).when(gameStateRegistry).remove(anyLong());
        when(registerActivityAttemptUseCase.register(anyLong(), anyLong(), anyLong(), isNull(), isNull(), anyLong(), any(), any(), any()))
            .thenReturn(new AttemptRegistrationResult(1L, LocalDateTime.now(), List.of()));
        when(evaluateGameCompletionAchievementsUseCase.evaluate(anyLong(), anyLong(), isNull()))
            .thenReturn(List.of());

        orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);
        orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);
        orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);

        orchestratorService.clearSessionData(100L);

        GameState secondGame = createRealGameState(2L, 100L, 200L, 1L, 5L, GameStatus.WAITING);
        when(gameStateRegistry.findByGameId(2L)).thenReturn(Optional.of(secondGame));

        GameState result = orchestratorService.readyGame(2L);

        assertFalse(result.isRepetition());
        verify(sessionAntiRepetitionRegistry).clearSession(100L);
    }

    @Test
    void readyGame_resolvesRoundParametersFromDifficultyLevelAndColorVisionMode() {
        GameState storedState = createRealGameState(1L, 100L, 200L, 1L, 5L, GameStatus.WAITING);
        storedState.setRecognitionCategory(es.vargontoc.educational.framework.game.model.enums.RecognitionCategory.LETTER);
        storedState.setCandidates(List.of("500"));

        DifficultyLevel difficultyLevel = createDifficultyLevel(5L);
        difficultyLevel.setDifficultyCode(DifficultyCode.MEDIUM);

        es.vargontoc.educational.framework.family.model.ChildProfile childProfile =
                new es.vargontoc.educational.framework.family.model.ChildProfile();
        childProfile.setColorVisionMode(es.vargontoc.educational.framework.family.model.ColorVisionMode.NONE);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));
        when(difficultyLevelUseCase.getGameReadyDifficultyLevel(5L)).thenReturn(difficultyLevel);
        when(childProfileUseCase.getChild(200L)).thenReturn(childProfile);
        when(recognitionDifficultyService.resolveRoundParameters(
                DifficultyCode.MEDIUM,
                es.vargontoc.educational.framework.game.model.enums.RecognitionCategory.LETTER,
                es.vargontoc.educational.framework.family.model.ColorVisionMode.NONE))
            .thenReturn(new es.vargontoc.educational.framework.game.model.recognition.RoundParameters(
                    3, es.vargontoc.educational.framework.game.model.recognition.DistractorStrategy.SAME_CATEGORY,
                    false, 800, false));

        GameState result = orchestratorService.readyGame(1L);

        assertEquals(GameStatus.IN_PROGRESS, result.getStatus());
        verify(recognitionDifficultyService).resolveRoundParameters(
                DifficultyCode.MEDIUM,
                es.vargontoc.educational.framework.game.model.enums.RecognitionCategory.LETTER,
                es.vargontoc.educational.framework.family.model.ColorVisionMode.NONE);
    }

    @Test
    void readyGame_colorCategoryWithDeuteranopia_marksNonChromaticKeyRequired() {
        GameState storedState = createRealGameState(1L, 100L, 200L, 1L, 5L, GameStatus.WAITING);
        storedState.setRecognitionCategory(es.vargontoc.educational.framework.game.model.enums.RecognitionCategory.COLOR);
        storedState.setCandidates(List.of("500"));

        DifficultyLevel difficultyLevel = createDifficultyLevel(5L);
        difficultyLevel.setDifficultyCode(DifficultyCode.EASY);

        es.vargontoc.educational.framework.family.model.ChildProfile childProfile =
                new es.vargontoc.educational.framework.family.model.ChildProfile();
        childProfile.setColorVisionMode(es.vargontoc.educational.framework.family.model.ColorVisionMode.DEUTERANOPIA);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));
        when(difficultyLevelUseCase.getGameReadyDifficultyLevel(5L)).thenReturn(difficultyLevel);
        when(childProfileUseCase.getChild(200L)).thenReturn(childProfile);

        orchestratorService.readyGame(1L);

        verify(recognitionDifficultyService).resolveRoundParameters(
                DifficultyCode.EASY,
                es.vargontoc.educational.framework.game.model.enums.RecognitionCategory.COLOR,
                es.vargontoc.educational.framework.family.model.ColorVisionMode.DEUTERANOPIA);
    }

    @Test
    void readyGame_missingDifficultyLevel_defaultsToEasy() {
        GameState storedState = createRealGameState(1L, 100L, 200L, 1L, 5L, GameStatus.WAITING);
        storedState.setRecognitionCategory(es.vargontoc.educational.framework.game.model.enums.RecognitionCategory.LETTER);
        storedState.setCandidates(List.of("500"));

        es.vargontoc.educational.framework.family.model.ChildProfile childProfile =
                new es.vargontoc.educational.framework.family.model.ChildProfile();
        childProfile.setColorVisionMode(es.vargontoc.educational.framework.family.model.ColorVisionMode.NONE);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));
        when(difficultyLevelUseCase.getGameReadyDifficultyLevel(5L))
                .thenThrow(new es.vargontoc.educational.framework.shared.exception.ContentNotReadyException("not ready"));
        when(childProfileUseCase.getChild(200L)).thenReturn(childProfile);

        GameState result = orchestratorService.readyGame(1L);

        assertEquals(GameStatus.IN_PROGRESS, result.getStatus());
        verify(recognitionDifficultyService).resolveRoundParameters(
                eq(DifficultyCode.EASY),
                eq(es.vargontoc.educational.framework.game.model.enums.RecognitionCategory.LETTER),
                any());
    }

    @Test
    void readyGame_missingChildProfile_defaultsToColorVisionModeNone() {
        GameState storedState = createRealGameState(1L, 100L, 200L, 1L, 5L, GameStatus.WAITING);
        storedState.setRecognitionCategory(es.vargontoc.educational.framework.game.model.enums.RecognitionCategory.LETTER);
        storedState.setCandidates(List.of("500"));

        DifficultyLevel difficultyLevel = createDifficultyLevel(5L);
        difficultyLevel.setDifficultyCode(DifficultyCode.EASY);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));
        when(difficultyLevelUseCase.getGameReadyDifficultyLevel(5L)).thenReturn(difficultyLevel);
        when(childProfileUseCase.getChild(200L))
                .thenThrow(new es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException("not found"));

        GameState result = orchestratorService.readyGame(1L);

        assertEquals(GameStatus.IN_PROGRESS, result.getStatus());
        verify(recognitionDifficultyService).resolveRoundParameters(
                eq(DifficultyCode.EASY),
                eq(es.vargontoc.educational.framework.game.model.enums.RecognitionCategory.LETTER),
                eq(es.vargontoc.educational.framework.family.model.ColorVisionMode.NONE));
    }
}
