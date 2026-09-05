package es.vargontoc.educational.framework.game.service;

import es.vargontoc.educational.framework.content.model.Activity;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.DifficultyCode;
import es.vargontoc.educational.framework.content.model.DifficultyLevel;
import es.vargontoc.educational.framework.content.model.GameCatalogReadiness;
import es.vargontoc.educational.framework.content.model.RecognitionElement;
import es.vargontoc.educational.framework.content.model.RecognitionType;
import es.vargontoc.educational.framework.content.model.Topic;
import es.vargontoc.educational.framework.content.ports.in.GameCatalogUseCase;
import es.vargontoc.educational.framework.content.ports.in.TopicUseCase;
import es.vargontoc.educational.framework.content.ports.out.RecognitionElementRepository;
import es.vargontoc.educational.framework.game.model.GameState;
import es.vargontoc.educational.framework.game.model.enums.EngineType;
import es.vargontoc.educational.framework.game.ports.out.GameStateRegistry;
import es.vargontoc.educational.framework.game.ports.out.SessionAntiRepetitionRegistry;
import es.vargontoc.educational.framework.tracking.model.ElementMasteryState;
import es.vargontoc.educational.framework.tracking.model.ElementSummary;
import es.vargontoc.educational.framework.tracking.model.RecognitionCategory;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameOrchestratorServiceMasteryPrioritizationTest {

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

    @Test
    void candidates_prioritizeNotStartedOverMastered() {
        Activity activity = createActivity(1L, List.of(10L));
        DifficultyLevel dl = createDifficultyLevel(5L);
        GameCatalogReadiness readiness = new GameCatalogReadiness(activity, dl, true);

        Topic topic = createTopic(10L, RecognitionType.LETTER);
        RecognitionElement element = createElement(100L, 10L);

        when(gameCatalogUseCase.getGameReadiness(100L, 1L)).thenReturn(readiness);
        when(topicUseCase.getTopic(10L)).thenReturn(topic);
        when(filterAllowedRecognitionCategoriesUseCase.filterAllowedCategories(
                eq(100L), org.mockito.ArgumentMatchers.<List<RecognitionCategory>>any()))
                .thenReturn(List.of(RecognitionCategory.LETTER));
        when(topicUseCase.listTopicsByRecognitionType(RecognitionType.LETTER))
                .thenReturn(List.of(topic));
        when(recognitionElementRepository.findByTopicIdAndStatus(10L, ContentStatus.ACTIVE))
                .thenReturn(List.of(element));

        ElementSummary masteredSummary = new ElementSummary();
        masteredSummary.setElementId(100L);
        masteredSummary.setMasteryState(ElementMasteryState.MASTERED);

        when(elementProgressPort.getElementSummariesForChildInTopic(100L, 10L))
                .thenReturn(List.of(masteredSummary));

        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));

        GameState result = orchestratorService.startGame(100L, 1L);

        assertEquals(List.of("100"), result.getCandidates());
    }

    @Test
    void candidates_orderNotStartedBeforeLearningBeforeMastered() {
        Activity activity = createActivity(1L, List.of(10L));
        DifficultyLevel dl = createDifficultyLevel(5L);
        GameCatalogReadiness readiness = new GameCatalogReadiness(activity, dl, true);

        Topic topic = createTopic(10L, RecognitionType.LETTER);
        Topic topic2 = createTopic(20L, RecognitionType.LETTER);
        Topic topic3 = createTopic(30L, RecognitionType.LETTER);

        RecognitionElement element10 = createElement(100L, 10L);
        RecognitionElement element20 = createElement(200L, 20L);
        RecognitionElement element30 = createElement(300L, 30L);

        when(gameCatalogUseCase.getGameReadiness(100L, 1L)).thenReturn(readiness);
        when(topicUseCase.getTopic(10L)).thenReturn(topic);
        when(filterAllowedRecognitionCategoriesUseCase.filterAllowedCategories(
                eq(100L), org.mockito.ArgumentMatchers.<List<RecognitionCategory>>any()))
                .thenReturn(List.of(RecognitionCategory.LETTER));

        when(topicUseCase.listTopicsByRecognitionType(RecognitionType.LETTER))
                .thenReturn(List.of(topic, topic2, topic3));

        when(recognitionElementRepository.findByTopicIdAndStatus(10L, ContentStatus.ACTIVE))
                .thenReturn(List.of(element10));
        when(recognitionElementRepository.findByTopicIdAndStatus(20L, ContentStatus.ACTIVE))
                .thenReturn(List.of(element20));
        when(recognitionElementRepository.findByTopicIdAndStatus(30L, ContentStatus.ACTIVE))
                .thenReturn(List.of(element30));

        ElementSummary mastered = new ElementSummary();
        mastered.setElementId(100L);
        mastered.setMasteryState(ElementMasteryState.MASTERED);

        ElementSummary learning = new ElementSummary();
        learning.setElementId(200L);
        learning.setMasteryState(ElementMasteryState.LEARNING);

        when(elementProgressPort.getElementSummariesForChildInTopic(100L, 10L))
                .thenReturn(List.of(mastered, learning));

        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));

        GameState result = orchestratorService.startGame(100L, 1L);

        List<String> candidates = result.getCandidates();
        assertEquals(3, candidates.size());
        assertEquals("300", candidates.get(0));
        assertEquals("200", candidates.get(1));
        assertEquals("100", candidates.get(2));
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

    private RecognitionElement createElement(Long id, Long topicId) {
        RecognitionElement element = new RecognitionElement();
        element.setId(id);
        element.setTopicId(topicId);
        element.setStatus(ContentStatus.ACTIVE);
        return element;
    }
}
