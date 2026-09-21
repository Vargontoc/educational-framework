package es.vargontoc.educational.framework.game.engine;

import es.vargontoc.educational.framework.content.model.DifficultyCode;
import es.vargontoc.educational.framework.game.model.ActionResult;
import es.vargontoc.educational.framework.game.model.ActionResultType;
import es.vargontoc.educational.framework.game.model.GameState;
import es.vargontoc.educational.framework.game.model.recognition.ComparisonOption;
import es.vargontoc.educational.framework.game.model.recognition.ComparisonScaleLadder;
import es.vargontoc.educational.framework.game.model.recognition.RecognitionState;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SPRINT-108: comparison mode of the RecognitionEngine (big/small ladder, ADR-029).
 */
class RecognitionEngineComparisonTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final List<String> CANDIDATES = List.of("1", "2", "3", "4", "5");

    private String engineParams(DifficultyCode difficulty, boolean withScales) throws Exception {
        List<Double> scales = ComparisonScaleLadder.forDifficulty(difficulty);
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("candidates", CANDIDATES);
        root.put("recognitionCategory", "COMPARISON");
        root.put("comparisonMode", true);

        Map<String, Object> rp = new LinkedHashMap<>();
        rp.put("optionCount", scales.size());
        rp.put("distractorStrategy", "SEMANTICALLY_FAR");
        rp.put("guideChromEnabled", false);
        rp.put("touchEnableDelayMs", 0);
        rp.put("nonChromaticKeyRequired", false);
        rp.put("showIcon", true);
        if (withScales) {
            rp.put("comparisonScales", scales);
        }
        root.put("roundParameters", rp);
        return MAPPER.writeValueAsString(root);
    }

    private RecognitionState state(GameState gs) throws Exception {
        return MAPPER.readValue(gs.getEnginePayload(), RecognitionState.class);
    }

    private GameState init(RecognitionEngine engine, DifficultyCode difficulty) throws Exception {
        GameState gs = new GameState();
        engine.initGame(gs, engineParams(difficulty, true));
        return gs;
    }

    private List<Double> scales(RecognitionState st) {
        return st.getComparisonOptions().stream().map(ComparisonOption::scalePercent).sorted().toList();
    }

    private String action(String selectedOptionId, Double scale) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("selectedOptionId", selectedOptionId);
        if (scale != null) {
            m.put("selectedScalePercent", scale);
        }
        m.put("responseTimeMs", 1200);
        try {
            return MAPPER.writeValueAsString(m);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    // ---- ladder per difficulty

    @Test
    void easy_offersTwoOptionsAt100And40() throws Exception {
        RecognitionState st = state(init(new RecognitionEngine(new Random(1)), DifficultyCode.EASY));

        assertTrue(st.isComparisonMode());
        assertEquals(List.of(40.0, 100.0), scales(st));
        assertEquals(2, st.getOptionIds().size());
    }

    @Test
    void medium_offersTwoOptionsAt100And65() throws Exception {
        RecognitionState st = state(init(new RecognitionEngine(new Random(1)), DifficultyCode.MEDIUM));

        assertEquals(List.of(65.0, 100.0), scales(st));
        assertEquals(2, st.getOptionIds().size());
    }

    @Test
    void hard_offersThreeOptionsAt100_75And50() throws Exception {
        RecognitionState st = state(init(new RecognitionEngine(new Random(1)), DifficultyCode.HARD));

        assertEquals(List.of(50.0, 75.0, 100.0), scales(st));
        assertEquals(3, st.getOptionIds().size());
    }

    @Test
    void allOptionsUseTheSameElementId_theTarget() throws Exception {
        for (DifficultyCode difficulty : DifficultyCode.values()) {
            RecognitionState st = state(init(new RecognitionEngine(new Random(3)), difficulty));

            Set<String> ids = st.getComparisonOptions().stream()
                    .map(ComparisonOption::elementId).collect(Collectors.toSet());
            assertEquals(Set.of(st.getTargetElementId()), ids, difficulty.name());
            assertEquals(Set.of(st.getTargetElementId()), Set.copyOf(st.getOptionIds()), difficulty.name());
        }
    }

    @Test
    void withoutExplicitLadder_fallsBackToTheEasyOne() throws Exception {
        GameState gs = new GameState();
        new RecognitionEngine(new Random(1)).initGame(gs, engineParams(DifficultyCode.HARD, false));

        assertEquals(List.of(40.0, 100.0), scales(state(gs)));
    }

    @Test
    void comparisonModeFlagAloneEnablesTheMode() throws Exception {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("candidates", CANDIDATES);
        root.put("comparisonMode", true);
        GameState gs = new GameState();
        new RecognitionEngine(new Random(1)).initGame(gs, MAPPER.writeValueAsString(root));

        assertTrue(state(gs).isComparisonMode());
    }

    @Test
    void recognitionRounds_stayUntouched() throws Exception {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("candidates", CANDIDATES);
        root.put("recognitionCategory", "ANIMAL");
        GameState gs = new GameState();
        RecognitionEngine engine = new RecognitionEngine(new Random(1));
        engine.initGame(gs, MAPPER.writeValueAsString(root));
        RecognitionState st = state(gs);

        assertFalse(st.isComparisonMode());
        assertNull(st.getComparisonOptions());
        assertFalse(engine.getNextElement(gs).contains("comparisonOptions"));
    }

    @Test
    void biggestOptionIsNotAlwaysInTheSamePosition() throws Exception {
        Set<Integer> positions = new java.util.HashSet<>();
        for (long seed = 0; seed < 40; seed++) {
            RecognitionState st = state(init(new RecognitionEngine(new Random(seed)), DifficultyCode.HARD));
            for (int i = 0; i < st.getComparisonOptions().size(); i++) {
                if (st.getComparisonOptions().get(i).scalePercent() == 100.0) {
                    positions.add(i);
                }
            }
        }
        assertEquals(Set.of(0, 1, 2), positions);
    }

    // ---- getNextElement

    @Test
    void getNextElement_includesComparisonOptionsInComparisonMode() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(1));
        GameState gs = init(engine, DifficultyCode.HARD);
        RecognitionState st = state(gs);

        JsonNode next = MAPPER.readTree(engine.getNextElement(gs));

        JsonNode options = next.get("comparisonOptions");
        assertNotNull(options);
        assertEquals(3, options.size());
        for (int i = 0; i < 3; i++) {
            assertEquals(st.getTargetElementId(), options.get(i).get("elementId").asString());
            assertEquals(st.getComparisonOptions().get(i).scalePercent(), options.get(i).get("scalePercent").asDouble());
        }
    }

    // ---- processAction

    @Test
    void selectingTheBiggest_isCorrectAndAdvancesTheRound() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(1));
        GameState gs = init(engine, DifficultyCode.EASY);
        String target = state(gs).getTargetElementId();

        ActionResult result = engine.processAction(gs, action(target, 100.0));

        assertEquals(ActionResultType.CORRECT, result.getResultType());
        RecognitionState after = state(gs);
        assertEquals(1, after.getRoundIndex());
        assertEquals(1, after.getTotalCorrectFirstTry());
    }

    @Test
    void selectingASmallerOne_isIncorrectAndKeepsTheRound() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(1));
        GameState gs = init(engine, DifficultyCode.HARD);
        RecognitionState before = state(gs);
        List<ComparisonOption> options = new ArrayList<>(before.getComparisonOptions());

        for (ComparisonOption smaller : options) {
            if (smaller.scalePercent() == 100.0) {
                continue;
            }
            ActionResult result = engine.processAction(gs, action(smaller.elementId(), smaller.scalePercent()));
            assertEquals(ActionResultType.INCORRECT, result.getResultType());
        }

        RecognitionState after = state(gs);
        assertEquals(0, after.getRoundIndex());
        assertEquals(before.getTargetElementId(), after.getTargetElementId());
        assertEquals(before.getComparisonOptions(), after.getComparisonOptions());
        assertEquals(2, after.getTotalIncorrectAttempts());
    }

    @Test
    void twoWrongTapsActivateTheHint_asInRecognition() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(1));
        GameState gs = init(engine, DifficultyCode.EASY);
        String target = state(gs).getTargetElementId();

        engine.processAction(gs, action(target, 40.0));
        assertFalse(state(gs).isHintActive());
        engine.processAction(gs, action(target, 40.0));

        assertTrue(state(gs).isHintActive());
    }

    @Test
    void anAnswerWithoutScale_cannotBeTheBiggest() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(1));
        GameState gs = init(engine, DifficultyCode.EASY);
        String target = state(gs).getTargetElementId();

        // legacy client: all options share the element id, so the id alone proves nothing
        ActionResult result = engine.processAction(gs, action(target, null));

        assertEquals(ActionResultType.INCORRECT, result.getResultType());
        assertEquals(0, state(gs).getRoundIndex());
    }

    @Test
    void anotherElementId_isNeverCorrect() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(1));
        GameState gs = init(engine, DifficultyCode.EASY);

        ActionResult result = engine.processAction(gs, action("not-the-target", 100.0));

        assertEquals(ActionResultType.INCORRECT, result.getResultType());
    }

    @Test
    void aScaleThatIsNotInTheRound_isIncorrect() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(1));
        GameState gs = init(engine, DifficultyCode.EASY);
        String target = state(gs).getTargetElementId();

        assertEquals(ActionResultType.INCORRECT, engine.processAction(gs, action(target, 75.0)).getResultType());
        assertEquals(ActionResultType.INCORRECT, engine.processAction(gs, action(target, 999.0)).getResultType());
    }

    @Test
    void fullGame_everyRoundKeepsTheLadder_andCompletes() throws Exception {
        for (DifficultyCode difficulty : DifficultyCode.values()) {
            RecognitionEngine engine = new RecognitionEngine(new Random(7));
            GameState gs = init(engine, difficulty);
            List<Double> expected = ComparisonScaleLadder.forDifficulty(difficulty).stream().sorted().toList();

            RecognitionState st = state(gs);
            int rounds = 0;
            while (st.getRoundIndex() < st.getTotalRounds()) {
                assertEquals(expected, scales(st), difficulty + " round " + rounds);
                assertEquals(Set.of(st.getTargetElementId()), Set.copyOf(st.getOptionIds()));
                engine.processAction(gs, action(st.getTargetElementId(), 100.0));
                st = state(gs);
                rounds++;
            }

            assertEquals(st.getTotalRounds(), rounds);
            assertTrue(engine.isGameComplete(gs));
        }
    }

    @Test
    void fullGame_targetsAreNotRepeatedWhileThereAreUnshownOnes() throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(11));
        GameState gs = init(engine, DifficultyCode.EASY);

        List<String> targets = new ArrayList<>();
        RecognitionState st = state(gs);
        while (st.getRoundIndex() < st.getTotalRounds()) {
            targets.add(st.getTargetElementId());
            engine.processAction(gs, action(st.getTargetElementId(), 100.0));
            st = state(gs);
        }

        assertEquals(targets.size(), Set.copyOf(targets).size());
    }
}
