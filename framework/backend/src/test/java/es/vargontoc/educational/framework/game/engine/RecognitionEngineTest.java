package es.vargontoc.educational.framework.game.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import es.vargontoc.educational.framework.game.model.ActionResult;
import es.vargontoc.educational.framework.game.model.ActionResultType;
import es.vargontoc.educational.framework.game.model.GameState;
import es.vargontoc.educational.framework.game.model.GameStatus;
import es.vargontoc.educational.framework.game.model.enums.EngineType;
import es.vargontoc.educational.framework.game.model.recognition.RecognitionDefaults;
import es.vargontoc.educational.framework.game.model.recognition.RecognitionState;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecognitionEngineTest {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private GameState createGameState() {
        GameState gs = new GameState();
        gs.setGameId(1L);
        gs.setChildSessionId(10L);
        gs.setActivityId(100L);
        return gs;
    }

    private String buildEngineParams(List<String> candidates) {
        try {
            return MAPPER.writeValueAsString(java.util.Map.of("candidates", candidates));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private RecognitionState deserializeState(String payload) throws Exception {
        return MAPPER.readValue(payload, new TypeReference<RecognitionState>() {});
    }

    @Test
    void initGame_setsGameStateFieldsAndCreatesValidFirstRound() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(42));
        GameState gs = createGameState();
        List<String> candidates = List.of("elem-1", "elem-2", "elem-3", "elem-4", "elem-5", "elem-6");

        engine.initGame(gs, buildEngineParams(candidates));

        assertEquals(GameStatus.IN_PROGRESS, gs.getStatus());
        assertEquals(0, gs.getAttempts());
        assertEquals(0, gs.getCorrectAttempts());
        assertEquals(0, gs.getIncorrectAttempts());
        assertEquals(0, gs.getTimeoutAttempts());
        assertEquals(0, gs.getCurrentStreak());
        assertEquals(0, gs.getStarsEarned());
        assertEquals(0, gs.getSequenceNumber());
        assertFalse(gs.isSystemEventPending());
        assertNotNull(gs.getStartedAt());
        assertEquals(EngineType.RECOGNITION, gs.getEngine());
        assertNotNull(gs.getEnginePayload());

        RecognitionState state = deserializeState(gs.getEnginePayload());
        assertEquals(0, state.getRoundIndex());
        assertEquals(RecognitionDefaults.DEFAULT_TOTAL_ROUNDS, state.getTotalRounds());
        assertEquals(RecognitionDefaults.DEFAULT_DIFFICULTY_LEVEL, state.getCurrentDifficultyLevel());
        assertNotNull(state.getTargetElementId());
        assertNotNull(state.getOptionIds());
        assertFalse(state.getOptionIds().isEmpty());
        assertTrue(candidates.contains(state.getTargetElementId()));
        assertEquals(1, state.getRoundsShownElementIds().size());
        assertEquals(state.getTargetElementId(), state.getRoundsShownElementIds().get(0));
        assertNotNull(state.getRoundStartedAt());
    }

    @Test
    void initGame_optionsIncludeTargetExactlyOnce() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(99));
        GameState gs = createGameState();
        List<String> candidates = List.of("a", "b", "c", "d", "e");

        engine.initGame(gs, buildEngineParams(candidates));

        RecognitionState state = deserializeState(gs.getEnginePayload());
        String target = state.getTargetElementId();
        long targetCount = state.getOptionIds().stream().filter(target::equals).count();
        assertEquals(1, targetCount, "Target must appear exactly once in options");
    }

    @Test
    void initGame_optionCountIs2To3WhenEnoughCandidates() throws Exception {
        for (int seed = 0; seed < 20; seed++) {
            RecognitionEngine engine = new RecognitionEngine(new Random(seed));
            GameState gs = createGameState();
            List<String> candidates = List.of("c1", "c2", "c3", "c4", "c5");

            engine.initGame(gs, buildEngineParams(candidates));

            RecognitionState state = deserializeState(gs.getEnginePayload());
            int optionCount = state.getOptionIds().size();
            assertTrue(optionCount >= RecognitionDefaults.MIN_OPTIONS_PER_ROUND
                    && optionCount <= RecognitionDefaults.MAX_OPTIONS_PER_ROUND,
                    "Option count must be 2-3, was: " + optionCount);
        }
    }

    @Test
    void initGame_optionCountAdaptsWhenFewCandidates() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(0));
        GameState gs = createGameState();
        List<String> candidates = List.of("only-one");

        engine.initGame(gs, buildEngineParams(candidates));

        RecognitionState state = deserializeState(gs.getEnginePayload());
        assertEquals(1, state.getOptionIds().size());
        assertEquals("only-one", state.getTargetElementId());
        assertEquals("only-one", state.getOptionIds().get(0));
    }

    @Test
    void initGame_twoCandidatesProducesTwoOptions() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(0));
        GameState gs = createGameState();
        List<String> candidates = List.of("x", "y");

        engine.initGame(gs, buildEngineParams(candidates));

        RecognitionState state = deserializeState(gs.getEnginePayload());
        assertEquals(2, state.getOptionIds().size());
        assertTrue(state.getOptionIds().contains("x"));
        assertTrue(state.getOptionIds().contains("y"));
    }

    @Test
    void selectTarget_avoidsAlreadyShownElementsWhenPossible() {
        RecognitionEngine engine = new RecognitionEngine(new Random(42));
        List<String> candidates = List.of("a", "b", "c", "d", "e");
        List<String> shown = new ArrayList<>(List.of("a", "b"));

        for (int i = 0; i < 50; i++) {
            String target = engine.selectTarget(candidates, shown);
            assertFalse(shown.contains(target),
                    "Target should avoid shown elements when unshown candidates exist");
        }
    }

    @Test
    void selectTarget_allowsRepetitionWhenAllCandidatesShown() {
        RecognitionEngine engine = new RecognitionEngine(new Random(7));
        List<String> candidates = List.of("a", "b");
        List<String> shown = new ArrayList<>(List.of("a", "b"));

        boolean sawA = false;
        boolean sawB = false;
        for (int i = 0; i < 100; i++) {
            String target = engine.selectTarget(candidates, shown);
            assertNotNull(target);
            assertTrue(candidates.contains(target));
            if ("a".equals(target)) sawA = true;
            if ("b".equals(target)) sawB = true;
        }
        assertTrue(sawA && sawB, "Should be able to pick any candidate when all are shown");
    }

    @Test
    void selectTarget_returnsNullForEmptyCandidates() {
        RecognitionEngine engine = new RecognitionEngine(new Random(0));
        assertNull(engine.selectTarget(List.of(), List.of()));
    }

    @Test
    void selectTarget_returnsSoleCandidate() {
        RecognitionEngine engine = new RecognitionEngine(new Random(0));
        assertEquals("solo", engine.selectTarget(List.of("solo"), List.of()));
    }

    @Test
    void initGame_emptyCandidatesProducesStateWithNullTarget() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(0));
        GameState gs = createGameState();

        engine.initGame(gs, buildEngineParams(List.of()));

        RecognitionState state = deserializeState(gs.getEnginePayload());
        assertNull(state.getTargetElementId());
        assertTrue(state.getOptionIds().isEmpty());
    }

    @Test
    void initGame_nullEngineParamsProducesStateWithNullTarget() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(0));
        GameState gs = createGameState();

        engine.initGame(gs, null);

        RecognitionState state = deserializeState(gs.getEnginePayload());
        assertNull(state.getTargetElementId());
        assertTrue(state.getOptionIds().isEmpty());
    }

    @Test
    void engineHasNoDependenciesOnWorldTrackingContentOrSession() {
        Class<?> engineClass = RecognitionEngine.class;
        var constructors = engineClass.getConstructors();
        for (var ctor : constructors) {
            for (var paramType : ctor.getParameterTypes()) {
                String name = paramType.getName();
                assertFalse(name.contains("world"), "Engine should not depend on world classes");
                assertFalse(name.contains("tracking"), "Engine should not depend on tracking classes");
                assertFalse(name.contains("content"), "Engine should not depend on content classes");
                assertFalse(name.contains("session"), "Engine should not depend on session classes");
            }
        }

        var fields = engineClass.getDeclaredFields();
        for (var field : fields) {
            String typeName = field.getType().getName();
            assertFalse(typeName.contains("world"), "Engine should not reference world classes");
            assertFalse(typeName.contains("tracking"), "Engine should not reference tracking classes");
            assertFalse(typeName.contains("content"), "Engine should not reference content classes");
            assertFalse(typeName.contains("session"), "Engine should not reference session classes");
        }
    }

    @Test
    void initGame_allOptionsAreFromCandidates() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(123));
        GameState gs = createGameState();
        List<String> candidates = List.of("e1", "e2", "e3", "e4", "e5", "e6", "e7");

        engine.initGame(gs, buildEngineParams(candidates));

        RecognitionState state = deserializeState(gs.getEnginePayload());
        for (String option : state.getOptionIds()) {
            assertTrue(candidates.contains(option),
                    "Option '" + option + "' must come from candidates");
        }
    }

    @Test
    void initGame_roundsShownContainsTargetAfterInit() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(55));
        GameState gs = createGameState();
        List<String> candidates = List.of("r1", "r2", "r3", "r4", "r5");

        engine.initGame(gs, buildEngineParams(candidates));

        RecognitionState state = deserializeState(gs.getEnginePayload());
        assertEquals(1, state.getRoundsShownElementIds().size());
        assertEquals(state.getTargetElementId(), state.getRoundsShownElementIds().get(0));
    }

    private String buildActionPayload(String selectedOptionId, Integer responseTimeMs) {
        try {
            var map = new java.util.LinkedHashMap<String, Object>();
            map.put("selectedOptionId", selectedOptionId);
            if (responseTimeMs != null) {
                map.put("responseTimeMs", responseTimeMs);
            }
            return MAPPER.writeValueAsString(map);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private GameState initEngineWithKnownTarget() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(42));
        GameState gs = createGameState();
        List<String> candidates = List.of("elem-1", "elem-2", "elem-3");
        engine.initGame(gs, buildEngineParams(candidates));
        return gs;
    }

    private String findWrongOption(RecognitionState state) {
        for (String opt : state.getOptionIds()) {
            if (!opt.equals(state.getTargetElementId())) {
                return opt;
            }
        }
        return null;
    }

    @Test
    void processAction_incorrectAnswerKeepsSameRoundOpen() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(42));
        GameState gs = initEngineWithKnownTarget();
        RecognitionState stateBefore = deserializeState(gs.getEnginePayload());
        String target = stateBefore.getTargetElementId();
        List<String> optionsBefore = new ArrayList<>(stateBefore.getOptionIds());
        String wrong = findWrongOption(stateBefore);

        ActionResult result = engine.processAction(gs, buildActionPayload(wrong, 2000));

        assertEquals(ActionResultType.INCORRECT, result.getResultType());
        RecognitionState stateAfter = deserializeState(gs.getEnginePayload());
        assertEquals(target, stateAfter.getTargetElementId(),
                "Target must remain the same after incorrect answer");
        assertEquals(optionsBefore, stateAfter.getOptionIds(),
                "Options must remain the same after incorrect answer");
        assertEquals(1, stateAfter.getCurrentRoundAttemptCount());
        assertEquals(1, stateAfter.getCurrentRoundConsecutiveFailures());
        assertEquals(1, stateAfter.getTotalIncorrectAttempts());
        assertEquals(wrong, stateAfter.getSelectedOptionId());
        assertNotNull(stateAfter.getLastActionAt());
    }

    @Test
    void processAction_firstIncorrectDoesNotActivateHint() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(42));
        GameState gs = initEngineWithKnownTarget();
        RecognitionState stateBefore = deserializeState(gs.getEnginePayload());
        String wrong = findWrongOption(stateBefore);

        engine.processAction(gs, buildActionPayload(wrong, 1000));

        RecognitionState stateAfter = deserializeState(gs.getEnginePayload());
        assertFalse(stateAfter.isHintActive(),
                "Hint must not be active after first incorrect answer");
        assertNull(stateAfter.getHintTriggeredAtAttempt());
    }

    @Test
    void processAction_secondConsecutiveIncorrectActivatesHint() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(42));
        GameState gs = initEngineWithKnownTarget();
        RecognitionState stateBefore = deserializeState(gs.getEnginePayload());
        String wrong = findWrongOption(stateBefore);

        engine.processAction(gs, buildActionPayload(wrong, 1000));
        engine.processAction(gs, buildActionPayload(wrong, 1500));

        RecognitionState stateAfter = deserializeState(gs.getEnginePayload());
        assertTrue(stateAfter.isHintActive(),
                "Hint must be active after 2 consecutive failures");
        assertEquals(2, stateAfter.getHintTriggeredAtAttempt());
        assertEquals(2, stateAfter.getCurrentRoundAttemptCount());
        assertEquals(2, stateAfter.getCurrentRoundConsecutiveFailures());
        assertEquals(2, stateAfter.getTotalIncorrectAttempts());
    }

    @Test
    void processAction_correctAfterFailuresReturnsCorrectAndAdvancesRound() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(42));
        GameState gs = initEngineWithKnownTarget();
        RecognitionState stateBefore = deserializeState(gs.getEnginePayload());
        String target = stateBefore.getTargetElementId();
        String wrong = findWrongOption(stateBefore);

        engine.processAction(gs, buildActionPayload(wrong, 1000));
        engine.processAction(gs, buildActionPayload(wrong, 1500));

        ActionResult result = engine.processAction(gs, buildActionPayload(target, 3000));

        assertEquals(ActionResultType.CORRECT, result.getResultType());
        RecognitionState stateAfter = deserializeState(gs.getEnginePayload());
        assertEquals(1, stateAfter.getRoundIndex(), "Round must advance after correct answer");
        assertEquals(0, stateAfter.getCurrentRoundAttemptCount(),
                "Per-round attempt count must reset after advancement");
        assertEquals(0, stateAfter.getCurrentRoundConsecutiveFailures(),
                "Consecutive failures must reset to 0 on correct answer");
        assertEquals(2, stateAfter.getTotalIncorrectAttempts(),
                "Total incorrect must preserve previous failures");
        assertEquals(0, stateAfter.getTotalCorrectFirstTry(),
                "Not a first-try correct, so totalCorrectFirstTry stays 0");
        assertNull(stateAfter.getSelectedOptionId(),
                "selectedOptionId must reset after advancement");
        assertFalse(stateAfter.isHintActive(),
                "Hint must reset after advancement");
        assertNull(stateAfter.getHintTriggeredAtAttempt(),
                "hintTriggeredAtAttempt must reset after advancement");
        assertNotNull(result.getNewState());
        assertNotNull(result.getAttemptContext());
        assertEquals(3000, result.getResponseTimeMs());
    }

    @Test
    void processAction_correctFirstTryIncrementsTotalCorrectFirstTryAndAdvancesRound() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(42));
        GameState gs = initEngineWithKnownTarget();
        RecognitionState stateBefore = deserializeState(gs.getEnginePayload());
        String target = stateBefore.getTargetElementId();

        ActionResult result = engine.processAction(gs, buildActionPayload(target, 500));

        assertEquals(ActionResultType.CORRECT, result.getResultType());
        RecognitionState stateAfter = deserializeState(gs.getEnginePayload());
        assertEquals(1, stateAfter.getTotalCorrectFirstTry());
        assertEquals(1, stateAfter.getRoundIndex(), "Round must advance after correct answer");
        assertEquals(0, stateAfter.getCurrentRoundAttemptCount(),
                "Per-round attempt count must reset after advancement");
        assertEquals(0, stateAfter.getCurrentRoundConsecutiveFailures());
        assertEquals(0, stateAfter.getTotalIncorrectAttempts());
        assertFalse(stateAfter.isHintActive());
    }

    @Test
    void processAction_neverReturnsTimeout() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(42));
        GameState gs = initEngineWithKnownTarget();
        RecognitionState stateBefore = deserializeState(gs.getEnginePayload());
        String target = stateBefore.getTargetElementId();
        String wrong = findWrongOption(stateBefore);

        ActionResult correctResult = engine.processAction(gs, buildActionPayload(target, 100));
        assertNotEquals(ActionResultType.TIMEOUT, correctResult.getResultType(),
                "RecognitionEngine must never return TIMEOUT");

        GameState gs2 = initEngineWithKnownTarget();
        ActionResult incorrectResult = engine.processAction(gs2, buildActionPayload(wrong, 100));
        assertNotEquals(ActionResultType.TIMEOUT, incorrectResult.getResultType(),
                "RecognitionEngine must never return TIMEOUT");

        GameState gs3 = initEngineWithKnownTarget();
        ActionResult nullPayloadResult = engine.processAction(gs3, null);
        assertNotEquals(ActionResultType.TIMEOUT, nullPayloadResult.getResultType(),
                "RecognitionEngine must never return TIMEOUT even with null payload");
    }

    @Test
    void processAction_responseTimeMsIsPropagated() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(42));
        GameState gs = initEngineWithKnownTarget();
        RecognitionState stateBefore = deserializeState(gs.getEnginePayload());
        String target = stateBefore.getTargetElementId();

        ActionResult result = engine.processAction(gs, buildActionPayload(target, 3500));

        assertEquals(3500, result.getResponseTimeMs());
    }

    @Test
    void processAction_nullPayloadReturnsIncorrect() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(42));
        GameState gs = initEngineWithKnownTarget();

        ActionResult result = engine.processAction(gs, null);

        assertEquals(ActionResultType.INCORRECT, result.getResultType());
        RecognitionState stateAfter = deserializeState(gs.getEnginePayload());
        assertEquals(1, stateAfter.getCurrentRoundAttemptCount());
        assertEquals(1, stateAfter.getCurrentRoundConsecutiveFailures());
        assertNull(stateAfter.getSelectedOptionId());
    }

    @Test
    void processAction_hintStaysActiveAfterActivation() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(42));
        GameState gs = initEngineWithKnownTarget();
        RecognitionState stateBefore = deserializeState(gs.getEnginePayload());
        String target = stateBefore.getTargetElementId();
        String wrong = findWrongOption(stateBefore);

        engine.processAction(gs, buildActionPayload(wrong, 1000));
        engine.processAction(gs, buildActionPayload(wrong, 1000));
        engine.processAction(gs, buildActionPayload(wrong, 1000));

        RecognitionState stateAfter = deserializeState(gs.getEnginePayload());
        assertTrue(stateAfter.isHintActive());
        assertEquals(2, stateAfter.getHintTriggeredAtAttempt(),
                "hintTriggeredAtAttempt must remain at the first activation");
        assertEquals(3, stateAfter.getCurrentRoundConsecutiveFailures());
    }

    private GameState initEngineWithCandidates(int seed, List<String> candidates) throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(seed));
        GameState gs = createGameState();
        engine.initGame(gs, buildEngineParams(candidates));
        return gs;
    }

    private void answerCorrectlyForRound(RecognitionEngine engine, GameState gs) throws Exception {
        RecognitionState state = deserializeState(gs.getEnginePayload());
        String target = state.getTargetElementId();
        engine.processAction(gs, buildActionPayload(target, 1000));
    }

    @Test
    void correctAnswersAdvanceRoundsUntilCompletion() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(42));
        List<String> candidates = List.of("a", "b", "c", "d", "e", "f", "g");
        GameState gs = createGameState();
        engine.initGame(gs, buildEngineParams(candidates));

        for (int i = 0; i < RecognitionDefaults.DEFAULT_TOTAL_ROUNDS; i++) {
            assertFalse(engine.isGameComplete(gs),
                    "Game should not be complete before round " + (i + 1));
            answerCorrectlyForRound(engine, gs);
        }

        assertTrue(engine.isGameComplete(gs),
                "Game should be complete after all rounds answered correctly");

        RecognitionState finalState = deserializeState(gs.getEnginePayload());
        assertEquals(RecognitionDefaults.DEFAULT_TOTAL_ROUNDS, finalState.getRoundIndex());
        assertEquals(RecognitionDefaults.DEFAULT_TOTAL_ROUNDS, finalState.getTotalCorrectFirstTry());
        assertEquals(0, finalState.getTotalIncorrectAttempts());
    }

    @Test
    void incorrectAnswersDoNotAdvanceRound() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(42));
        List<String> candidates = List.of("a", "b", "c", "d", "e", "f", "g");
        GameState gs = createGameState();
        engine.initGame(gs, buildEngineParams(candidates));

        RecognitionState stateBefore = deserializeState(gs.getEnginePayload());
        String target = stateBefore.getTargetElementId();
        String wrong = findWrongOption(stateBefore);

        engine.processAction(gs, buildActionPayload(wrong, 1000));
        engine.processAction(gs, buildActionPayload(wrong, 1500));

        RecognitionState stateAfterIncorrect = deserializeState(gs.getEnginePayload());
        assertEquals(0, stateAfterIncorrect.getRoundIndex(),
                "Round must not advance after incorrect answers");
        assertEquals(target, stateAfterIncorrect.getTargetElementId(),
                "Target must remain the same after incorrect answers");

        engine.processAction(gs, buildActionPayload(target, 2000));

        RecognitionState stateAfterCorrect = deserializeState(gs.getEnginePayload());
        assertEquals(1, stateAfterCorrect.getRoundIndex(),
                "Round must advance only after correct answer");
    }

    @Test
    void threeStarScoring_firstTryAndGoodResponseTime() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(42));
        List<String> candidates = List.of("a", "b", "c", "d", "e", "f", "g");
        GameState gs = createGameState();
        engine.initGame(gs, buildEngineParams(candidates));

        for (int i = 0; i < RecognitionDefaults.DEFAULT_TOTAL_ROUNDS; i++) {
            RecognitionState state = deserializeState(gs.getEnginePayload());
            String target = state.getTargetElementId();
            engine.processAction(gs, buildActionPayload(target, 2000));
        }

        assertTrue(engine.isGameComplete(gs));
        ActionResult summary = engine.buildSummary(gs);

        assertEquals(ActionResultType.CORRECT, summary.getResultType());
        assertTrue(summary.isCompleted());
        assertEquals(GameStatus.COMPLETED, summary.getNewState().getStatus());
        assertEquals(3, summary.getNewState().getStarsEarned());

        RecognitionState finalState = deserializeState(gs.getEnginePayload());
        assertEquals(5, finalState.getTotalCorrectFirstTry());
        assertEquals(0, finalState.getTotalIncorrectAttempts());
    }

    @Test
    void twoStarScoring_completedWithAvgAttemptsAtMostTwo() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(42));
        List<String> candidates = List.of("a", "b", "c", "d", "e", "f", "g");
        GameState gs = createGameState();
        engine.initGame(gs, buildEngineParams(candidates));

        for (int i = 0; i < RecognitionDefaults.DEFAULT_TOTAL_ROUNDS; i++) {
            RecognitionState state = deserializeState(gs.getEnginePayload());
            String target = state.getTargetElementId();
            String wrong = findWrongOption(state);
            engine.processAction(gs, buildActionPayload(wrong, 1000));
            engine.processAction(gs, buildActionPayload(target, 8000));
        }

        assertTrue(engine.isGameComplete(gs));
        ActionResult summary = engine.buildSummary(gs);

        assertEquals(GameStatus.COMPLETED, summary.getNewState().getStatus());
        int stars = summary.getNewState().getStarsEarned();
        assertTrue(stars == 2 || stars == 1,
                "Expected 2 or fewer stars when each round has 2 attempts, got: " + stars);

        RecognitionState finalState = deserializeState(gs.getEnginePayload());
        assertEquals(0, finalState.getTotalCorrectFirstTry(),
                "No round was first-try correct");
        assertEquals(5, finalState.getTotalIncorrectAttempts());
    }

    @Test
    void oneStarMinimum_manyRetriesStillCompletes() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(42));
        List<String> candidates = List.of("a", "b", "c", "d", "e", "f", "g");
        GameState gs = createGameState();
        engine.initGame(gs, buildEngineParams(candidates));

        for (int i = 0; i < RecognitionDefaults.DEFAULT_TOTAL_ROUNDS; i++) {
            RecognitionState state = deserializeState(gs.getEnginePayload());
            String target = state.getTargetElementId();
            String wrong = findWrongOption(state);
            engine.processAction(gs, buildActionPayload(wrong, 1000));
            engine.processAction(gs, buildActionPayload(wrong, 1000));
            engine.processAction(gs, buildActionPayload(wrong, 1000));
            engine.processAction(gs, buildActionPayload(target, 9000));
        }

        assertTrue(engine.isGameComplete(gs));
        ActionResult summary = engine.buildSummary(gs);

        assertEquals(GameStatus.COMPLETED, summary.getNewState().getStatus());
        assertEquals(ActionResultType.CORRECT, summary.getResultType());
        assertTrue(summary.isCompleted());
        int stars = summary.getNewState().getStarsEarned();
        assertTrue(stars >= 1, "Minimum 1 star guaranteed for completed game");

        RecognitionState finalState = deserializeState(gs.getEnginePayload());
        assertEquals(15, finalState.getTotalIncorrectAttempts());
    }

    @Test
    void buildSummary_neverProducesFailedGameState() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(42));
        List<String> candidates = List.of("a", "b", "c", "d", "e", "f", "g");
        GameState gs = createGameState();
        engine.initGame(gs, buildEngineParams(candidates));

        for (int i = 0; i < RecognitionDefaults.DEFAULT_TOTAL_ROUNDS; i++) {
            RecognitionState state = deserializeState(gs.getEnginePayload());
            String target = state.getTargetElementId();
            String wrong = findWrongOption(state);
            engine.processAction(gs, buildActionPayload(wrong, 1000));
            engine.processAction(gs, buildActionPayload(wrong, 1000));
            engine.processAction(gs, buildActionPayload(wrong, 1000));
            engine.processAction(gs, buildActionPayload(wrong, 1000));
            engine.processAction(gs, buildActionPayload(target, 9000));
        }

        ActionResult summary = engine.buildSummary(gs);

        assertNotEquals(GameStatus.ABANDONED, summary.getNewState().getStatus(),
                "Completed recognition game must not be ABANDONED");
        assertEquals(GameStatus.COMPLETED, summary.getNewState().getStatus(),
                "Completed recognition game must be COMPLETED");
        assertEquals(ActionResultType.CORRECT, summary.getResultType(),
                "buildSummary must return CORRECT, not a failure result");
        assertTrue(summary.getNewState().getStarsEarned() >= 1,
                "At least 1 star must be awarded");
    }

    @Test
    void isGameComplete_falseDuringProgress() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(42));
        List<String> candidates = List.of("a", "b", "c", "d", "e", "f", "g");
        GameState gs = createGameState();
        engine.initGame(gs, buildEngineParams(candidates));

        assertFalse(engine.isGameComplete(gs));
    }

    @Test
    void getNextElement_returnsCurrentRoundInfo() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(42));
        List<String> candidates = List.of("a", "b", "c", "d", "e", "f", "g");
        GameState gs = createGameState();
        engine.initGame(gs, buildEngineParams(candidates));

        String nextElement = engine.getNextElement(gs);
        assertNotNull(nextElement);

        var node = MAPPER.readTree(nextElement);
        assertNotNull(node.get("targetElementId"));
        assertNotNull(node.get("optionIds"));
        assertEquals(0, node.get("roundIndex").asInt());
    }

    @Test
    void getNextElement_returnsNullWhenGameComplete() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(42));
        List<String> candidates = List.of("a", "b", "c", "d", "e", "f", "g");
        GameState gs = createGameState();
        engine.initGame(gs, buildEngineParams(candidates));

        for (int i = 0; i < RecognitionDefaults.DEFAULT_TOTAL_ROUNDS; i++) {
            answerCorrectlyForRound(engine, gs);
        }

        assertNull(engine.getNextElement(gs));
    }

    @Test
    void processAction_accumulatesTotalResponseTime() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(42));
        List<String> candidates = List.of("a", "b", "c", "d", "e", "f", "g");
        GameState gs = createGameState();
        engine.initGame(gs, buildEngineParams(candidates));

        RecognitionState stateBefore = deserializeState(gs.getEnginePayload());
        String target = stateBefore.getTargetElementId();
        String wrong = findWrongOption(stateBefore);

        engine.processAction(gs, buildActionPayload(wrong, 1500));
        engine.processAction(gs, buildActionPayload(target, 2500));

        RecognitionState stateAfter = deserializeState(gs.getEnginePayload());
        assertEquals(4000L, stateAfter.getTotalResponseTimeMs(),
                "totalResponseTimeMs must accumulate across actions");
    }

    @Test
    void processAction_afterGameCompleteReturnsCompletedResult() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(42));
        List<String> candidates = List.of("a", "b", "c", "d", "e", "f", "g");
        GameState gs = createGameState();
        engine.initGame(gs, buildEngineParams(candidates));

        for (int i = 0; i < RecognitionDefaults.DEFAULT_TOTAL_ROUNDS; i++) {
            answerCorrectlyForRound(engine, gs);
        }

        ActionResult result = engine.processAction(gs, buildActionPayload("a", 1000));
        assertTrue(result.isCompleted(),
                "Actions after game complete should return completed result");
    }

    @Test
    void processAction_completedFlagSetOnLastCorrectAnswer() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(42));
        List<String> candidates = List.of("a", "b", "c", "d", "e", "f", "g");
        GameState gs = createGameState();
        engine.initGame(gs, buildEngineParams(candidates));

        for (int i = 0; i < RecognitionDefaults.DEFAULT_TOTAL_ROUNDS - 1; i++) {
            ActionResult result = answerCorrectlyForRoundAndReturn(engine, gs);
            assertFalse(result.isCompleted(),
                    "Game should not be completed before last round");
        }

        ActionResult lastResult = answerCorrectlyForRoundAndReturn(engine, gs);
        assertTrue(lastResult.isCompleted(),
                "Game should be completed after last correct answer");
    }

    private ActionResult answerCorrectlyForRoundAndReturn(RecognitionEngine engine, GameState gs) throws Exception {
        RecognitionState state = deserializeState(gs.getEnginePayload());
        String target = state.getTargetElementId();
        return engine.processAction(gs, buildActionPayload(target, 1000));
    }

    @Test
    void advanceRound_selectsNewTargetDifferentFromPrevious() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(42));
        List<String> candidates = List.of("a", "b", "c", "d", "e", "f", "g");
        GameState gs = createGameState();
        engine.initGame(gs, buildEngineParams(candidates));

        RecognitionState state0 = deserializeState(gs.getEnginePayload());
        String firstTarget = state0.getTargetElementId();

        answerCorrectlyForRound(engine, gs);

        RecognitionState state1 = deserializeState(gs.getEnginePayload());
        assertNotEquals(firstTarget, state1.getTargetElementId(),
                "New round should select a different target when possible");
        assertEquals(2, state1.getRoundsShownElementIds().size());
    }

    @Test
    void buildSummary_setsCompletedAtAndAttempts() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(42));
        List<String> candidates = List.of("a", "b", "c", "d", "e", "f", "g");
        GameState gs = createGameState();
        engine.initGame(gs, buildEngineParams(candidates));

        for (int i = 0; i < RecognitionDefaults.DEFAULT_TOTAL_ROUNDS; i++) {
            answerCorrectlyForRound(engine, gs);
        }

        engine.buildSummary(gs);

        assertNotNull(gs.getCompletedAt());
        assertEquals(GameStatus.COMPLETED, gs.getStatus());
        assertEquals(5, gs.getAttempts());
        assertEquals(0, gs.getIncorrectAttempts());
    }
}
