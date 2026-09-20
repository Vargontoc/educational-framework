package es.vargontoc.educational.framework.game.engine;

import es.vargontoc.educational.framework.family.model.ColorVisionMode;
import es.vargontoc.educational.framework.family.service.AdaptativeColorService;
import es.vargontoc.educational.framework.game.model.GameState;
import es.vargontoc.educational.framework.game.model.recognition.RecognitionState;
import es.vargontoc.educational.framework.game.service.ColorSimilarityValidator;
import es.vargontoc.educational.framework.game.service.RecognitionSimilarityService;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * COLOR rounds: showIcon per difficulty and end-to-end distractor validation with the real colour service.
 */
class RecognitionEngineColorTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final AdaptativeColorService colorService = new AdaptativeColorService();
    private final ColorSimilarityValidator validator = new ColorSimilarityValidator(colorService);

    private static final Map<String, String> COLORS = Map.of(
            "1", "#FF0000",   // red
            "2", "#FE0000",   // almost identical to red
            "3", "#0000FF",   // blue
            "4", "#00FF00",   // green
            "5", "#FFFF00");  // yellow

    private String engineParams(Integer optionCount, Boolean showIcon) throws Exception {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("candidates", List.copyOf(COLORS.keySet().stream().sorted().toList()));
        root.put("recognitionCategory", "COLOR");

        Map<String, Object> rp = new LinkedHashMap<>();
        rp.put("optionCount", optionCount);
        rp.put("distractorStrategy", "SAME_CATEGORY");
        rp.put("guideChromEnabled", false);
        rp.put("touchEnableDelayMs", 0);
        rp.put("nonChromaticKeyRequired", false);
        if (showIcon != null) {
            rp.put("showIcon", showIcon);
        }
        root.put("roundParameters", rp);

        List<Map<String, Object>> metadata = new ArrayList<>();
        COLORS.keySet().stream().sorted().forEach(id -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", id);
            m.put("topicId", 1L);
            m.put("code", "color_" + id);
            m.put("colorHex", COLORS.get(id));
            metadata.add(m);
        });
        root.put("candidateMetadata", metadata);
        return MAPPER.writeValueAsString(root);
    }

    private RecognitionState init(RecognitionEngine engine, Integer optionCount, Boolean showIcon) throws Exception {
        GameState gs = new GameState();
        engine.initGame(gs, engineParams(optionCount, showIcon));
        return MAPPER.readValue(gs.getEnginePayload(), RecognitionState.class);
    }

    private RecognitionEngine engine(long seed, ColorVisionMode mode) {
        return new RecognitionEngine(new Random(seed), (RecognitionSimilarityService) null, validator, mode);
    }

    @Test
    void showIconTrue_isStoredInState() throws Exception {
        assertTrue(init(engine(1, ColorVisionMode.NONE), 2, true).isShowIcon());
    }

    @Test
    void showIconFalse_isStoredInState() throws Exception {
        assertFalse(init(engine(1, ColorVisionMode.NONE), 3, false).isShowIcon());
    }

    @Test
    void showIconMissingInEngineParams_defaultsToTrue() throws Exception {
        assertTrue(init(engine(1, ColorVisionMode.NONE), 3, null).isShowIcon());
    }

    @Test
    void showIcon_isPreservedWhenTheRoundAdvances() throws Exception {
        RecognitionEngine engine = engine(2, ColorVisionMode.NONE);
        GameState gs = new GameState();
        engine.initGame(gs, engineParams(3, false));
        RecognitionState first = MAPPER.readValue(gs.getEnginePayload(), RecognitionState.class);

        engine.processAction(gs, MAPPER.writeValueAsString(Map.of("selectedOptionId", first.getTargetElementId())));

        RecognitionState second = MAPPER.readValue(gs.getEnginePayload(), RecognitionState.class);
        assertEquals(1, second.getRoundIndex());
        assertFalse(second.isShowIcon());
    }

    @Test
    void colorRounds_neverOfferTwoOptionsTooSimilarToEachOther() throws Exception {
        for (long seed = 0; seed < 60; seed++) {
            RecognitionState state = init(engine(seed, ColorVisionMode.NONE), 3, true);
            List<String> options = state.getOptionIds();

            assertEquals(3, options.size(), "seed " + seed);
            for (int i = 0; i < options.size(); i++) {
                for (int j = i + 1; j < options.size(); j++) {
                    assertFalse(colorService.isTooSimilar(ColorVisionMode.NONE,
                                    COLORS.get(options.get(i)), COLORS.get(options.get(j))),
                            "seed " + seed + ": " + options.get(i) + " vs " + options.get(j));
                }
            }
        }
    }

    @Test
    void colorRounds_respectTheChildColorVisionMode() throws Exception {
        // For a deuteranopia-like profile red and green collapse into similar hues; the selection must
        // still be valid for THAT profile, not only for normal vision.
        ColorVisionMode mode = ColorVisionMode.ACHROMATOMALY;
        for (long seed = 0; seed < 40; seed++) {
            List<String> options = init(engine(seed, mode), 2, true).getOptionIds();
            boolean similar = colorService.isTooSimilar(mode, COLORS.get(options.get(0)), COLORS.get(options.get(1)));
            // 5 candidates leave enough valid alternatives, so the relaxed fallback must not be needed
            assertFalse(similar, "seed " + seed + ": " + options);
        }
    }
}
