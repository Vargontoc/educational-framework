package es.vargontoc.educational.framework.game.service;

import es.vargontoc.educational.framework.family.model.ColorVisionMode;
import es.vargontoc.educational.framework.family.service.AdaptativeColorService;
import es.vargontoc.educational.framework.game.model.enums.RecognitionCategory;
import es.vargontoc.educational.framework.game.model.recognition.CandidateMetadata;
import es.vargontoc.educational.framework.game.model.recognition.DistractorStrategy;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DistractorSelectorColorTest {

    private final ColorSimilarityValidator validator = new ColorSimilarityValidator(new AdaptativeColorService());

    private Map<String, CandidateMetadata> colors() {
        Map<String, CandidateMetadata> metadata = new HashMap<>();
        metadata.put("red", new CandidateMetadata("red", 1L, null, "color_red", "#FF0000"));
        metadata.put("nearRed", new CandidateMetadata("nearRed", 1L, null, "color_near_red", "#FE0000"));
        metadata.put("blue", new CandidateMetadata("blue", 1L, null, "color_blue", "#0000FF"));
        metadata.put("green", new CandidateMetadata("green", 1L, null, "color_green", "#00FF00"));
        return metadata;
    }

    @Test
    void colorCategory_withValidator_avoidsColorsTooSimilarToTarget() {
        DistractorSelector selector = new DistractorSelector(new Random(1), null, validator, ColorVisionMode.NONE);
        Map<String, CandidateMetadata> metadata = colors();

        for (DistractorStrategy strategy : DistractorStrategy.values()) {
            for (int i = 0; i < 10; i++) {
                List<String> result = selector.select("red", List.of("red", "nearRed", "blue", "green"),
                        strategy, 2, RecognitionCategory.COLOR, metadata::get);

                assertEquals(2, result.size(), strategy.name());
                assertFalse(result.contains("nearRed"), strategy.name());
                assertFalse(result.contains("red"), strategy.name());
            }
        }
    }

    @Test
    void colorCategory_withoutValidator_usesExistingStrategyLogic() {
        DistractorSelector selector = new DistractorSelector(new Random(1));
        Map<String, CandidateMetadata> metadata = colors();

        List<String> result = selector.select("red", List.of("red", "nearRed", "blue", "green"),
                DistractorStrategy.SEMANTICALLY_FAR, 3, RecognitionCategory.COLOR, metadata::get);

        assertEquals(3, result.size());
        assertFalse(result.contains("red"));
    }

    @Test
    void otherCategories_ignoreColorValidation() {
        DistractorSelector selector = new DistractorSelector(new Random(1), null, validator, ColorVisionMode.NONE);
        Map<String, CandidateMetadata> metadata = colors();

        List<String> result = selector.select("red", List.of("red", "nearRed", "blue"),
                DistractorStrategy.SEMANTICALLY_FAR, 2, RecognitionCategory.ANIMAL, metadata::get);

        assertEquals(2, result.size());
    }
}
