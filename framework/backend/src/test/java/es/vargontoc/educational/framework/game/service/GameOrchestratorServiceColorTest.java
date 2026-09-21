package es.vargontoc.educational.framework.game.service;

import es.vargontoc.educational.framework.content.model.DifficultyCode;
import es.vargontoc.educational.framework.content.model.DifficultyLevel;
import es.vargontoc.educational.framework.content.model.RecognitionElement;
import es.vargontoc.educational.framework.content.ports.in.DifficultyLevelUseCase;
import es.vargontoc.educational.framework.content.ports.in.GameCatalogUseCase;
import es.vargontoc.educational.framework.content.ports.in.TopicUseCase;
import es.vargontoc.educational.framework.content.ports.out.RecognitionElementRepository;
import es.vargontoc.educational.framework.family.model.ChildProfile;
import es.vargontoc.educational.framework.family.model.ColorVisionMode;
import es.vargontoc.educational.framework.family.ports.in.ChildProfileUseCase;
import es.vargontoc.educational.framework.family.service.AdaptativeColorService;
import es.vargontoc.educational.framework.game.application.RecognitionProperties;
import es.vargontoc.educational.framework.game.exception.GameUnavailableException;
import es.vargontoc.educational.framework.game.model.GameState;
import es.vargontoc.educational.framework.game.model.GameStatus;
import es.vargontoc.educational.framework.game.model.enums.EngineType;
import es.vargontoc.educational.framework.game.model.enums.GameUnavailableReason;
import es.vargontoc.educational.framework.game.model.enums.RecognitionCategory;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * SPRINT-106: COLOR minigame availability per colour vision profile and colour-aware round building.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GameOrchestratorServiceColorTest {

    private static final Long GAME_ID = 1L;
    private static final Long CHILD_PROFILE_ID = 200L;
    private static final Long ACTIVITY_ID = 7L;
    private static final Long DIFFICULTY_LEVEL_ID = 5L;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Map<Long, String> COLORS = Map.of(
            1L, "#FF0000", 2L, "#FE0000", 3L, "#0000FF", 4L, "#00FF00", 5L, "#FFFF00");

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

    private final AdaptativeColorService colorService = new AdaptativeColorService();
    private GameOrchestratorService orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new GameOrchestratorService(
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
                new RecognitionDifficultyService(new RecognitionDifficultyConfig(new RecognitionProperties())),
                recognitionSimilarityService,
                roundAudioService,
                new ColorSimilarityValidator(colorService),
                null);

        when(recognitionElementRepository.findAllById(any())).thenAnswer(invocation -> {
            Iterable<Long> ids = invocation.getArgument(0);
            java.util.List<RecognitionElement> elements = new java.util.ArrayList<>();
            ids.forEach(id -> elements.add(colorElement(id)));
            return elements;
        });
    }

    private RecognitionElement colorElement(Long id) {
        RecognitionElement element = new RecognitionElement();
        element.setId(id);
        element.setTopicId(1L);
        element.setCode("color_" + id);
        element.setResourceRefs("{\"nubi-audio\": \"¿Dónde está?\", \"color\": \"" + COLORS.get(id)
                + "\", \"icon\": \"icon" + id + "\"}");
        return element;
    }

    private void profile(ColorVisionMode mode, DifficultyCode difficulty) {
        ChildProfile childProfile = new ChildProfile();
        childProfile.setId(CHILD_PROFILE_ID);
        childProfile.setColorVisionMode(mode);
        when(childProfileUseCase.getChild(CHILD_PROFILE_ID)).thenReturn(childProfile);

        DifficultyLevel level = new DifficultyLevel();
        level.setId(DIFFICULTY_LEVEL_ID);
        level.setDifficultyCode(difficulty);
        when(difficultyLevelUseCase.getGameReadyDifficultyLevel(DIFFICULTY_LEVEL_ID)).thenReturn(level);
    }

    private GameState waitingGame(RecognitionCategory category) {
        GameState state = new GameState();
        state.setGameId(GAME_ID);
        state.setChildSessionId(100L);
        state.setChildProfileId(CHILD_PROFILE_ID);
        state.setActivityId(ACTIVITY_ID);
        state.setDifficultyLevelId(DIFFICULTY_LEVEL_ID);
        state.setEngine(EngineType.RECOGNITION);
        state.setRecognitionCategory(category);
        state.setStatus(GameStatus.WAITING);
        state.setSequenceNumber(0);
        state.setAttempts(0);
        state.setCorrectAttempts(0);
        state.setIncorrectAttempts(0);
        state.setTimeoutAttempts(0);
        state.setCurrentScore(BigDecimal.ZERO);
        state.setCurrentStreak(0);
        state.setStarsEarned(0);
        state.setCandidates(List.of("1", "2", "3", "4", "5"));
        when(gameStateRegistry.findByGameId(GAME_ID)).thenReturn(Optional.of(state));
        return state;
    }

    private RecognitionState recognitionStateOf(GameState state) throws Exception {
        return MAPPER.readValue(state.getEnginePayload(), RecognitionState.class);
    }

    // ---- achromatic profiles: the COLOR minigame is not available

    @Test
    void readyGame_colorWithAchromatopsia_isUnavailableAndDiscardsTheGame() {
        profile(ColorVisionMode.ACHROMATOPSIA, DifficultyCode.EASY);
        GameState state = waitingGame(RecognitionCategory.COLOR);

        GameUnavailableException e = assertThrows(GameUnavailableException.class, () -> orchestrator.readyGame(GAME_ID));

        assertEquals(GameUnavailableReason.COLOR_VISION_ACHROMATIC, e.getReason());
        assertEquals(GAME_ID, e.getGameId());
        assertEquals(ACTIVITY_ID, e.getActivityId());
        verify(gameStateRegistry).remove(GAME_ID);
        verify(gameStateRegistry, never()).save(any());
        assertEquals(GameStatus.WAITING, state.getStatus(), "the game must not transition");
        assertNull(state.getEnginePayload(), "RecognitionEngine must not be initialised");
        verifyNoInteractions(roundAudioService);
    }

    @Test
    void readyGame_colorWithAchromatomaly_isUnavailable() {
        profile(ColorVisionMode.ACHROMATOMALY, DifficultyCode.EASY);
        waitingGame(RecognitionCategory.COLOR);

        assertThrows(GameUnavailableException.class, () -> orchestrator.readyGame(GAME_ID));
        verify(gameStateRegistry).remove(GAME_ID);
    }

    @Test
    void readyGame_unavailableGame_isNotRecordedAsAbandonedOrSummarised() {
        profile(ColorVisionMode.ACHROMATOPSIA, DifficultyCode.EASY);
        waitingGame(RecognitionCategory.COLOR);

        assertThrows(GameUnavailableException.class, () -> orchestrator.readyGame(GAME_ID));

        verifyNoInteractions(registerGameSessionSummaryUseCase);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void readyGame_letterWithAchromatopsia_isStillAvailable() throws Exception {
        profile(ColorVisionMode.ACHROMATOPSIA, DifficultyCode.EASY);
        waitingGame(RecognitionCategory.LETTER);

        GameState result = orchestrator.readyGame(GAME_ID);

        assertEquals(GameStatus.IN_PROGRESS, result.getStatus());
        verify(gameStateRegistry, never()).remove(GAME_ID);
    }

    // ---- round audio is generated after the game is ready, not before

    @Test
    void readyGame_withoutRoundAudio_doesNotCallTts_andAttachRoundAudioDoes() throws Exception {
        profile(ColorVisionMode.NONE, DifficultyCode.EASY);
        waitingGame(RecognitionCategory.LETTER);
        RoundAudioResult audio = RoundAudioResult.withAudio("audio-1", new byte[] {1, 2, 3}, "¿Dónde está?");
        when(roundAudioService.generateRoundAudio(org.mockito.ArgumentMatchers.eq(CHILD_PROFILE_ID), any())).thenReturn(audio);

        GameState ready = orchestrator.readyGame(GAME_ID, false);

        assertEquals(GameStatus.IN_PROGRESS, ready.getStatus());
        assertNull(ready.getRoundAudioResult());
        verifyNoInteractions(roundAudioService);

        GameState withAudio = orchestrator.attachRoundAudio(GAME_ID);

        assertEquals("audio-1", withAudio.getRoundAudioResult().audioId());
    }

    @Test
    void readyGame_default_stillGeneratesTheRoundAudio() throws Exception {
        profile(ColorVisionMode.NONE, DifficultyCode.EASY);
        waitingGame(RecognitionCategory.LETTER);
        RoundAudioResult audio = RoundAudioResult.withAudio("audio-2", new byte[] {1}, "texto");
        when(roundAudioService.generateRoundAudio(org.mockito.ArgumentMatchers.eq(CHILD_PROFILE_ID), any())).thenReturn(audio);

        assertEquals("audio-2", orchestrator.readyGame(GAME_ID).getRoundAudioResult().audioId());
    }

    // ---- other profiles: the game is available and rounds are colour-aware

    @Test
    void readyGame_colorWithNormalVision_isAvailable() throws Exception {
        profile(ColorVisionMode.NONE, DifficultyCode.EASY);
        waitingGame(RecognitionCategory.COLOR);

        GameState result = orchestrator.readyGame(GAME_ID);

        assertEquals(GameStatus.IN_PROGRESS, result.getStatus());
        assertEquals(RecognitionCategory.COLOR, recognitionStateOf(result).getRecognitionCategory());
    }

    @Test
    void readyGame_colorWithColorBlindProfile_isStillAvailable() throws Exception {
        profile(ColorVisionMode.PROTANOPIA, DifficultyCode.EASY);
        waitingGame(RecognitionCategory.COLOR);

        assertEquals(GameStatus.IN_PROGRESS, orchestrator.readyGame(GAME_ID).getStatus());
    }

    @Test
    void readyGame_colorEasy_showsIconAndOffersTwoDissimilarColors() throws Exception {
        profile(ColorVisionMode.NONE, DifficultyCode.EASY);
        waitingGame(RecognitionCategory.COLOR);

        RecognitionState state = recognitionStateOf(orchestrator.readyGame(GAME_ID));

        assertTrue(state.isShowIcon());
        assertEquals(2, state.getOptionIds().size());
        assertNoSimilarPair(state.getOptionIds(), ColorVisionMode.NONE);
    }

    @Test
    void readyGame_colorMedium_showsIconAndOffersThreeDissimilarColors() throws Exception {
        profile(ColorVisionMode.NONE, DifficultyCode.MEDIUM);
        waitingGame(RecognitionCategory.COLOR);

        RecognitionState state = recognitionStateOf(orchestrator.readyGame(GAME_ID));

        assertTrue(state.isShowIcon());
        assertEquals(3, state.getOptionIds().size());
        assertNoSimilarPair(state.getOptionIds(), ColorVisionMode.NONE);
    }

    @Test
    void readyGame_colorHard_doesNotShowIcon() throws Exception {
        profile(ColorVisionMode.NONE, DifficultyCode.HARD);
        waitingGame(RecognitionCategory.COLOR);

        RecognitionState state = recognitionStateOf(orchestrator.readyGame(GAME_ID));

        assertFalse(state.isShowIcon());
        assertNoSimilarPair(state.getOptionIds(), ColorVisionMode.NONE);
    }

    @Test
    void readyGame_colorRoundsAreValidatedForTheChildVisionMode_acrossManyGames() throws Exception {
        profile(ColorVisionMode.PROTANOPIA, DifficultyCode.MEDIUM);

        for (int i = 0; i < 25; i++) {
            waitingGame(RecognitionCategory.COLOR);
            RecognitionState state = recognitionStateOf(orchestrator.readyGame(GAME_ID));
            assertNoSimilarPair(state.getOptionIds(), ColorVisionMode.PROTANOPIA);
        }
    }

    private void assertNoSimilarPair(List<String> optionIds, ColorVisionMode mode) {
        for (int i = 0; i < optionIds.size(); i++) {
            for (int j = i + 1; j < optionIds.size(); j++) {
                String a = COLORS.get(Long.valueOf(optionIds.get(i)));
                String b = COLORS.get(Long.valueOf(optionIds.get(j)));
                assertFalse(colorService.isTooSimilar(mode, a, b),
                        "options " + optionIds + " contain similar colors " + a + " / " + b + " for " + mode);
            }
        }
    }
}
