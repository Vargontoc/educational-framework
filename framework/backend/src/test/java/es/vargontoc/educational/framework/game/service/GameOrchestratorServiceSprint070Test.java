package es.vargontoc.educational.framework.game.service;

import es.vargontoc.educational.framework.content.model.Activity;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.DifficultyCode;
import es.vargontoc.educational.framework.content.model.DifficultyLevel;
import es.vargontoc.educational.framework.content.model.GameCatalogReadiness;
import es.vargontoc.educational.framework.content.model.RecognitionType;
import es.vargontoc.educational.framework.content.model.Topic;
import es.vargontoc.educational.framework.content.ports.in.GameCatalogUseCase;
import es.vargontoc.educational.framework.content.ports.in.TopicUseCase;
import es.vargontoc.educational.framework.game.model.ActionProcessingResult;
import es.vargontoc.educational.framework.game.model.ActionResultType;
import es.vargontoc.educational.framework.game.model.GameState;
import es.vargontoc.educational.framework.game.model.GameStatus;
import es.vargontoc.educational.framework.game.model.enums.EngineType;
import es.vargontoc.educational.framework.game.model.recognition.RecognitionState;
import es.vargontoc.educational.framework.game.ports.out.GameStateRegistry;
import es.vargontoc.educational.framework.game.ports.out.SessionAntiRepetitionRegistry;
import es.vargontoc.educational.framework.tracking.model.AttemptRegistrationResult;
import es.vargontoc.educational.framework.tracking.model.RecognitionCategory;
import es.vargontoc.educational.framework.tracking.ports.in.EvaluateGameCompletionAchievementsUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.FilterAllowedRecognitionCategoriesUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.RegisterActivityAttemptUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.RegisterGameSessionSummaryUseCase;
import es.vargontoc.educational.framework.tracking.ports.out.ElementProgressPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameOrchestratorServiceSprint070Test {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
            org.mockito.Mockito.mock(ElementProgressPort.class)
        );
    }

    private Activity createActivity(Long id, List<Long> topicIds) {
        Activity activity = new Activity();
        activity.setId(id);
        activity.setName("Test Activity");
        activity.setStatus(ContentStatus.ACTIVE);
        activity.setGameEngineType(EngineType.RECOGNITION.name());
        activity.setTopicIds(topicIds);
        return activity;
    }

    private DifficultyLevel createDifficultyLevel(Long id) {
        DifficultyLevel dl = new DifficultyLevel();
        dl.setId(id);
        dl.setDifficultyCode(DifficultyCode.EASY);
        dl.setEngineParams("{\"speed\":1}");
        return dl;
    }

    private Topic createTopic(Long id, RecognitionType recognitionType) {
        Topic topic = new Topic();
        topic.setId(id);
        topic.setName("Topic " + id);
        topic.setRecognitionType(recognitionType);
        return topic;
    }

    private GameState createInProgressState(Long gameId, Long childSessionId, Long activityId,
                                             Long difficultyLevelId, String enginePayload) {
        GameState state = new GameState();
        state.setGameId(gameId);
        state.setChildSessionId(childSessionId);
        state.setActivityId(activityId);
        state.setDifficultyLevelId(difficultyLevelId);
        state.setEngine(EngineType.RECOGNITION);
        state.setStatus(GameStatus.IN_PROGRESS);
        state.setSequenceNumber(0);
        state.setAttempts(0);
        state.setCorrectAttempts(0);
        state.setIncorrectAttempts(0);
        state.setTimeoutAttempts(0);
        state.setEnginePayload(enginePayload);
        state.setCandidates(List.of("10", "11", "12", "13"));
        return state;
    }

    private String buildRecognitionPayload(String targetId, List<String> optionIds,
                                            List<String> candidateIds, int roundIndex,
                                            int currentDifficultyLevel, Integer pendingDifficultyLevel) {
        try {
            RecognitionState rs = new RecognitionState();
            rs.setRoundIndex(roundIndex);
            rs.setTotalRounds(5);
            rs.setTargetElementId(targetId);
            rs.setOptionIds(optionIds);
            rs.setCandidateElementIds(candidateIds);
            rs.setCurrentDifficultyLevel(currentDifficultyLevel);
            rs.setPendingDifficultyLevel(pendingDifficultyLevel);
            rs.setRoundsShownElementIds(new java.util.ArrayList<>());
            rs.setCurrentRoundAttemptCount(0);
            rs.setCurrentRoundConsecutiveFailures(0);
            rs.setTotalIncorrectAttempts(0);
            rs.setTotalCorrectFirstTry(0);
            rs.setHintActive(false);
            rs.setTotalResponseTimeMs(0L);
            rs.setRoundStartedAt(LocalDateTime.now());
            return OBJECT_MAPPER.writeValueAsString(rs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void startGame_antiRepetition_excludesRecentElementsWhenAlternativesExist() {
        Activity activity = createActivity(1L, List.of(10L));
        DifficultyLevel difficultyLevel = createDifficultyLevel(5L);
        GameCatalogReadiness readiness = new GameCatalogReadiness(activity, difficultyLevel, true);

        Topic topic10 = createTopic(10L, RecognitionType.LETTER);
        Topic topic11 = createTopic(11L, RecognitionType.LETTER);
        Topic topic12 = createTopic(12L, RecognitionType.LETTER);
        Topic topic13 = createTopic(13L, RecognitionType.LETTER);

        when(gameCatalogUseCase.getGameReadiness(100L, 1L)).thenReturn(readiness);
        when(topicUseCase.getTopic(10L)).thenReturn(topic10);
        when(filterAllowedRecognitionCategoriesUseCase.filterAllowedCategories(
                eq(100L), ArgumentMatchers.<List<RecognitionCategory>>any()))
                .thenReturn(List.of(RecognitionCategory.LETTER));
        when(topicUseCase.listTopicsByRecognitionType(RecognitionType.LETTER))
                .thenReturn(List.of(topic10, topic11, topic12, topic13));
        when(sessionAntiRepetitionRegistry.getRecentElements(100L, 10L))
                .thenReturn(List.of("10", "11"));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));

        GameState result = orchestratorService.startGame(100L, 1L);

        assertNotNull(result);
        assertNotNull(result.getCandidates());
        assertEquals(2, result.getCandidates().size());
        assertTrue(result.getCandidates().contains("12"));
        assertTrue(result.getCandidates().contains("13"));
        assertFalse(result.getCandidates().contains("10"));
        assertFalse(result.getCandidates().contains("11"));
    }

    @Test
    void startGame_antiRepetition_fallbackWhenAllCandidatesAreRecent() {
        Activity activity = createActivity(1L, List.of(10L));
        DifficultyLevel difficultyLevel = createDifficultyLevel(5L);
        GameCatalogReadiness readiness = new GameCatalogReadiness(activity, difficultyLevel, true);

        Topic topic10 = createTopic(10L, RecognitionType.LETTER);
        Topic topic11 = createTopic(11L, RecognitionType.LETTER);

        when(gameCatalogUseCase.getGameReadiness(100L, 1L)).thenReturn(readiness);
        when(topicUseCase.getTopic(10L)).thenReturn(topic10);
        when(filterAllowedRecognitionCategoriesUseCase.filterAllowedCategories(
                eq(100L), ArgumentMatchers.<List<RecognitionCategory>>any()))
                .thenReturn(List.of(RecognitionCategory.LETTER));
        when(topicUseCase.listTopicsByRecognitionType(RecognitionType.LETTER))
                .thenReturn(List.of(topic10, topic11));
        when(sessionAntiRepetitionRegistry.getRecentElements(100L, 10L))
                .thenReturn(List.of("10", "11"));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));

        GameState result = orchestratorService.startGame(100L, 1L);

        assertNotNull(result);
        assertNotNull(result.getCandidates());
        assertEquals(2, result.getCandidates().size());
        assertTrue(result.getCandidates().contains("10"));
        assertTrue(result.getCandidates().contains("11"));
    }

    @Test
    void processAction_correctAnswer_registersTargetAsRecent() {
        String payload = buildRecognitionPayload("10", List.of("10", "11", "12"),
                List.of("10", "11", "12", "13"), 0, 1, null);
        GameState storedState = createInProgressState(1L, 100L, 1L, 5L, payload);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));
        when(registerActivityAttemptUseCase.register(anyLong(), anyLong(), anyLong(), any(), any(), anyLong(), any(), any(), any()))
                .thenReturn(new AttemptRegistrationResult(1L, LocalDateTime.now(), List.of()));

        ActionProcessingResult result = orchestratorService.processAction(
                1L, "{\"selectedOptionId\":\"10\",\"responseTimeMs\":2000}", 10L, 2000);

        assertEquals(ActionResultType.CORRECT, result.resultType());
        verify(sessionAntiRepetitionRegistry).registerRecentElement(100L, 10L, "10");
    }

    @Test
    void processAction_incorrectAnswer_doesNotRegisterTarget() {
        String payload = buildRecognitionPayload("10", List.of("10", "11", "12"),
                List.of("10", "11", "12", "13"), 0, 1, null);
        GameState storedState = createInProgressState(1L, 100L, 1L, 5L, payload);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));
        when(registerActivityAttemptUseCase.register(anyLong(), anyLong(), anyLong(), any(), any(), anyLong(), any(), any(), any()))
                .thenReturn(new AttemptRegistrationResult(1L, LocalDateTime.now(), List.of()));

        ActionProcessingResult result = orchestratorService.processAction(
                1L, "{\"selectedOptionId\":\"11\",\"responseTimeMs\":2000}", 10L, 2000);

        assertEquals(ActionResultType.INCORRECT, result.resultType());
        verify(sessionAntiRepetitionRegistry, never()).registerRecentElement(anyLong(), anyLong(), any());
    }

    @Test
    void processAction_difficultyChangeDuringRetry_doesNotApplyImmediately() {
        String payload = buildRecognitionPayload("10", List.of("10", "11", "12"),
                List.of("10", "11", "12", "13"), 0, 1, null);
        GameState storedState = createInProgressState(1L, 100L, 1L, 5L, payload);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));
        when(registerActivityAttemptUseCase.register(anyLong(), anyLong(), anyLong(), any(), any(), anyLong(), any(), any(), any()))
                .thenReturn(new AttemptRegistrationResult(1L, LocalDateTime.now(), List.of(), true, 10L));

        ActionProcessingResult result = orchestratorService.processAction(
                1L, "{\"selectedOptionId\":\"11\",\"responseTimeMs\":2000}", 10L, 2000);

        assertEquals(ActionResultType.INCORRECT, result.resultType());
        assertTrue(result.difficultyChanged());
        assertEquals(5L, result.updatedState().getDifficultyLevelId());

        RecognitionState recState = deserializeRecognitionState(result.updatedState().getEnginePayload());
        assertEquals(10, recState.getPendingDifficultyLevel());
        assertEquals(1, recState.getCurrentDifficultyLevel());
    }

    @Test
    void processAction_pendingDifficulty_promotedAfterCorrectAnswer() {
        String payload = buildRecognitionPayload("10", List.of("10", "11", "12"),
                List.of("10", "11", "12", "13"), 0, 1, 10);
        GameState storedState = createInProgressState(1L, 100L, 1L, 5L, payload);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));
        when(registerActivityAttemptUseCase.register(anyLong(), anyLong(), anyLong(), any(), any(), anyLong(), any(), any(), any()))
                .thenReturn(new AttemptRegistrationResult(1L, LocalDateTime.now(), List.of()));

        ActionProcessingResult result = orchestratorService.processAction(
                1L, "{\"selectedOptionId\":\"10\",\"responseTimeMs\":2000}", 10L, 2000);

        assertEquals(ActionResultType.CORRECT, result.resultType());
        assertEquals(10L, result.updatedState().getDifficultyLevelId());

        RecognitionState recState = deserializeRecognitionState(result.updatedState().getEnginePayload());
        assertEquals(10, recState.getCurrentDifficultyLevel());
        assertNull(recState.getPendingDifficultyLevel());
    }

    @Test
    void processAction_noPendingDifficulty_correctAnswer_doesNotChangeDifficulty() {
        String payload = buildRecognitionPayload("10", List.of("10", "11", "12"),
                List.of("10", "11", "12", "13"), 0, 1, null);
        GameState storedState = createInProgressState(1L, 100L, 1L, 5L, payload);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));
        when(registerActivityAttemptUseCase.register(anyLong(), anyLong(), anyLong(), any(), any(), anyLong(), any(), any(), any()))
                .thenReturn(new AttemptRegistrationResult(1L, LocalDateTime.now(), List.of()));

        ActionProcessingResult result = orchestratorService.processAction(
                1L, "{\"selectedOptionId\":\"10\",\"responseTimeMs\":2000}", 10L, 2000);

        assertEquals(ActionResultType.CORRECT, result.resultType());
        assertEquals(5L, result.updatedState().getDifficultyLevelId());

        RecognitionState recState = deserializeRecognitionState(result.updatedState().getEnginePayload());
        assertEquals(1, recState.getCurrentDifficultyLevel());
        assertNull(recState.getPendingDifficultyLevel());
    }

    @Test
    void processAction_difficultyChangeAndCorrect_promotesImmediately() {
        String payload = buildRecognitionPayload("10", List.of("10", "11", "12"),
                List.of("10", "11", "12", "13"), 0, 1, null);
        GameState storedState = createInProgressState(1L, 100L, 1L, 5L, payload);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));
        when(registerActivityAttemptUseCase.register(anyLong(), anyLong(), anyLong(), any(), any(), anyLong(), any(), any(), any()))
                .thenReturn(new AttemptRegistrationResult(1L, LocalDateTime.now(), List.of(), true, 10L));

        ActionProcessingResult result = orchestratorService.processAction(
                1L, "{\"selectedOptionId\":\"10\",\"responseTimeMs\":2000}", 10L, 2000);

        assertEquals(ActionResultType.CORRECT, result.resultType());
        assertTrue(result.difficultyChanged());
        assertEquals(10L, result.updatedState().getDifficultyLevelId());

        RecognitionState recState = deserializeRecognitionState(result.updatedState().getEnginePayload());
        assertEquals(10, recState.getCurrentDifficultyLevel());
        assertNull(recState.getPendingDifficultyLevel());
    }

    @Test
    void clearSessionData_clearsAntiRepetitionRegistry() {
        orchestratorService.clearSessionData(100L);

        verify(sessionAntiRepetitionRegistry).clearSession(100L);
    }

    private RecognitionState deserializeRecognitionState(String payload) {
        try {
            return OBJECT_MAPPER.readValue(payload, RecognitionState.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
