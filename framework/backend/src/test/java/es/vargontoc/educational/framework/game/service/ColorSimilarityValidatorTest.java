package es.vargontoc.educational.framework.game.service;

import es.vargontoc.educational.framework.family.model.ColorVisionMode;
import es.vargontoc.educational.framework.family.ports.in.ColorAdaptativeUseCase;
import es.vargontoc.educational.framework.family.service.AdaptativeColorService;
import es.vargontoc.educational.framework.game.model.recognition.CandidateMetadata;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ColorSimilarityValidatorTest {

    private static final String RED = "#FF0000";
    private static final String NEAR_RED = "#FE0000";
    private static final String BLUE = "#0000FF";
    private static final String GREEN = "#00FF00";
    /** Gray with the same luminance as pure red: identical to it for an achromatopsia profile. */
    private static final String RED_LUMINANCE_GRAY = "#808080";

    private final ColorAdaptativeUseCase realColorService = new AdaptativeColorService();
    private final ColorSimilarityValidator validator = new ColorSimilarityValidator(realColorService);

    private Function<String, CandidateMetadata> resolver(Map<String, String> colorsById) {
        Map<String, CandidateMetadata> metadata = new HashMap<>();
        colorsById.forEach((id, hex) -> metadata.put(id, new CandidateMetadata(id, 1L, null, id, hex)));
        return metadata::get;
    }

    // ---- validateDistractors

    @Test
    void validateDistractors_targetTooSimilarToDistractor_returnsFalse() {
        assertFalse(validator.validateDistractors(RED, List.of(NEAR_RED, BLUE), ColorVisionMode.NONE));
    }

    @Test
    void validateDistractors_distractorsTooSimilarToEachOther_returnsFalse() {
        assertFalse(validator.validateDistractors(GREEN, List.of(RED, NEAR_RED), ColorVisionMode.NONE));
    }

    @Test
    void validateDistractors_sufficientlyDifferentColors_returnsTrue() {
        assertTrue(validator.validateDistractors(RED, List.of(BLUE, GREEN), ColorVisionMode.NONE));
    }

    @Test
    void validateDistractors_dependsOnColorVisionMode() {
        assertTrue(validator.validateDistractors(RED, List.of(RED_LUMINANCE_GRAY), ColorVisionMode.NONE));
        assertFalse(validator.validateDistractors(RED, List.of(RED_LUMINANCE_GRAY), ColorVisionMode.ACHROMATOPSIA));
    }

    @Test
    void validateDistractors_nullOrEmptyInput_isConsideredValid() {
        assertTrue(validator.validateDistractors(null, List.of(RED), ColorVisionMode.NONE));
        assertTrue(validator.validateDistractors(RED, null, ColorVisionMode.NONE));
        assertTrue(validator.validateDistractors(RED, List.of(), ColorVisionMode.NONE));
    }

    @Test
    void validateDistractors_nullColorVisionMode_behavesAsNone() {
        assertFalse(validator.validateDistractors(RED, List.of(NEAR_RED), null));
        assertTrue(validator.validateDistractors(RED, List.of(BLUE), null));
    }

    // ---- filterValidDistractors

    @Test
    void filterValidDistractors_skipsColorsTooSimilarToTarget() {
        Map<String, String> colors = Map.of("target", RED, "near", NEAR_RED, "blue", BLUE, "green", GREEN);

        for (int i = 0; i < 20; i++) {
            List<String> result = validator.filterValidDistractors(
                    "target", List.of("near", "blue", "green"), 2, ColorVisionMode.NONE, resolver(colors));

            assertEquals(2, result.size());
            assertFalse(result.contains("near"), "near-red must be replaced by an alternative from the pool");
        }
    }

    @Test
    void filterValidDistractors_doesNotPickTwoDistractorsTooSimilarToEachOther() {
        Map<String, String> colors = Map.of("target", GREEN, "red", RED, "nearRed", NEAR_RED, "blue", BLUE);

        for (int i = 0; i < 20; i++) {
            List<String> result = validator.filterValidDistractors(
                    "target", List.of("red", "nearRed", "blue"), 2, ColorVisionMode.NONE, resolver(colors));

            assertEquals(2, result.size());
            assertFalse(result.contains("red") && result.contains("nearRed"));
        }
    }

    @Test
    void filterValidDistractors_filtersAccordingToColorVisionMode() {
        Map<String, String> colors = Map.of("target", RED, "gray", RED_LUMINANCE_GRAY, "blue", BLUE);
        Function<String, CandidateMetadata> resolver = resolver(colors);
        List<String> pool = List.of("gray", "blue");

        // For NONE the gray is a valid distractor (both are picked); for ACHROMATOPSIA it is skipped first.
        assertEquals(2, validator.filterValidDistractors("target", pool, 2, ColorVisionMode.NONE, resolver).size());
        List<String> achromatopsia = validator.filterValidDistractors(
                "target", pool, 1, ColorVisionMode.ACHROMATOPSIA, resolver);
        assertEquals(List.of("blue"), achromatopsia);
    }

    @Test
    void filterValidDistractors_notEnoughValidCandidates_relaxesConstraint() {
        Map<String, String> colors = Map.of("target", RED, "near1", NEAR_RED, "near2", "#FD0000");

        List<String> result = validator.filterValidDistractors(
                "target", List.of("near1", "near2"), 2, ColorVisionMode.NONE, resolver(colors));

        assertEquals(2, result.size());
        assertTrue(result.containsAll(List.of("near1", "near2")));
    }

    @Test
    void filterValidDistractors_targetColorUnknown_fallsBackToRandomSelection() {
        List<String> result = validator.filterValidDistractors(
                "target", List.of("a", "b", "c"), 2, ColorVisionMode.NONE, id -> null);

        assertEquals(2, result.size());
        assertTrue(List.of("a", "b", "c").containsAll(result));
    }

    @Test
    void filterValidDistractors_candidateWithoutColor_isStillOffered() {
        Map<String, CandidateMetadata> metadata = new HashMap<>();
        metadata.put("target", new CandidateMetadata("target", 1L, null, "target", RED));
        metadata.put("unknown", new CandidateMetadata("unknown", 1L, null, "unknown", null));

        List<String> result = validator.filterValidDistractors(
                "target", List.of("unknown"), 1, ColorVisionMode.NONE, metadata::get);

        assertEquals(List.of("unknown"), result);
    }

    @Test
    void filterValidDistractors_emptyPoolOrNonPositiveCount_returnsEmpty() {
        Function<String, CandidateMetadata> resolver = resolver(Map.of("target", RED));

        assertTrue(validator.filterValidDistractors("target", List.of(), 2, ColorVisionMode.NONE, resolver).isEmpty());
        assertTrue(validator.filterValidDistractors("target", List.of("a"), 0, ColorVisionMode.NONE, resolver).isEmpty());
        assertTrue(validator.filterValidDistractors("target", null, 2, ColorVisionMode.NONE, resolver).isEmpty());
    }

    // ---- cache and robustness

    @Test
    void similarityResultIsCachedAndIndependentOfArgumentOrder() {
        ColorAdaptativeUseCase useCase = mock(ColorAdaptativeUseCase.class);
        when(useCase.isTooSimilar(any(), anyString(), anyString())).thenReturn(false);
        ColorSimilarityValidator cached = new ColorSimilarityValidator(useCase);

        cached.validateDistractors(RED, List.of(BLUE), ColorVisionMode.NONE);
        cached.validateDistractors(BLUE, List.of(RED), ColorVisionMode.NONE);
        cached.validateDistractors("#ff0000", List.of("#0000ff"), ColorVisionMode.NONE);

        verify(useCase, times(1)).isTooSimilar(any(), anyString(), anyString());
    }

    @Test
    void similarityCacheIsKeyedByColorVisionMode() {
        ColorAdaptativeUseCase useCase = mock(ColorAdaptativeUseCase.class);
        when(useCase.isTooSimilar(any(), anyString(), anyString())).thenReturn(false);
        ColorSimilarityValidator cached = new ColorSimilarityValidator(useCase);

        cached.validateDistractors(RED, List.of(BLUE), ColorVisionMode.NONE);
        cached.validateDistractors(RED, List.of(BLUE), ColorVisionMode.ACHROMATOPSIA);

        verify(useCase, times(2)).isTooSimilar(any(), anyString(), anyString());
    }

    @Test
    void malformedHexColor_doesNotBreakValidation() {
        ColorAdaptativeUseCase useCase = mock(ColorAdaptativeUseCase.class);
        when(useCase.isTooSimilar(any(), anyString(), anyString())).thenThrow(new NumberFormatException("bad hex"));
        ColorSimilarityValidator lenient = new ColorSimilarityValidator(useCase);

        assertTrue(lenient.validateDistractors(RED, List.of("not-a-color"), ColorVisionMode.NONE));
    }
}
