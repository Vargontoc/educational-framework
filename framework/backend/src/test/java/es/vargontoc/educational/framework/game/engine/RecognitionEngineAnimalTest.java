package es.vargontoc.educational.framework.game.engine;

import es.vargontoc.educational.framework.game.model.GameState;
import es.vargontoc.educational.framework.game.model.recognition.RecognitionState;
import es.vargontoc.educational.framework.game.service.AnimalGroupService;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ANIMAL rounds through the real engine: the distractors follow the animal groups of the target.
 */
class RecognitionEngineAnimalTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final AnimalGroupService animals = AnimalGroupService.fromSeed();

    // Candidate ids "1".."n" map to these animal codes.
    private static final List<String> CODES = List.of(
            "bull", "cow", "deer", "goat", "donkey", "horse", "duck", "goose", "chicken", "sheep", "cat", "dog", "pig");

    private String engineParams(int optionCount, String strategy) throws Exception {
        Map<String, Object> root = new LinkedHashMap<>();
        List<String> ids = new ArrayList<>();
        List<Map<String, Object>> metadata = new ArrayList<>();
        for (int i = 0; i < CODES.size(); i++) {
            String id = String.valueOf(i + 1);
            ids.add(id);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", id);
            m.put("topicId", 1L);
            m.put("code", CODES.get(i));
            metadata.add(m);
        }
        root.put("candidates", ids);
        root.put("recognitionCategory", "ANIMAL");

        Map<String, Object> rp = new LinkedHashMap<>();
        rp.put("optionCount", optionCount);
        rp.put("distractorStrategy", strategy);
        rp.put("guideChromEnabled", false);
        rp.put("touchEnableDelayMs", 0);
        rp.put("nonChromaticKeyRequired", false);
        rp.put("showIcon", true);
        root.put("roundParameters", rp);
        root.put("candidateMetadata", metadata);
        return MAPPER.writeValueAsString(root);
    }

    private RecognitionState state(GameState gs) throws Exception {
        return MAPPER.readValue(gs.getEnginePayload(), RecognitionState.class);
    }

    private String code(String id) {
        return CODES.get(Integer.parseInt(id) - 1);
    }

    /** Plays a whole game answering correctly and returns, per round, the target code and the distractor codes. */
    private List<Map.Entry<String, List<String>>> playGame(long seed, int optionCount, String strategy) throws Exception {
        RecognitionEngine engine = new RecognitionEngine(new Random(seed), null, null, null, animals);
        GameState gs = new GameState();
        engine.initGame(gs, engineParams(optionCount, strategy));

        List<Map.Entry<String, List<String>>> rounds = new ArrayList<>();
        RecognitionState st = state(gs);
        while (st.getRoundIndex() < st.getTotalRounds()) {
            String target = st.getTargetElementId();
            List<String> distractors = st.getOptionIds().stream()
                    .filter(id -> !id.equals(target)).map(this::code).toList();
            rounds.add(Map.entry(code(target), distractors));
            engine.processAction(gs, "{\"selectedOptionId\":\"" + target + "\"}");
            st = state(gs);
        }
        return rounds;
    }

    private Set<String> mates(String code) {
        return Set.copyOf(animals.getAnimalsInSameGroup(code));
    }

    @Test
    void easy_everyRoundHasOneDistractorOutsideTheTargetsGroup() throws Exception {
        for (long seed = 0; seed < 20; seed++) {
            for (var round : playGame(seed, 2, "SEMANTICALLY_FAR")) {
                assertEquals(1, round.getValue().size());
                assertFalse(mates(round.getKey()).contains(round.getValue().get(0)),
                        "seed " + seed + " target " + round.getKey() + " -> " + round.getValue());
            }
        }
    }

    @Test
    void medium_everyRoundHasTwoDistractorsOutsideTheTargetsGroup() throws Exception {
        for (long seed = 0; seed < 20; seed++) {
            for (var round : playGame(seed, 3, "SAME_CATEGORY")) {
                assertEquals(2, round.getValue().size());
                Set<String> groupMates = mates(round.getKey());
                String context = "seed " + seed + " target " + round.getKey() + " -> " + round.getValue();
                round.getValue().forEach(d -> assertFalse(groupMates.contains(d), context));
            }
        }
    }

    @Test
    void hard_distractorsComeFromTheTargetsGroupFirst() throws Exception {
        boolean checkedGroupedTarget = false;
        for (long seed = 0; seed < 20; seed++) {
            for (var round : playGame(seed, 3, "SIMILAR_OUTLINE")) {
                Set<String> groupMates = mates(round.getKey());
                assertEquals(2, round.getValue().size());
                if (groupMates.size() >= 2) {
                    checkedGroupedTarget = true;
                    String context = "seed " + seed + " target " + round.getKey() + " -> " + round.getValue();
                    round.getValue().forEach(d -> assertTrue(groupMates.contains(d), context));
                } else if (groupMates.size() == 1) {
                    assertTrue(round.getValue().contains(groupMates.iterator().next()),
                            "target " + round.getKey() + " -> " + round.getValue());
                }
            }
        }
        assertTrue(checkedGroupedTarget);
    }
}
