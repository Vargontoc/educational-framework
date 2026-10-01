package es.vargontoc.educational.framework.game.service;

import es.vargontoc.educational.framework.content.model.Activity;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.DifficultyCode;
import es.vargontoc.educational.framework.content.model.DifficultyLevel;
import es.vargontoc.educational.framework.content.model.GameCatalogReadiness;
import es.vargontoc.educational.framework.content.ports.in.DifficultyLevelUseCase;
import es.vargontoc.educational.framework.content.ports.in.GameCatalogUseCase;
import es.vargontoc.educational.framework.content.ports.in.TopicUseCase;
import es.vargontoc.educational.framework.content.ports.out.RecognitionElementRepository;
import es.vargontoc.educational.framework.family.ports.in.ChildProfileUseCase;
import es.vargontoc.educational.framework.game.model.GameState;
import es.vargontoc.educational.framework.game.model.GameStatus;
import es.vargontoc.educational.framework.game.model.enums.EngineType;
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
import org.springframework.context.ApplicationEventPublisher;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameOrchestratorServiceSprint115Test {

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
    void twoGamesCreatedInSameMillisecond_doNotCollide() {
        Activity activity = createActivity(1L);
        DifficultyLevel dl = createDifficultyLevel(1L);
        GameCatalogReadiness readiness = new GameCatalogReadiness(activity, dl, true);
        when(gameCatalogUseCase.getGameReadiness(any(), any())).thenReturn(readiness);
        doAnswer(invocation -> null).when(gameStateRegistry).save(any(GameState.class));
        lenient().when(recognitionElementRepository.findByTopicIdAndStatus(any(), any())).thenReturn(List.of());
        lenient().when(filterAllowedRecognitionCategoriesUseCase.filterAllowedCategories(any(), any())).thenReturn(List.of());

        Set<Long> generatedIds = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            GameState state = service.startGame(100L, 1L);
            generatedIds.add(state.getGameId());
        }
        assertEquals(100, generatedIds.size(), "All 100 game IDs should be unique");
    }

    @Test
    void gameLocks_returnsToZeroAfterAbandon() {
        GameState state = createMinimalGameState(999L, 1L, GameStatus.IN_PROGRESS);
        when(gameStateRegistry.findByGameId(999L)).thenReturn(Optional.of(state));

        assertEquals(0, service.activeLockCount());

        service.abandonGame(999L);

        assertEquals(0, service.activeLockCount());
    }

    @Test
    void gameLocks_returnsToZeroAfterDiscard() {
        GameState state = createMinimalGameState(888L, 2L, GameStatus.IN_PROGRESS);
        when(gameStateRegistry.findByChildSessionId(2L)).thenReturn(Optional.of(state));
        when(gameStateRegistry.findByGameId(888L)).thenReturn(Optional.of(state));

        assertEquals(0, service.activeLockCount());

        service.discardGameForSession(2L);

        assertEquals(0, service.activeLockCount());
    }

    private GameState createMinimalGameState(Long gameId, Long childSessionId, GameStatus status) {
        GameState state = new GameState();
        state.setGameId(gameId);
        state.setChildSessionId(childSessionId);
        state.setChildProfileId(100L);
        state.setActivityId(1L);
        state.setDifficultyLevelId(1L);
        state.setEngine(EngineType.RECOGNITION);
        state.setStatus(status);
        state.setCandidates(List.of("elem-1"));
        return state;
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
}
