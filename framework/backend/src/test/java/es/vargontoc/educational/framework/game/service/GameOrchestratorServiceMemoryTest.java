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
import es.vargontoc.educational.framework.game.application.RecognitionProperties;
import es.vargontoc.educational.framework.game.model.ActionProcessingResult;
import es.vargontoc.educational.framework.game.model.ActionResultType;
import es.vargontoc.educational.framework.game.model.GameState;
import es.vargontoc.educational.framework.game.model.GameStatus;
import es.vargontoc.educational.framework.game.model.enums.EngineType;
import es.vargontoc.educational.framework.game.model.memory.MemoryCard;
import es.vargontoc.educational.framework.game.model.memory.MemoryState;
import es.vargontoc.educational.framework.game.model.recognition.RecognitionDifficultyConfig;
import es.vargontoc.educational.framework.tracking.model.AttemptRegistrationResult;
import es.vargontoc.educational.framework.tracking.model.AttemptResult;
import es.vargontoc.educational.framework.tracking.model.GameSessionFinalStatus;
import es.vargontoc.educational.framework.tracking.model.UnlockedAchievement;
import org.mockito.ArgumentCaptor;
import es.vargontoc.educational.framework.game.ports.out.GameStateRegistry;
import es.vargontoc.educational.framework.game.ports.out.SessionAntiRepetitionRegistry;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SPRINT-109: full flow of a MEMORY activity through the orchestrator and the real MemoryEngine.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GameOrchestratorServiceMemoryTest {

    private static final Long CHILD_PROFILE_ID = 200L;
    private static final Long ACTIVITY_ID = 11L;
    private static final Long TOPIC_ID = 31L;
    private static final Long DIFFICULTY_LEVEL_ID = 6L;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Mock private GameCatalogUseCase gameCatalogUseCase;
    @Mock private GameStateRegistry gameStateRegistry;
    @Mock private SessionAntiRepetitionRegistry sessionAntiRepetitionRegistry;
    @Mock private RegisterActivityAttemptUseCase registerActivityAttemptUseCase;
    @Mock private EvaluateGameCompletionAchievementsUseCase evaluateGameCompletionAchievementsUseCase;
    @Mock private RegisterGameSessionSummaryUseCase registerGameSessionSummaryUseCase;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private TopicUseCase topicUseCase;
    @Mock private FilterAllowedRecognitionCategoriesUseCase filterAllowedRecognitionCategoriesUseCase;
    @Mock private ElementProgressPort elementProgressPort;
    @Mock private RecognitionElementRepository recognitionElementRepository;
    @Mock private DifficultyLevelUseCase difficultyLevelUseCase;
    @Mock private ChildProfileUseCase childProfileUseCase;
    @Mock private RecognitionSimilarityService recognitionSimilarityService;
    @Mock private RoundAudioService roundAudioService;

    private GameOrchestratorService orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new GameOrchestratorService(
                gameCatalogUseCase, gameStateRegistry, sessionAntiRepetitionRegistry,
                registerActivityAttemptUseCase, evaluateGameCompletionAchievementsUseCase,
                registerGameSessionSummaryUseCase, eventPublisher, topicUseCase,
                filterAllowedRecognitionCategoriesUseCase, elementProgressPort, recognitionElementRepository,
                difficultyLevelUseCase, childProfileUseCase,
                new RecognitionDifficultyService(new RecognitionDifficultyConfig(new RecognitionProperties())),
                recognitionSimilarityService, roundAudioService, null, null);

        // Ten elements in two groups of five: ids 1..5 fruit, 6..10 vehicle.
        List<RecognitionElement> elements = new ArrayList<>();
        for (long id = 1; id <= 10; id++) {
            RecognitionElement element = new RecognitionElement();
            element.setId(id);
            element.setTopicId(TOPIC_ID);
            element.setCode("item" + id);
            element.setSimilarityGroup(id <= 5 ? "fruit" : "vehicle");
            element.setStatus(ContentStatus.ACTIVE);
            elements.add(element);
        }
        when(recognitionElementRepository.findByTopicIdAndStatus(TOPIC_ID, ContentStatus.ACTIVE)).thenReturn(elements);
        when(recognitionElementRepository.findAllById(any())).thenAnswer(invocation -> {
            Iterable<Long> ids = invocation.getArgument(0);
            List<RecognitionElement> found = new ArrayList<>();
            ids.forEach(id -> found.add(elements.get((int) (id - 1))));
            return found;
        });
    }

    private GameState start(DifficultyCode difficulty) {
        Activity activity = new Activity();
        activity.setId(ACTIVITY_ID);
        activity.setName("Memoria de Parejas");
        activity.setStatus(ContentStatus.ACTIVE);
        activity.setGameEngineType(EngineType.MEMORY.name());
        activity.setTopicIds(List.of(TOPIC_ID));
        DifficultyLevel level = new DifficultyLevel();
        level.setId(DIFFICULTY_LEVEL_ID);
        level.setDifficultyCode(difficulty);
        level.setEngineParams("{}");
        when(gameCatalogUseCase.getGameReadiness(CHILD_PROFILE_ID, ACTIVITY_ID))
                .thenReturn(new GameCatalogReadiness(activity, level, true));
        when(difficultyLevelUseCase.getGameReadyDifficultyLevel(DIFFICULTY_LEVEL_ID)).thenReturn(level);

        GameState started = orchestrator.startGame(CHILD_PROFILE_ID, ACTIVITY_ID);
        started.setChildSessionId(100L);
        when(gameStateRegistry.findByGameId(started.getGameId())).thenReturn(Optional.of(started));
        return started;
    }

    private GameState startAndReady(DifficultyCode difficulty) {
        GameState started = start(difficulty);
        return orchestrator.readyGame(started.getGameId());
    }

    private MemoryState memoryState(GameState state) throws Exception {
        return MAPPER.readValue(state.getEnginePayload(), MemoryState.class);
    }

    private static String tap(String cardId) {
        return "{\"cardId\":\"" + cardId + "\",\"responseTimeMs\":700}";
    }

    @Test
    void startGame_resolvesEngineAndTheCandidatesOfTheActivityTopic() {
        GameState started = start(DifficultyCode.EASY);

        assertEquals(EngineType.MEMORY, started.getEngine());
        assertEquals(GameStatus.WAITING, started.getStatus());
        assertEquals(10, started.getCandidates().size());
        assertEquals(null, started.getRecognitionCategory());
    }

    @Test
    void readyGame_easy_dealsA2x2Board() throws Exception {
        GameState ready = startAndReady(DifficultyCode.EASY);
        MemoryState state = memoryState(ready);

        assertEquals(GameStatus.IN_PROGRESS, ready.getStatus());
        assertEquals(4, state.getCards().size());
        assertEquals(2, state.getTotalPairs());
        assertEquals(2000, state.getFlipDelayMs());
    }

    @Test
    void readyGame_medium_dealsA2x3Board() throws Exception {
        MemoryState state = memoryState(startAndReady(DifficultyCode.MEDIUM));

        assertEquals(6, state.getCards().size());
        assertEquals(3, state.getTotalPairs());
        assertEquals(1500, state.getFlipDelayMs());
    }

    @Test
    void readyGame_hard_dealsA2x4BoardWithElementsOfOneGroup() throws Exception {
        MemoryState state = memoryState(startAndReady(DifficultyCode.HARD));

        assertEquals(8, state.getCards().size());
        assertEquals(4, state.getTotalPairs());
        assertEquals(1000, state.getFlipDelayMs());
        Set<String> groups = state.getCards().stream()
                .map(c -> Integer.parseInt(c.getElementId()) <= 5 ? "fruit" : "vehicle")
                .collect(Collectors.toSet());
        assertEquals(1, groups.size(), "HARD deals elements of a single thematic group");
    }

    @Test
    void readyGame_generatesThePromptAudioOfTheFirstCardsElement() throws Exception {
        RoundAudioResult prompt = RoundAudioResult.withAudio("audio-1", new byte[] {1, 2}, "Encuentra las parejas");
        when(roundAudioService.generateRoundAudio(anyLong(), any())).thenReturn(prompt);

        GameState ready = startAndReady(DifficultyCode.EASY);

        String firstCardElement = memoryState(ready).getCards().get(0).getElementId();
        verify(roundAudioService).generateRoundAudio(CHILD_PROFILE_ID, firstCardElement);
        assertEquals(prompt, ready.getRoundAudioResult());
    }

    @Test
    void readyGameWithoutAudio_thenAttachRoundAudio_generatesThePromptOnce() {
        RoundAudioResult prompt = RoundAudioResult.withAudio("audio-1", new byte[] {1}, "Encuentra las parejas");
        when(roundAudioService.generateRoundAudio(anyLong(), any())).thenReturn(prompt);
        GameState started = start(DifficultyCode.EASY);

        orchestrator.readyGame(started.getGameId(), false);
        verify(roundAudioService, never()).generateRoundAudio(anyLong(), any());

        assertEquals(prompt, orchestrator.attachRoundAudio(started.getGameId()).getRoundAudioResult());
    }

    @Test
    void processAction_doesNotResendThePromptWithEachAction() throws Exception {
        when(roundAudioService.generateRoundAudio(anyLong(), any()))
                .thenReturn(RoundAudioResult.withAudio("audio-1", new byte[] {1}, "Encuentra las parejas"));
        GameState ready = startAndReady(DifficultyCode.EASY);
        String cardId = memoryState(ready).getCards().get(0).getCardId();

        ActionProcessingResult result = orchestrator.processAction(ready.getGameId(), tap(cardId), null, 700);

        assertNull(result.updatedState().getRoundAudioResult());
        verify(roundAudioService, times(1)).generateRoundAudio(anyLong(), any());
    }

    @Test
    void readyGame_audioFailure_doesNotBlockTheGame() {
        when(roundAudioService.generateRoundAudio(anyLong(), any())).thenThrow(new IllegalStateException("tts down"));

        GameState ready = startAndReady(DifficultyCode.EASY);

        assertEquals(GameStatus.IN_PROGRESS, ready.getStatus());
    }

    @Test
    void processAction_playsAMismatchAndThenTheWholeBoardToCompletion() throws Exception {
        GameState ready = startAndReady(DifficultyCode.EASY);
        Long gameId = ready.getGameId();
        List<MemoryCard> cards = memoryState(ready).getCards();

        MemoryCard first = cards.get(0);
        MemoryCard other = cards.stream()
                .filter(c -> !c.getElementId().equals(first.getElementId())).findFirst().orElseThrow();
        orchestrator.processAction(gameId, tap(first.getCardId()), null, 700);
        ActionProcessingResult mismatch = orchestrator.processAction(gameId, tap(other.getCardId()), null, 700);

        assertEquals(ActionResultType.INCORRECT, mismatch.resultType());
        assertFalse(mismatch.gameCompleted());
        assertTrue(memoryState(mismatch.updatedState()).isWaitingForFlipBack());

        // The mismatched pair is still on show: start with a card outside it (that turns the pair back), then the rest.
        Map<String, List<String>> byElement = new LinkedHashMap<>();
        MemoryCard outside = cards.stream()
                .filter(c -> !c.getCardId().equals(first.getCardId()) && !c.getCardId().equals(other.getCardId()))
                .findFirst().orElseThrow();
        byElement.computeIfAbsent(outside.getElementId(), k -> new ArrayList<>()).add(outside.getCardId());
        for (MemoryCard c : cards) {
            byElement.computeIfAbsent(c.getElementId(), k -> new ArrayList<>()).add(c.getCardId());
        }
        ActionProcessingResult last = null;
        for (List<String> pair : byElement.values()) {
            List<String> ordered = pair.stream().distinct().collect(Collectors.toList());
            orchestrator.processAction(gameId, tap(ordered.get(0)), null, 700);
            last = orchestrator.processAction(gameId, tap(ordered.get(1)), null, 700);
        }

        assertEquals(ActionResultType.CORRECT, last.resultType());
        assertTrue(last.gameCompleted());
        assertEquals(GameStatus.COMPLETED, last.updatedState().getStatus());
        verify(gameStateRegistry).remove(gameId);
    }

    /** Plays one mismatch and then every pair; returns the result of the last action. */
    private ActionProcessingResult playWithOneMismatch(GameState ready) throws Exception {
        Long gameId = ready.getGameId();
        List<MemoryCard> cards = memoryState(ready).getCards();
        MemoryCard first = cards.get(0);
        MemoryCard other = cards.stream()
                .filter(c -> !c.getElementId().equals(first.getElementId())).findFirst().orElseThrow();
        MemoryCard outside = cards.stream()
                .filter(c -> !c.getCardId().equals(first.getCardId()) && !c.getCardId().equals(other.getCardId()))
                .findFirst().orElseThrow();
        orchestrator.processAction(gameId, tap(first.getCardId()), null, 500);
        orchestrator.processAction(gameId, tap(other.getCardId()), null, 600);

        // The pair on show turns back with the next touch: begin with a card outside it.
        List<String> firstPair = new ArrayList<>(List.of(outside.getCardId()));
        cards.stream().filter(c -> c.getElementId().equals(outside.getElementId())
                && !c.getCardId().equals(outside.getCardId())).forEach(c -> firstPair.add(c.getCardId()));
        ActionProcessingResult last = null;
        List<List<String>> pairs = new ArrayList<>();
        pairs.add(firstPair);
        Map<String, List<String>> byElement = new LinkedHashMap<>();
        for (MemoryCard c : cards) {
            if (!c.getElementId().equals(outside.getElementId())) {
                byElement.computeIfAbsent(c.getElementId(), k -> new ArrayList<>()).add(c.getCardId());
            }
        }
        pairs.addAll(byElement.values());
        for (List<String> pair : pairs) {
            orchestrator.processAction(gameId, tap(pair.get(0)), null, 700);
            last = orchestrator.processAction(gameId, tap(pair.get(1)), null, 800);
        }
        return last;
    }

    @Test
    void nothingIsRegisteredBeforeTheGameCompletes() throws Exception {
        GameState ready = startAndReady(DifficultyCode.EASY);
        List<MemoryCard> cards = memoryState(ready).getCards();
        MemoryCard first = cards.get(0);
        MemoryCard other = cards.stream()
                .filter(c -> !c.getElementId().equals(first.getElementId())).findFirst().orElseThrow();

        orchestrator.processAction(ready.getGameId(), tap(first.getCardId()), null, 500);
        orchestrator.processAction(ready.getGameId(), tap(other.getCardId()), null, 600);

        assertEquals(1, memoryState(ready).getRoundAttempts().size(), "the pair attempt is buffered");
        verify(registerActivityAttemptUseCase, never()).register(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void completion_flushesEveryPairAttemptThroughRegisterActivityAttempt() throws Exception {
        GameState ready = startAndReady(DifficultyCode.EASY);

        ActionProcessingResult last = playWithOneMismatch(ready);

        assertTrue(last.gameCompleted());
        ArgumentCaptor<AttemptResult> results = ArgumentCaptor.forClass(AttemptResult.class);
        ArgumentCaptor<Long> elements = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> topics = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> levels = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Integer> times = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<String> contexts = ArgumentCaptor.forClass(String.class);
        // 1 mismatch + 2 matches on a 2x2 board
        verify(registerActivityAttemptUseCase, times(3)).register(
                eq(CHILD_PROFILE_ID), eq(ACTIVITY_ID), eq(100L), topics.capture(), elements.capture(), levels.capture(),
                results.capture(), times.capture(), contexts.capture());

        assertEquals(List.of(AttemptResult.INCORRECT, AttemptResult.CORRECT, AttemptResult.CORRECT), results.getAllValues());
        assertEquals(List.of(700, 700, 700), times.getAllValues(), "the response time comes from the action payload");
        assertNull(elements.getAllValues().get(0), "a pair that did not match has no element to attribute it to");
        assertNotNull(elements.getAllValues().get(1));
        assertNotNull(elements.getAllValues().get(2));
        assertTrue(topics.getAllValues().stream().allMatch(TOPIC_ID::equals));
        assertTrue(levels.getAllValues().stream().allMatch(DIFFICULTY_LEVEL_ID::equals));

        JsonNode mismatch = MAPPER.readTree(contexts.getAllValues().get(0));
        assertEquals("MEMORY", mismatch.get("engineType").asString());
        assertFalse(mismatch.get("match").asBoolean());
        assertEquals(1, mismatch.get("attemptNumber").asInt());
        assertFalse(mismatch.get("cardId1").isNull());
        assertFalse(mismatch.get("cardId2").isNull());
        JsonNode match = MAPPER.readTree(contexts.getAllValues().get(1));
        assertTrue(match.get("match").asBoolean());
        assertEquals(2, match.get("attemptNumber").asInt());
    }

    @Test
    void completion_registersTheSessionSummaryOnce() throws Exception {
        GameState ready = startAndReady(DifficultyCode.EASY);

        ActionProcessingResult last = playWithOneMismatch(ready);

        assertTrue(last.gameCompleted());
        verify(registerGameSessionSummaryUseCase).registerGameSessionSummary(
                eq(CHILD_PROFILE_ID), eq(100L), eq(ACTIVITY_ID), eq(DIFFICULTY_LEVEL_ID), eq(DIFFICULTY_LEVEL_ID),
                eq(0), eq(3), eq(2), eq(0), any(), any(), eq(GameSessionFinalStatus.COMPLETED), any());
    }

    @Test
    void completion_evaluatesTheAchievementsOfTheBoardsTopicWhenTheClientSendsNone() throws Exception {
        GameState ready = startAndReady(DifficultyCode.EASY);

        playWithOneMismatch(ready);

        verify(evaluateGameCompletionAchievementsUseCase).evaluate(CHILD_PROFILE_ID, ACTIVITY_ID, TOPIC_ID);
    }

    @Test
    void completion_returnsTheUnlockedAchievementsOfTheFlush() throws Exception {
        UnlockedAchievement achievement = new UnlockedAchievement("MEMORY_FIRST", 1L, null);
        when(registerActivityAttemptUseCase.register(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new AttemptRegistrationResult(1L, java.time.LocalDateTime.now(), List.of(achievement)));
        GameState ready = startAndReady(DifficultyCode.EASY);

        ActionProcessingResult last = playWithOneMismatch(ready);

        assertEquals("MEMORY_FIRST", last.unlockedAchievements().get(0).achievementCode());
    }

    @Test
    void repetition_registersNothing() throws Exception {
        GameState first = startAndReady(DifficultyCode.EASY);
        playWithOneMismatch(first);
        clearInvocations(registerActivityAttemptUseCase, registerGameSessionSummaryUseCase);

        GameState second = startAndReady(DifficultyCode.EASY);
        assertTrue(second.isRepetition());
        playWithOneMismatch(second);

        verify(registerActivityAttemptUseCase, never()).register(any(), any(), any(), any(), any(), any(), any(), any(), any());
        verify(registerGameSessionSummaryUseCase, never()).registerGameSessionSummary(
                any(), any(), any(), any(), any(), anyInt(), anyInt(), anyInt(), anyInt(), any(), any(), any(), any());
    }

    @Test
    void abandonedGame_dropsTheBufferWithoutRegisteringAttempts() throws Exception {
        GameState ready = startAndReady(DifficultyCode.EASY);
        List<MemoryCard> cards = memoryState(ready).getCards();
        MemoryCard first = cards.get(0);
        MemoryCard other = cards.stream()
                .filter(c -> !c.getElementId().equals(first.getElementId())).findFirst().orElseThrow();
        orchestrator.processAction(ready.getGameId(), tap(first.getCardId()), null, 500);
        orchestrator.processAction(ready.getGameId(), tap(other.getCardId()), null, 600);

        orchestrator.abandonGame(ready.getGameId());

        verify(registerActivityAttemptUseCase, never()).register(any(), any(), any(), any(), any(), any(), any(), any(), any());
        verify(gameStateRegistry).remove(ready.getGameId());
    }

    @Test
    void abandonedGame_reportsNoPartialProgressInTheSessionSummary() throws Exception {
        GameState ready = startAndReady(DifficultyCode.EASY);
        List<MemoryCard> cards = memoryState(ready).getCards();
        MemoryCard first = cards.get(0);
        MemoryCard other = cards.stream()
                .filter(c -> !c.getElementId().equals(first.getElementId())).findFirst().orElseThrow();
        orchestrator.processAction(ready.getGameId(), tap(first.getCardId()), null, 500);
        orchestrator.processAction(ready.getGameId(), tap(other.getCardId()), null, 600);
        assertEquals(1, ready.getAttempts(), "the engine does keep running counters in the state");

        orchestrator.abandonGame(ready.getGameId());

        verify(registerGameSessionSummaryUseCase).registerGameSessionSummary(
                eq(CHILD_PROFILE_ID), eq(100L), eq(ACTIVITY_ID), eq(DIFFICULTY_LEVEL_ID), eq(DIFFICULTY_LEVEL_ID),
                eq(0), eq(0), eq(0), eq(0), any(), any(), eq(GameSessionFinalStatus.ABANDONED), any());
    }

    @Test
    void discardedGame_dropsTheBufferWithoutRegisteringAnything() throws Exception {
        GameState ready = startAndReady(DifficultyCode.EASY);
        when(gameStateRegistry.findByChildSessionId(100L)).thenReturn(Optional.of(ready));
        String cardId = memoryState(ready).getCards().get(0).getCardId();
        orchestrator.processAction(ready.getGameId(), tap(cardId), null, 500);

        orchestrator.discardGameForSession(100L);

        verify(registerActivityAttemptUseCase, never()).register(any(), any(), any(), any(), any(), any(), any(), any(), any());
        verify(registerGameSessionSummaryUseCase, never()).registerGameSessionSummary(
                any(), any(), any(), any(), any(), anyInt(), anyInt(), anyInt(), anyInt(), any(), any(), any(), any());
        verify(gameStateRegistry).remove(ready.getGameId());
    }

    @Test
    void abandonGame_worksForMemory() {
        GameState ready = startAndReady(DifficultyCode.MEDIUM);

        GameState abandoned = orchestrator.abandonGame(ready.getGameId());

        assertEquals(GameStatus.ABANDONED, abandoned.getStatus());
        verify(gameStateRegistry).remove(ready.getGameId());
    }

    @Test
    void enginePayload_isAMemoryStateNotARecognitionState() throws Exception {
        GameState ready = startAndReady(DifficultyCode.EASY);

        JsonNode payload = MAPPER.readTree(ready.getEnginePayload());
        assertTrue(payload.has("cards"));
        assertFalse(payload.has("roundIndex"));
    }

    private static int anyInt() {
        return org.mockito.ArgumentMatchers.anyInt();
    }
}
