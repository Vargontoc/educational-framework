package es.vargontoc.educational.framework.game.service;

import es.vargontoc.educational.framework.content.model.Activity;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.DifficultyCode;
import es.vargontoc.educational.framework.content.model.DifficultyLevel;
import es.vargontoc.educational.framework.content.model.GameCatalogReadiness;
import es.vargontoc.educational.framework.content.model.RecognitionElement;
import es.vargontoc.educational.framework.content.model.RecognitionType;
import es.vargontoc.educational.framework.content.model.Topic;
import es.vargontoc.educational.framework.content.ports.in.DifficultyLevelUseCase;
import es.vargontoc.educational.framework.content.ports.in.GameCatalogUseCase;
import es.vargontoc.educational.framework.content.ports.in.TopicUseCase;
import es.vargontoc.educational.framework.content.ports.out.RecognitionElementRepository;
import es.vargontoc.educational.framework.family.model.ChildProfile;
import es.vargontoc.educational.framework.family.model.ColorVisionMode;
import es.vargontoc.educational.framework.family.ports.in.ChildProfileUseCase;
import es.vargontoc.educational.framework.game.application.RecognitionProperties;
import es.vargontoc.educational.framework.game.model.ActionProcessingResult;
import es.vargontoc.educational.framework.game.model.ActionResultType;
import es.vargontoc.educational.framework.game.model.GameState;
import es.vargontoc.educational.framework.game.model.GameStatus;
import es.vargontoc.educational.framework.game.model.enums.EngineType;
import es.vargontoc.educational.framework.game.model.enums.RecognitionCategory;
import es.vargontoc.educational.framework.game.model.recognition.ComparisonOption;
import es.vargontoc.educational.framework.game.model.recognition.RecognitionDifficultyConfig;
import es.vargontoc.educational.framework.game.model.recognition.RecognitionState;
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
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * SPRINT-108: full flow of a COMPARISON activity through the orchestrator and the real RecognitionEngine.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GameOrchestratorServiceComparisonTest {

    private static final Long CHILD_PROFILE_ID = 200L;
    private static final Long ACTIVITY_ID = 9L;
    private static final Long TOPIC_ID = 30L;
    private static final Long DIFFICULTY_LEVEL_ID = 5L;
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

        ChildProfile child = new ChildProfile();
        child.setId(CHILD_PROFILE_ID);
        child.setColorVisionMode(ColorVisionMode.NONE);
        when(childProfileUseCase.getChild(CHILD_PROFILE_ID)).thenReturn(child);

        Topic topic = new Topic();
        topic.setId(TOPIC_ID);
        topic.setName("Comparación");
        topic.setRecognitionType(RecognitionType.COMPARISON);
        when(topicUseCase.getTopic(TOPIC_ID)).thenReturn(topic);
        when(topicUseCase.listTopicsByRecognitionType(RecognitionType.COMPARISON)).thenReturn(List.of(topic));

        List<RecognitionElement> elements = new ArrayList<>();
        for (long id = 1; id <= 5; id++) {
            RecognitionElement element = new RecognitionElement();
            element.setId(id);
            element.setTopicId(TOPIC_ID);
            element.setCode("item" + id);
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

        when(filterAllowedRecognitionCategoriesUseCase.filterAllowedCategories(
                eq(CHILD_PROFILE_ID), ArgumentMatchers.<List<es.vargontoc.educational.framework.tracking.model.RecognitionCategory>>any()))
                .thenReturn(List.of(es.vargontoc.educational.framework.tracking.model.RecognitionCategory.COMPARISON));
        when(sessionAntiRepetitionRegistry.getRecentElements(any(), any())).thenReturn(List.of());
    }

    private GameState startAndReady(DifficultyCode difficulty) {
        Activity activity = new Activity();
        activity.setId(ACTIVITY_ID);
        activity.setName("Comparación de Tamaño");
        activity.setStatus(ContentStatus.ACTIVE);
        activity.setGameEngineType(EngineType.RECOGNITION.name());
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
        return orchestrator.readyGame(started.getGameId());
    }

    private RecognitionState recognitionState(GameState state) throws Exception {
        return MAPPER.readValue(state.getEnginePayload(), RecognitionState.class);
    }

    private List<Double> scales(RecognitionState state) {
        return state.getComparisonOptions().stream().map(ComparisonOption::scalePercent).sorted().toList();
    }

    @Test
    void easy_readyGame_buildsTwoOptionsOfTheSameElementAt100And40() throws Exception {
        GameState ready = startAndReady(DifficultyCode.EASY);
        RecognitionState state = recognitionState(ready);

        assertEquals(GameStatus.IN_PROGRESS, ready.getStatus());
        assertEquals(RecognitionCategory.COMPARISON, ready.getRecognitionCategory());
        assertTrue(state.isComparisonMode());
        assertEquals(List.of(40.0, 100.0), scales(state));
        assertEquals(Set.of(state.getTargetElementId()), Set.copyOf(state.getOptionIds()));
    }

    @Test
    void medium_readyGame_buildsTwoOptionsAt100And65() throws Exception {
        assertEquals(List.of(65.0, 100.0), scales(recognitionState(startAndReady(DifficultyCode.MEDIUM))));
    }

    @Test
    void hard_readyGame_buildsThreeOptionsAt100_75And50() throws Exception {
        RecognitionState state = recognitionState(startAndReady(DifficultyCode.HARD));

        assertEquals(List.of(50.0, 75.0, 100.0), scales(state));
        assertEquals(3, state.getOptionIds().size());
    }

    @Test
    void processAction_biggestAdvancesTheRound_smallerKeepsIt() throws Exception {
        GameState ready = startAndReady(DifficultyCode.EASY);
        RecognitionState state = recognitionState(ready);
        String target = state.getTargetElementId();

        ActionProcessingResult wrong = orchestrator.processAction(
                ready.getGameId(), "{\"selectedOptionId\":\"" + target + "\",\"selectedScalePercent\":40}", null, 900);
        assertEquals(ActionResultType.INCORRECT, wrong.resultType());
        assertEquals(0, recognitionState(wrong.updatedState()).getRoundIndex());

        ActionProcessingResult right = orchestrator.processAction(
                ready.getGameId(), "{\"selectedOptionId\":\"" + target + "\",\"selectedScalePercent\":100}", null, 900);
        assertEquals(ActionResultType.CORRECT, right.resultType());
        RecognitionState next = recognitionState(right.updatedState());
        assertEquals(1, next.getRoundIndex());
        assertEquals(List.of(40.0, 100.0), scales(next));
    }

    @Test
    void enginePayload_carriesTheComparisonState() throws Exception {
        GameState ready = startAndReady(DifficultyCode.HARD);

        JsonNode payload = MAPPER.readTree(ready.getEnginePayload());
        assertTrue(payload.get("comparisonMode").asBoolean());
        assertFalse(payload.get("comparisonOptions").isEmpty());
    }
}
