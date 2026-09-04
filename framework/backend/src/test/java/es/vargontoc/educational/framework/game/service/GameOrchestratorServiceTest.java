package es.vargontoc.educational.framework.game.service;

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
import es.vargontoc.educational.framework.game.ports.out.GameStateRegistry;
import es.vargontoc.educational.framework.tracking.model.AttemptRegistrationResult;
import es.vargontoc.educational.framework.tracking.model.UnlockedAchievement;
import es.vargontoc.educational.framework.tracking.ports.in.EvaluateGameCompletionAchievementsUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.FilterAllowedRecognitionCategoriesUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.RegisterActivityAttemptUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.RegisterGameSessionSummaryUseCase;
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
import static org.mockito.Mockito.doThrow;
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

    private GameOrchestratorService orchestratorService;

    @BeforeEach
    void setUp() {
        orchestratorService = new GameOrchestratorService(
            gameCatalogUseCase,
            gameStateRegistry,
            registerActivityAttemptUseCase,
            evaluateGameCompletionAchievementsUseCase,
            registerGameSessionSummaryUseCase,
            eventPublisher,
            topicUseCase,
            filterAllowedRecognitionCategoriesUseCase
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

    private GameState createRealGameState(Long gameId, Long childSessionId, Long activityId, Long difficultyLevelId, GameStatus status) {
        GameState state = new GameState();
        state.setGameId(gameId);
        state.setChildSessionId(childSessionId);
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
        return "{\"roundIndex\":0,\"totalRounds\":3,\"currentDifficultyLevel\":1," +
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
        assertEquals(100L, result.getChildSessionId());
        assertEquals(1L, result.getActivityId());
        assertEquals(5L, result.getDifficultyLevelId());
        assertNotNull(result.getStartedAt());
        verify(gameStateRegistry).save(any(GameState.class));
    }

    @Test
    void readyGame_transitionsToStartingThenInProgress() {
        GameState storedState = createRealGameState(1L, 100L, 1L, 5L, GameStatus.WAITING);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));

        GameState result = orchestratorService.readyGame(1L);

        assertEquals(GameStatus.IN_PROGRESS, result.getStatus());
        verify(gameStateRegistry, times(2)).save(any(GameState.class));
    }

    @Test
    void readyGame_invalidTransition_throwsException() {
        GameState storedState = createRealGameState(1L, 100L, 1L, 5L, GameStatus.IN_PROGRESS);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));

        assertThrows(InvalidStateTransitionException.class, () -> orchestratorService.readyGame(1L));
    }

    @Test
    void processAction_correctAction_calls_tracking() {
        GameState storedState = createRealGameState(1L, 100L, 1L, 5L, GameStatus.IN_PROGRESS);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));
        when(registerActivityAttemptUseCase.register(anyLong(), anyLong(), anyLong(), isNull(), anyLong(), any(), any(), any()))
            .thenReturn(new AttemptRegistrationResult(1L, LocalDateTime.now(), List.of()));

        ActionProcessingResult result = orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);

        assertEquals(es.vargontoc.educational.framework.game.model.ActionResultType.CORRECT, result.resultType());
        assertNotNull(result.updatedState());
        verify(registerActivityAttemptUseCase).register(anyLong(), anyLong(), anyLong(), isNull(), anyLong(), any(), any(), any());
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    void processAction_completedAction_calls_game_completion_and_summary() {
        GameState storedState = createRealGameState(1L, 100L, 1L, 5L, GameStatus.IN_PROGRESS);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));
        doAnswer(invocation -> null).when(gameStateRegistry).remove(anyLong());
        when(registerActivityAttemptUseCase.register(anyLong(), anyLong(), anyLong(), isNull(), anyLong(), any(), any(), any()))
            .thenReturn(new AttemptRegistrationResult(1L, LocalDateTime.now(), List.of()));
        when(evaluateGameCompletionAchievementsUseCase.evaluate(anyLong(), anyLong(), isNull()))
            .thenReturn(List.of());

        orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);
        orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);
        ActionProcessingResult finalResult = orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);

        assertTrue(finalResult.gameCompleted());
        verify(evaluateGameCompletionAchievementsUseCase).evaluate(anyLong(), anyLong(), isNull());
        verify(registerGameSessionSummaryUseCase).registerGameSessionSummary(
            anyLong(), anyLong(), anyLong(), anyLong(), anyLong(),
            any(), any(), any(), any(), any(), any(), any()
        );
        verify(gameStateRegistry).remove(anyLong());
    }

    @Test
    void processAction_gameNotInProgress_throwsException() {
        GameState storedState = createRealGameState(1L, 100L, 1L, 5L, GameStatus.WAITING);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));

        assertThrows(InvalidStateTransitionException.class,
            () -> orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000));
    }

    @Test
    void abandonGame_removesFromRegistryAndRegistersSummary() {
        GameState storedState = createRealGameState(1L, 100L, 1L, 5L, GameStatus.IN_PROGRESS);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));

        GameState result = orchestratorService.abandonGame(1L);

        assertEquals(GameStatus.ABANDONED, result.getStatus());
        verify(gameStateRegistry).remove(1L);
        verify(registerGameSessionSummaryUseCase).registerGameSessionSummary(
            eq(100L), eq(1L), eq(1L), eq(5L), eq(5L), anyInt(), anyInt(), anyInt(), anyInt(),
            any(), any(), eq(es.vargontoc.educational.framework.tracking.model.GameSessionFinalStatus.ABANDONED)
        );
    }

    @Test
    void abandonGame_alreadyCompleted_throwsException() {
        GameState storedState = createRealGameState(1L, 100L, 1L, 5L, GameStatus.COMPLETED);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));

        assertThrows(InvalidStateTransitionException.class,
            () -> orchestratorService.abandonGame(1L));
    }

    @Test
    void readyGame_notDevProfile_throwsEngineNotAvailable() {
        orchestratorService = new GameOrchestratorService(
            gameCatalogUseCase,
            gameStateRegistry,
            registerActivityAttemptUseCase,
            evaluateGameCompletionAchievementsUseCase,
            registerGameSessionSummaryUseCase,
            eventPublisher,
            topicUseCase,
            filterAllowedRecognitionCategoriesUseCase
        );

        GameState storedState = createRealGameState(1L, 100L, 1L, 5L, GameStatus.WAITING);
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
    void processAction_unlocked_achievements_returned() {
        GameState storedState = createRealGameState(1L, 100L, 1L, 5L, GameStatus.IN_PROGRESS);

        UnlockedAchievement achievement = new UnlockedAchievement("FIRST_CORRECT_STREAK", 1L, null);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));
        when(registerActivityAttemptUseCase.register(anyLong(), anyLong(), anyLong(), isNull(), anyLong(), any(), any(), any()))
            .thenReturn(new AttemptRegistrationResult(1L, LocalDateTime.now(), List.of(achievement)));

        ActionProcessingResult result = orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);

        assertFalse(result.unlockedAchievements().isEmpty());
        assertEquals("FIRST_CORRECT_STREAK", result.unlockedAchievements().get(0).achievementCode());
    }

    @Test
    void processAction_difficulty_change_updates_state() {
        GameState storedState = createRealGameState(1L, 100L, 1L, 5L, GameStatus.IN_PROGRESS);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));
        when(registerActivityAttemptUseCase.register(anyLong(), anyLong(), anyLong(), isNull(), anyLong(), any(), any(), any()))
            .thenReturn(new AttemptRegistrationResult(1L, LocalDateTime.now(), List.of(), true, 10L));

        ActionProcessingResult result = orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);

        assertTrue(result.difficultyChanged());
        assertEquals(10L, result.newDifficultyLevelId());
        assertEquals(10L, result.updatedState().getDifficultyLevelId());
    }

    @Test
    void abandonGameForSession_withActiveGame_abandonsAndRegistersSummary() {
        GameState storedState = createRealGameState(1L, 100L, 1L, 5L, GameStatus.IN_PROGRESS);

        when(gameStateRegistry.findByChildSessionId(100L)).thenReturn(Optional.of(storedState));
        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));

        orchestratorService.abandonGameForSession(100L);

        assertTrue(storedState.isSystemEventPending());
        assertEquals(GameStatus.ABANDONED, storedState.getStatus());
        verify(gameStateRegistry).remove(1L);
        verify(registerGameSessionSummaryUseCase).registerGameSessionSummary(
            eq(100L), eq(1L), eq(1L), eq(5L), eq(5L), anyInt(), anyInt(), anyInt(), anyInt(),
            any(), any(), eq(es.vargontoc.educational.framework.tracking.model.GameSessionFinalStatus.ABANDONED)
        );
    }

    @Test
    void abandonGameForSession_withoutActiveGame_doesNothing() {
        when(gameStateRegistry.findByChildSessionId(100L)).thenReturn(Optional.empty());

        orchestratorService.abandonGameForSession(100L);

        verify(gameStateRegistry, never()).remove(anyLong());
        verify(registerGameSessionSummaryUseCase, never()).registerGameSessionSummary(
            anyLong(), anyLong(), anyLong(), anyLong(), anyLong(), anyInt(), anyInt(), anyInt(), anyInt(),
            any(), any(), any()
        );
    }

    @Test
    void processAction_withSystemEventPending_discardsActionAndDoesNotRegisterAttempt() {
        GameState storedState = createRealGameState(1L, 100L, 1L, 5L, GameStatus.IN_PROGRESS);
        storedState.setSystemEventPending(true);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));

        ActionProcessingResult result = orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);

        assertEquals(ActionResultType.CORRECT, result.resultType());
        assertEquals("discarded_system_event_pending", result.attemptContext());
        verify(registerActivityAttemptUseCase, never()).register(
            anyLong(), anyLong(), anyLong(), any(), anyLong(), any(), any(), any()
        );
    }

    @Test
    void processAction_trackingFails_continuesWithoutTracking() {
        GameState storedState = createRealGameState(1L, 100L, 1L, 5L, GameStatus.IN_PROGRESS);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));
        when(registerActivityAttemptUseCase.register(anyLong(), anyLong(), anyLong(), any(), anyLong(), any(), any(), any()))
            .thenThrow(new RuntimeException("Tracking service unavailable"));

        ActionProcessingResult result = orchestratorService.processAction(1L, "{\"selectedOptionId\":\"elem-1\",\"responseTimeMs\":2000}", null, 2000);

        assertEquals(ActionResultType.CORRECT, result.resultType());
        assertTrue(result.unlockedAchievements().isEmpty());
    }

    @Test
    void abandonGameForSession_trackingFails_stillAbandonsGame() {
        GameState storedState = createRealGameState(1L, 100L, 1L, 5L, GameStatus.IN_PROGRESS);

        when(gameStateRegistry.findByChildSessionId(100L)).thenReturn(Optional.of(storedState));
        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));
        doThrow(new RuntimeException("Tracking unavailable"))
            .when(registerGameSessionSummaryUseCase).registerGameSessionSummary(
                anyLong(), anyLong(), anyLong(), anyLong(), anyLong(), anyInt(), anyInt(), anyInt(), anyInt(),
                any(), any(), any()
            );

        orchestratorService.abandonGameForSession(100L);

        assertEquals(GameStatus.ABANDONED, storedState.getStatus());
        verify(gameStateRegistry).remove(1L);
    }

    @Test
    void processAction_withDifferentGameIds_runConcurrently() {
        GameState state1 = createRealGameState(1L, 100L, 1L, 5L, GameStatus.IN_PROGRESS);
        GameState state2 = createRealGameState(2L, 101L, 1L, 5L, GameStatus.IN_PROGRESS);

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
        GameState storedState = createRealGameState(1L, 100L, 1L, 5L, GameStatus.IN_PROGRESS);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));
        doAnswer(invocation -> null).when(gameStateRegistry).remove(anyLong());
        when(registerActivityAttemptUseCase.register(anyLong(), anyLong(), anyLong(), isNull(), anyLong(), any(), any(), any()))
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
        GameState storedState = createRealGameState(1L, 100L, 1L, 5L, GameStatus.IN_PROGRESS);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));

        orchestratorService.abandonGame(1L);

        verify(eventPublisher).publishEvent(any(GameSessionCompletedEvent.class));
    }

    @Test
    void abandonGameForSession_publishesGameSessionCompletedEvent() {
        GameState storedState = createRealGameState(1L, 100L, 1L, 5L, GameStatus.IN_PROGRESS);

        when(gameStateRegistry.findByChildSessionId(100L)).thenReturn(Optional.of(storedState));
        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));

        orchestratorService.abandonGameForSession(100L);

        verify(eventPublisher).publishEvent(any(GameSessionCompletedEvent.class));
    }
}
