package es.vargontoc.educational.framework.game.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import es.vargontoc.educational.framework.game.model.GameState;
import es.vargontoc.educational.framework.game.model.GameStatus;
import es.vargontoc.educational.framework.game.model.enums.EngineType;
import es.vargontoc.educational.framework.game.model.recognition.RecognitionDefaults;
import es.vargontoc.educational.framework.game.model.recognition.RecognitionState;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
}
