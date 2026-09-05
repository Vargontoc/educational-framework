package es.vargontoc.educational.framework.game.service;

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
import es.vargontoc.educational.framework.tracking.ports.in.EvaluateGameCompletionAchievementsUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.FilterAllowedRecognitionCategoriesUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.RegisterActivityAttemptUseCase;
import es.vargontoc.educational.framework.tracking.ports.in.RegisterGameSessionSummaryUseCase;
import es.vargontoc.educational.framework.tracking.ports.out.ElementProgressPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameOrchestratorServiceTrackingIntegrationTest {

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

    @Mock
    private ElementProgressPort elementProgressPort;

    @Mock
    private es.vargontoc.educational.framework.content.ports.out.RecognitionElementRepository recognitionElementRepository;

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
            recognitionElementRepository
        );
    }

    private GameState createInProgressState(Long gameId, Long childSessionId, Long activityId,
                                             Long difficultyLevelId, String enginePayload) {
        GameState state = new GameState();
        state.setGameId(gameId);
        state.setChildSessionId(childSessionId);
        state.setChildProfileId(200L);
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
                                            int currentRoundAttemptCount,
                                            int currentRoundConsecutiveFailures,
                                            boolean hintActive, Integer hintTriggeredAtAttempt) {
        try {
            RecognitionState rs = new RecognitionState();
            rs.setRoundIndex(roundIndex);
            rs.setTotalRounds(5);
            rs.setTargetElementId(targetId);
            rs.setOptionIds(optionIds);
            rs.setCandidateElementIds(candidateIds);
            rs.setCurrentDifficultyLevel(1);
            rs.setPendingDifficultyLevel(null);
            rs.setRoundsShownElementIds(new java.util.ArrayList<>());
            rs.setCurrentRoundAttemptCount(currentRoundAttemptCount);
            rs.setCurrentRoundConsecutiveFailures(currentRoundConsecutiveFailures);
            rs.setTotalIncorrectAttempts(0);
            rs.setTotalCorrectFirstTry(0);
            rs.setHintActive(hintActive);
            rs.setHintTriggeredAtAttempt(hintTriggeredAtAttempt);
            rs.setTotalResponseTimeMs(0L);
            rs.setRoundStartedAt(LocalDateTime.now());
            return OBJECT_MAPPER.writeValueAsString(rs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void incorrectAttempt_trackingReceivesContextWithAllFields() {
        String payload = buildRecognitionPayload("10", List.of("10", "11", "12"),
                List.of("10", "11", "12", "13"), 0, 0, 0, false, null);
        GameState storedState = createInProgressState(1L, 100L, 1L, 5L, payload);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));
        when(registerActivityAttemptUseCase.register(anyLong(), anyLong(), anyLong(), any(), any(), anyLong(), any(), any(), any()))
                .thenReturn(new AttemptRegistrationResult(1L, LocalDateTime.now(), List.of()));

        ActionProcessingResult result = orchestratorService.processAction(
                1L, "{\"selectedOptionId\":\"11\",\"responseTimeMs\":2000}", 10L, 2000);

        assertEquals(ActionResultType.INCORRECT, result.resultType());
        assertNotNull(result.attemptContext());

        ArgumentCaptor<String> contextCaptor = ArgumentCaptor.forClass(String.class);
        verify(registerActivityAttemptUseCase).register(
                eq(200L), eq(1L), eq(100L), eq(10L), any(), eq(5L),
                any(), eq(2000), contextCaptor.capture());

        String capturedContext = contextCaptor.getValue();
        assertNotNull(capturedContext);

        try {
            JsonNode ctx = OBJECT_MAPPER.readTree(capturedContext);
            assertEquals("RECOGNITION", ctx.get("engineType").asString());
            assertEquals(0, ctx.get("roundIndex").asInt());
            assertEquals("10", ctx.get("targetElementId").asString());
            assertEquals("11", ctx.get("selectedOptionId").asString());
            assertTrue(ctx.get("optionIds").isArray());
            assertTrue(ctx.get("firstTry").asBoolean());
            assertFalse(ctx.get("hintActive").asBoolean());
            assertFalse(ctx.get("hintTriggeredBeforeAnswer").asBoolean());
            assertEquals(1, ctx.get("attemptNumberInRound").asInt());
            assertEquals(2000, ctx.get("responseTimeMs").asLong());
            assertFalse(ctx.has("topicId"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void firstCorrectAttempt_trackingReceivesIsFirstTryTrue() {
        String payload = buildRecognitionPayload("10", List.of("10", "11", "12"),
                List.of("10", "11", "12", "13"), 0, 0, 0, false, null);
        GameState storedState = createInProgressState(1L, 100L, 1L, 5L, payload);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));
        when(registerActivityAttemptUseCase.register(anyLong(), anyLong(), anyLong(), any(), any(), anyLong(), any(), any(), any()))
                .thenReturn(new AttemptRegistrationResult(1L, LocalDateTime.now(), List.of()));

        ActionProcessingResult result = orchestratorService.processAction(
                1L, "{\"selectedOptionId\":\"10\",\"responseTimeMs\":1500}", 10L, 1500);

        assertEquals(ActionResultType.CORRECT, result.resultType());

        ArgumentCaptor<String> contextCaptor = ArgumentCaptor.forClass(String.class);
        verify(registerActivityAttemptUseCase).register(
                eq(200L), eq(1L), eq(100L), eq(10L), any(), eq(5L),
                any(), eq(1500), contextCaptor.capture());

        try {
            JsonNode ctx = OBJECT_MAPPER.readTree(contextCaptor.getValue());
            assertTrue(ctx.get("firstTry").asBoolean());
            assertEquals(1, ctx.get("attemptNumberInRound").asInt());
            assertEquals(0, ctx.get("roundIndex").asInt());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void retryCorrectAttempt_trackingReceivesIsFirstTryFalse() {
        String payload = buildRecognitionPayload("10", List.of("10", "11", "12"),
                List.of("10", "11", "12", "13"), 0, 1, 1, false, null);
        GameState storedState = createInProgressState(1L, 100L, 1L, 5L, payload);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));
        when(registerActivityAttemptUseCase.register(anyLong(), anyLong(), anyLong(), any(), any(), anyLong(), any(), any(), any()))
                .thenReturn(new AttemptRegistrationResult(1L, LocalDateTime.now(), List.of()));

        ActionProcessingResult result = orchestratorService.processAction(
                1L, "{\"selectedOptionId\":\"10\",\"responseTimeMs\":3000}", 10L, 3000);

        assertEquals(ActionResultType.CORRECT, result.resultType());

        ArgumentCaptor<String> contextCaptor = ArgumentCaptor.forClass(String.class);
        verify(registerActivityAttemptUseCase).register(
                eq(200L), eq(1L), eq(100L), eq(10L), any(), eq(5L),
                any(), eq(3000), contextCaptor.capture());

        try {
            JsonNode ctx = OBJECT_MAPPER.readTree(contextCaptor.getValue());
            assertFalse(ctx.get("firstTry").asBoolean());
            assertEquals(2, ctx.get("attemptNumberInRound").asInt());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void afterHintActivation_trackingReceivesCorrectHintFlags() {
        String payload = buildRecognitionPayload("10", List.of("10", "11", "12"),
                List.of("10", "11", "12", "13"), 0, 2, 2, true, 2);
        GameState storedState = createInProgressState(1L, 100L, 1L, 5L, payload);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));
        when(registerActivityAttemptUseCase.register(anyLong(), anyLong(), anyLong(), any(), any(), anyLong(), any(), any(), any()))
                .thenReturn(new AttemptRegistrationResult(1L, LocalDateTime.now(), List.of()));

        ActionProcessingResult result = orchestratorService.processAction(
                1L, "{\"selectedOptionId\":\"11\",\"responseTimeMs\":2000}", 10L, 2000);

        assertEquals(ActionResultType.INCORRECT, result.resultType());

        ArgumentCaptor<String> contextCaptor = ArgumentCaptor.forClass(String.class);
        verify(registerActivityAttemptUseCase).register(
                eq(200L), eq(1L), eq(100L), eq(10L), any(), eq(5L),
                any(), eq(2000), contextCaptor.capture());

        try {
            JsonNode ctx = OBJECT_MAPPER.readTree(contextCaptor.getValue());
            assertTrue(ctx.get("hintActive").asBoolean());
            assertTrue(ctx.get("hintTriggeredBeforeAnswer").asBoolean());
            assertEquals(3, ctx.get("attemptNumberInRound").asInt());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void gameSessionSummary_doesNotContainAttemptDetails() {
        String payload = buildRecognitionPayload("10", List.of("10", "11", "12"),
                List.of("10", "11", "12", "13"), 4, 0, 0, false, null);
        GameState storedState = createInProgressState(1L, 100L, 1L, 5L, payload);
        storedState.setStartedAt(LocalDateTime.now().minusMinutes(5));
        storedState.setCurrentScore(java.math.BigDecimal.ZERO);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));
        doAnswer(invocation -> null).when(gameStateRegistry).remove(anyLong());
        when(registerActivityAttemptUseCase.register(anyLong(), anyLong(), anyLong(), any(), any(), anyLong(), any(), any(), any()))
                .thenReturn(new AttemptRegistrationResult(1L, LocalDateTime.now(), List.of()));
        when(evaluateGameCompletionAchievementsUseCase.evaluate(anyLong(), anyLong(), any()))
                .thenReturn(List.of());

        orchestratorService.processAction(
                1L, "{\"selectedOptionId\":\"10\",\"responseTimeMs\":1000}", 10L, 1000);

        ArgumentCaptor<Integer> scoreCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> attemptsCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> correctCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> timeoutCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(registerGameSessionSummaryUseCase).registerGameSessionSummary(
                eq(200L), eq(100L), eq(1L), eq(5L), eq(5L),
                scoreCaptor.capture(), attemptsCaptor.capture(),
                correctCaptor.capture(), timeoutCaptor.capture(),
                any(), any(), any());

        assertEquals(0, scoreCaptor.getValue());
        assertEquals(0, attemptsCaptor.getValue());
        assertEquals(0, correctCaptor.getValue());
        assertEquals(0, timeoutCaptor.getValue());
    }

    @Test
    void trackingContext_attemptContextIsPassedThrough() {
        String payload = buildRecognitionPayload("10", List.of("10", "11", "12"),
                List.of("10", "11", "12", "13"), 0, 0, 0, false, null);
        GameState storedState = createInProgressState(1L, 100L, 1L, 5L, payload);

        when(gameStateRegistry.findByGameId(1L)).thenReturn(Optional.of(storedState));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));
        when(registerActivityAttemptUseCase.register(anyLong(), anyLong(), anyLong(), any(), any(), anyLong(), any(), any(), any()))
                .thenReturn(new AttemptRegistrationResult(1L, LocalDateTime.now(), List.of()));

        orchestratorService.processAction(
                1L, "{\"selectedOptionId\":\"11\",\"responseTimeMs\":2000}", 10L, 2000);

        verify(registerActivityAttemptUseCase).register(
                eq(200L), eq(1L), eq(100L), eq(10L), any(), eq(5L),
                any(), eq(2000), any(String.class));
    }
}
