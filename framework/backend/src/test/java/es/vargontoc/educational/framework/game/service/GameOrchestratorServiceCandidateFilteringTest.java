package es.vargontoc.educational.framework.game.service;

import es.vargontoc.educational.framework.game.service.RecognitionSimilarityService;
import es.vargontoc.educational.framework.content.model.Activity;
import es.vargontoc.educational.framework.content.model.Biome;
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
import es.vargontoc.educational.framework.game.model.GameStatus;
import es.vargontoc.educational.framework.game.model.LaunchContext;
import es.vargontoc.educational.framework.game.model.enums.EngineType;
import es.vargontoc.educational.framework.game.ports.out.GameStateRegistry;
import es.vargontoc.educational.framework.game.ports.out.SessionAntiRepetitionRegistry;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameOrchestratorServiceCandidateFilteringTest {

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
            roundAudioService,
            null,
            null
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

    private RecognitionElement createElement(Long id, Long topicId) {
        RecognitionElement element = new RecognitionElement();
        element.setId(id);
        element.setTopicId(topicId);
        element.setStatus(ContentStatus.ACTIVE);
        return element;
    }

    @Test
    void startGame_withoutLaunchContext_stillWorks() {
        Activity activity = createActivity(1L, List.of(10L));
        DifficultyLevel difficultyLevel = createDifficultyLevel(5L);
        GameCatalogReadiness readiness = new GameCatalogReadiness(activity, difficultyLevel, true);

        Topic topic = createTopic(10L, RecognitionType.LETTER);
        RecognitionElement element = createElement(100L, 10L);

        when(gameCatalogUseCase.getGameReadiness(100L, 1L)).thenReturn(readiness);
        when(topicUseCase.getTopic(10L)).thenReturn(topic);
        when(filterAllowedRecognitionCategoriesUseCase.filterAllowedCategories(
                eq(100L), ArgumentMatchers.<List<RecognitionCategory>>any()))
                .thenReturn(List.of(RecognitionCategory.LETTER));
        when(topicUseCase.listTopicsByRecognitionType(RecognitionType.LETTER))
                .thenReturn(List.of(topic));
        when(recognitionElementRepository.findByTopicIdAndStatus(10L, ContentStatus.ACTIVE))
                .thenReturn(List.of(element));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));

        GameState result = orchestratorService.startGame(100L, 1L);

        assertNotNull(result);
        assertEquals(GameStatus.WAITING, result.getStatus());
        assertNotNull(result.getCandidates());
        assertEquals(1, result.getCandidates().size());
        assertEquals("100", result.getCandidates().get(0));
        verify(gameStateRegistry).save(any(GameState.class));
    }

    @Test
    void startGame_withHabitatTag_filtersAnimalCandidates() {
        Activity activity = createActivity(1L, List.of(20L));
        DifficultyLevel difficultyLevel = createDifficultyLevel(5L);
        GameCatalogReadiness readiness = new GameCatalogReadiness(activity, difficultyLevel, true);

        Topic animalTopic = createTopic(20L, RecognitionType.ANIMAL);
        Topic farmAnimalTopic = createTopic(21L, RecognitionType.ANIMAL);
        farmAnimalTopic.setHabitatTag(Biome.FARM);
        RecognitionElement farmElement = createElement(210L, 21L);

        LaunchContext launchContext = new LaunchContext(null, "FARM", null, null);

        when(gameCatalogUseCase.getGameReadiness(100L, 1L)).thenReturn(readiness);
        when(topicUseCase.getTopic(20L)).thenReturn(animalTopic);
        when(filterAllowedRecognitionCategoriesUseCase.filterAllowedCategories(
                eq(100L), ArgumentMatchers.<List<RecognitionCategory>>any()))
                .thenReturn(List.of(RecognitionCategory.ANIMAL));
        when(topicUseCase.listTopicsByRecognitionTypeAndHabitat(RecognitionType.ANIMAL, Biome.FARM))
                .thenReturn(List.of(farmAnimalTopic));
        when(recognitionElementRepository.findByTopicIdAndStatus(21L, ContentStatus.ACTIVE))
                .thenReturn(List.of(farmElement));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));

        GameState result = orchestratorService.startGame(100L, 1L, launchContext);

        assertNotNull(result);
        assertNotNull(result.getCandidates());
        assertEquals(1, result.getCandidates().size());
        assertEquals("210", result.getCandidates().get(0));
        verify(topicUseCase).listTopicsByRecognitionTypeAndHabitat(RecognitionType.ANIMAL, Biome.FARM);
        verify(topicUseCase, never()).listTopicsByRecognitionType(RecognitionType.ANIMAL);
    }

    @Test
    void startGame_nonAnimalCategory_doesNotRequireHabitatTag() {
        Activity activity = createActivity(1L, List.of(30L));
        DifficultyLevel difficultyLevel = createDifficultyLevel(5L);
        GameCatalogReadiness readiness = new GameCatalogReadiness(activity, difficultyLevel, true);

        Topic shapeTopic = createTopic(30L, RecognitionType.SHAPE);
        Topic anotherShapeTopic = createTopic(31L, RecognitionType.SHAPE);
        RecognitionElement element30 = createElement(300L, 30L);
        RecognitionElement element31 = createElement(310L, 31L);

        LaunchContext launchContext = new LaunchContext(null, "JUNGLE", null, null);

        when(gameCatalogUseCase.getGameReadiness(100L, 1L)).thenReturn(readiness);
        when(topicUseCase.getTopic(30L)).thenReturn(shapeTopic);
        when(filterAllowedRecognitionCategoriesUseCase.filterAllowedCategories(
                eq(100L), ArgumentMatchers.<List<RecognitionCategory>>any()))
                .thenReturn(List.of(RecognitionCategory.SHAPE));
        when(topicUseCase.listTopicsByRecognitionType(RecognitionType.SHAPE))
                .thenReturn(List.of(shapeTopic, anotherShapeTopic));
        when(recognitionElementRepository.findByTopicIdAndStatus(30L, ContentStatus.ACTIVE))
                .thenReturn(List.of(element30));
        when(recognitionElementRepository.findByTopicIdAndStatus(31L, ContentStatus.ACTIVE))
                .thenReturn(List.of(element31));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));

        GameState result = orchestratorService.startGame(100L, 1L, launchContext);

        assertNotNull(result);
        assertNotNull(result.getCandidates());
        assertEquals(2, result.getCandidates().size());
        assertTrue(result.getCandidates().contains("300"));
        assertTrue(result.getCandidates().contains("310"));
        verify(topicUseCase).listTopicsByRecognitionType(RecognitionType.SHAPE);
        verify(topicUseCase, never()).listTopicsByRecognitionTypeAndHabitat(any(), any());
    }

    @Test
    void startGame_numberLocked_excludesNumber() {
        Activity activity = createActivity(1L, List.of(40L));
        DifficultyLevel difficultyLevel = createDifficultyLevel(5L);
        GameCatalogReadiness readiness = new GameCatalogReadiness(activity, difficultyLevel, true);

        Topic numberTopic = createTopic(40L, RecognitionType.NUMBER);

        when(gameCatalogUseCase.getGameReadiness(100L, 1L)).thenReturn(readiness);
        when(topicUseCase.getTopic(40L)).thenReturn(numberTopic);
        when(filterAllowedRecognitionCategoriesUseCase.filterAllowedCategories(
                eq(100L), ArgumentMatchers.<List<RecognitionCategory>>any()))
                .thenReturn(List.of());
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));

        GameState result = orchestratorService.startGame(100L, 1L);

        assertNotNull(result);
        assertNotNull(result.getCandidates());
        assertTrue(result.getCandidates().isEmpty());
        verify(topicUseCase, never()).listTopicsByRecognitionType(any());
        verify(topicUseCase, never()).listTopicsByRecognitionTypeAndHabitat(any(), any());
    }

    @Test
    void startGame_zeroActiveElements_doesNotCrash() {
        Activity activity = createActivity(1L, List.of(60L));
        DifficultyLevel difficultyLevel = createDifficultyLevel(5L);
        GameCatalogReadiness readiness = new GameCatalogReadiness(activity, difficultyLevel, true);

        Topic topic = createTopic(60L, RecognitionType.LETTER);

        when(gameCatalogUseCase.getGameReadiness(100L, 1L)).thenReturn(readiness);
        when(topicUseCase.getTopic(60L)).thenReturn(topic);
        when(filterAllowedRecognitionCategoriesUseCase.filterAllowedCategories(
                eq(100L), ArgumentMatchers.<List<RecognitionCategory>>any()))
                .thenReturn(List.of(RecognitionCategory.LETTER));
        when(topicUseCase.listTopicsByRecognitionType(RecognitionType.LETTER))
                .thenReturn(List.of(topic));
        when(recognitionElementRepository.findByTopicIdAndStatus(60L, ContentStatus.ACTIVE))
                .thenReturn(List.of());
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));

        GameState result = orchestratorService.startGame(100L, 1L);

        assertNotNull(result);
        assertEquals(GameStatus.WAITING, result.getStatus());
        assertNotNull(result.getCandidates());
        assertTrue(result.getCandidates().isEmpty());
        verify(gameStateRegistry).save(any(GameState.class));
    }

    @Test
    void readyGame_engineReceivesCandidatesNotLaunchContext() {
        Activity activity = createActivity(1L, List.of(50L));
        DifficultyLevel difficultyLevel = createDifficultyLevel(5L);
        GameCatalogReadiness readiness = new GameCatalogReadiness(activity, difficultyLevel, true);

        Topic letterTopic = createTopic(50L, RecognitionType.LETTER);
        Topic anotherLetterTopic = createTopic(51L, RecognitionType.LETTER);
        RecognitionElement element50 = createElement(500L, 50L);
        RecognitionElement element51 = createElement(510L, 51L);

        LaunchContext launchContext = new LaunchContext("world-1", null, "disc-1", "narr-1");

        when(gameCatalogUseCase.getGameReadiness(100L, 1L)).thenReturn(readiness);
        when(topicUseCase.getTopic(50L)).thenReturn(letterTopic);
        when(filterAllowedRecognitionCategoriesUseCase.filterAllowedCategories(
                eq(100L), ArgumentMatchers.<List<RecognitionCategory>>any()))
                .thenReturn(List.of(RecognitionCategory.LETTER));
        when(topicUseCase.listTopicsByRecognitionType(RecognitionType.LETTER))
                .thenReturn(List.of(letterTopic, anotherLetterTopic));
        when(recognitionElementRepository.findByTopicIdAndStatus(50L, ContentStatus.ACTIVE))
                .thenReturn(List.of(element50));
        when(recognitionElementRepository.findByTopicIdAndStatus(51L, ContentStatus.ACTIVE))
                .thenReturn(List.of(element51));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));
        when(gameStateRegistry.findByGameId(any())).thenAnswer(invocation -> {
            GameState savedState = new GameState();
            savedState.setGameId(invocation.getArgument(0));
            savedState.setStatus(GameStatus.WAITING);
            savedState.setEngine(EngineType.RECOGNITION);
            savedState.setCandidates(List.of("500", "510"));
            return java.util.Optional.of(savedState);
        });

        GameState startResult = orchestratorService.startGame(100L, 1L, launchContext);
        assertNotNull(startResult.getCandidates());
        assertEquals(List.of("500", "510"), startResult.getCandidates());

        GameState readyResult = orchestratorService.readyGame(startResult.getGameId());

        assertNotNull(readyResult);
        assertEquals(GameStatus.IN_PROGRESS, readyResult.getStatus());
        assertNotNull(readyResult.getEnginePayload());
        assertTrue(readyResult.getEnginePayload().contains("500"));
        assertTrue(readyResult.getEnginePayload().contains("510"));
    }

    @Test
    void startGame_colorCategory_returnsColorElements() {
        Activity activity = createActivity(1L, List.of(70L));
        DifficultyLevel difficultyLevel = createDifficultyLevel(5L);
        GameCatalogReadiness readiness = new GameCatalogReadiness(activity, difficultyLevel, true);

        Topic colorTopic = createTopic(70L, RecognitionType.COLOR);
        RecognitionElement red = createElement(700L, 70L);
        RecognitionElement blue = createElement(710L, 70L);

        when(gameCatalogUseCase.getGameReadiness(100L, 1L)).thenReturn(readiness);
        when(topicUseCase.getTopic(70L)).thenReturn(colorTopic);
        when(filterAllowedRecognitionCategoriesUseCase.filterAllowedCategories(
                eq(100L), ArgumentMatchers.<List<RecognitionCategory>>any()))
                .thenReturn(List.of(RecognitionCategory.COLOR));
        when(topicUseCase.listTopicsByRecognitionType(RecognitionType.COLOR))
                .thenReturn(List.of(colorTopic));
        when(recognitionElementRepository.findByTopicIdAndStatus(70L, ContentStatus.ACTIVE))
                .thenReturn(List.of(red, blue));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));

        GameState result = orchestratorService.startGame(100L, 1L);

        assertNotNull(result);
        assertNotNull(result.getCandidates());
        assertEquals(2, result.getCandidates().size());
        assertTrue(result.getCandidates().contains("700"));
        assertTrue(result.getCandidates().contains("710"));
        verify(topicUseCase).listTopicsByRecognitionType(RecognitionType.COLOR);
    }

    @Test
    void startGame_shapeCategory_returnsShapeElementsWithSimilarityGroup() {
        Activity activity = createActivity(1L, List.of(80L));
        DifficultyLevel difficultyLevel = createDifficultyLevel(5L);
        GameCatalogReadiness readiness = new GameCatalogReadiness(activity, difficultyLevel, true);

        Topic shapeTopic = createTopic(80L, RecognitionType.SHAPE);
        RecognitionElement circle = createElement(800L, 80L);
        circle.setSimilarityGroup("round");
        RecognitionElement oval = createElement(810L, 80L);
        oval.setSimilarityGroup("round");

        when(gameCatalogUseCase.getGameReadiness(100L, 1L)).thenReturn(readiness);
        when(topicUseCase.getTopic(80L)).thenReturn(shapeTopic);
        when(filterAllowedRecognitionCategoriesUseCase.filterAllowedCategories(
                eq(100L), ArgumentMatchers.<List<RecognitionCategory>>any()))
                .thenReturn(List.of(RecognitionCategory.SHAPE));
        when(topicUseCase.listTopicsByRecognitionType(RecognitionType.SHAPE))
                .thenReturn(List.of(shapeTopic));
        when(recognitionElementRepository.findByTopicIdAndStatus(80L, ContentStatus.ACTIVE))
                .thenReturn(List.of(circle, oval));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));

        GameState result = orchestratorService.startGame(100L, 1L);

        assertNotNull(result);
        assertEquals(2, result.getCandidates().size());
        assertTrue(result.getCandidates().contains("800"));
        assertTrue(result.getCandidates().contains("810"));
        assertEquals("round", circle.getSimilarityGroup());
        assertEquals("round", oval.getSimilarityGroup());
    }

    @Test
    void readyGame_colorCategoryWithRealContent_resolvesWithoutError() {
        Activity activity = createActivity(1L, List.of(90L));
        DifficultyLevel difficultyLevel = createDifficultyLevel(5L);
        GameCatalogReadiness readiness = new GameCatalogReadiness(activity, difficultyLevel, true);

        Topic colorTopic = createTopic(90L, RecognitionType.COLOR);
        RecognitionElement red = createElement(900L, 90L);
        RecognitionElement blue = createElement(910L, 90L);

        when(gameCatalogUseCase.getGameReadiness(100L, 1L)).thenReturn(readiness);
        when(topicUseCase.getTopic(90L)).thenReturn(colorTopic);
        when(filterAllowedRecognitionCategoriesUseCase.filterAllowedCategories(
                eq(100L), ArgumentMatchers.<List<RecognitionCategory>>any()))
                .thenReturn(List.of(RecognitionCategory.COLOR));
        when(topicUseCase.listTopicsByRecognitionType(RecognitionType.COLOR))
                .thenReturn(List.of(colorTopic));
        when(recognitionElementRepository.findByTopicIdAndStatus(90L, ContentStatus.ACTIVE))
                .thenReturn(List.of(red, blue));
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));

        GameState startResult = orchestratorService.startGame(100L, 1L);
        assertEquals(List.of("900", "910"), startResult.getCandidates());

        GameState savedState = new GameState();
        savedState.setGameId(startResult.getGameId());
        savedState.setStatus(GameStatus.WAITING);
        savedState.setEngine(EngineType.RECOGNITION);
        savedState.setCandidates(List.of("900", "910"));
        savedState.setChildProfileId(100L);
        savedState.setDifficultyLevelId(5L);
        savedState.setRecognitionCategory(es.vargontoc.educational.framework.game.model.enums.RecognitionCategory.COLOR);
        when(gameStateRegistry.findByGameId(startResult.getGameId())).thenReturn(java.util.Optional.of(savedState));

        DifficultyLevel gameReadyLevel = createDifficultyLevel(5L);
        gameReadyLevel.setDifficultyCode(DifficultyCode.MEDIUM);
        when(difficultyLevelUseCase.getGameReadyDifficultyLevel(5L)).thenReturn(gameReadyLevel);

        es.vargontoc.educational.framework.family.model.ChildProfile childProfile =
                new es.vargontoc.educational.framework.family.model.ChildProfile();
        childProfile.setColorVisionMode(es.vargontoc.educational.framework.family.model.ColorVisionMode.DEUTERANOPIA);
        when(childProfileUseCase.getChild(100L)).thenReturn(childProfile);

        when(recognitionDifficultyService.resolveRoundParameters(
                DifficultyCode.MEDIUM,
                es.vargontoc.educational.framework.game.model.enums.RecognitionCategory.COLOR,
                es.vargontoc.educational.framework.family.model.ColorVisionMode.DEUTERANOPIA))
            .thenReturn(new es.vargontoc.educational.framework.game.model.recognition.RoundParameters(
                    3, es.vargontoc.educational.framework.game.model.recognition.DistractorStrategy.SIMILAR_OUTLINE,
                    false, 800, true));

        GameState readyResult = orchestratorService.readyGame(startResult.getGameId());

        assertEquals(GameStatus.IN_PROGRESS, readyResult.getStatus());
        assertNotNull(readyResult.getEnginePayload());
    }

    private GameOrchestratorService orchestratorWithAnimalService() {
        return new GameOrchestratorService(
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
            roundAudioService,
            null,
            AnimalGroupService.fromSeed()
        );
    }

    private RecognitionElement createAnimal(Long id, Long topicId, String code) {
        RecognitionElement element = createElement(id, topicId);
        element.setCode(code);
        return element;
    }

    private GameState startAnimalGame(GameOrchestratorService service, String biome, List<RecognitionElement> elements) {
        Activity activity = createActivity(1L, List.of(20L));
        GameCatalogReadiness readiness = new GameCatalogReadiness(activity, createDifficultyLevel(5L), true);
        Topic animalTopic = createTopic(20L, RecognitionType.ANIMAL);

        when(gameCatalogUseCase.getGameReadiness(100L, 1L)).thenReturn(readiness);
        when(topicUseCase.getTopic(20L)).thenReturn(animalTopic);
        when(filterAllowedRecognitionCategoriesUseCase.filterAllowedCategories(
                eq(100L), ArgumentMatchers.<List<RecognitionCategory>>any()))
                .thenReturn(List.of(RecognitionCategory.ANIMAL));
        // The seeded "Animales" topic has no habitatTag, so the habitat query finds nothing
        when(topicUseCase.listTopicsByRecognitionTypeAndHabitat(eq(RecognitionType.ANIMAL), any(Biome.class)))
                .thenReturn(List.of());
        when(topicUseCase.listTopicsByRecognitionType(RecognitionType.ANIMAL)).thenReturn(List.of(animalTopic));
        when(recognitionElementRepository.findByTopicIdAndStatus(20L, ContentStatus.ACTIVE)).thenReturn(elements);
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));

        return service.startGame(100L, 1L, new LaunchContext(null, biome, null, null));
    }

    @Test
    void startGame_animalWithBiome_keepsOnlyAnimalsValidForThatBiome() {
        List<RecognitionElement> elements = List.of(
                createAnimal(1L, 20L, "bull"),   // MEADOW + FARM
                createAnimal(2L, 20L, "cat"),    // FARM
                createAnimal(3L, 20L, "bee"),    // MEADOW
                createAnimal(4L, 20L, "pig"),    // FARM
                createAnimal(5L, 20L, "frog"));  // MEADOW

        GameState meadow = startAnimalGame(orchestratorWithAnimalService(), "MEADOW", elements);

        assertEquals(List.of("1", "3", "5"), meadow.getCandidates());
    }

    @Test
    void startGame_animalWithBiome_farmExcludesMeadowOnlyAnimals() {
        List<RecognitionElement> elements = List.of(
                createAnimal(1L, 20L, "bull"),
                createAnimal(2L, 20L, "cat"),
                createAnimal(3L, 20L, "bee"),
                createAnimal(4L, 20L, "pig"),
                createAnimal(5L, 20L, "frog"));

        GameState farm = startAnimalGame(orchestratorWithAnimalService(), "FARM", elements);

        assertEquals(List.of("1", "2", "4"), farm.getCandidates());
    }

    @Test
    void startGame_animalWithBiome_tooFewValidAnimals_keepsAllInsteadOfBlockingTheGame() {
        List<RecognitionElement> elements = List.of(
                createAnimal(1L, 20L, "cat"),   // only FARM animal
                createAnimal(2L, 20L, "bee"),
                createAnimal(3L, 20L, "frog"));

        GameState farm = startAnimalGame(orchestratorWithAnimalService(), "FARM", elements);

        assertEquals(3, farm.getCandidates().size());
    }
}
