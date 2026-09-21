package es.vargontoc.educational.framework.game.service;

import es.vargontoc.educational.framework.content.model.DifficultyCode;
import es.vargontoc.educational.framework.family.model.ColorVisionMode;
import es.vargontoc.educational.framework.game.application.RecognitionProperties;
import es.vargontoc.educational.framework.game.model.enums.RecognitionCategory;
import es.vargontoc.educational.framework.game.model.recognition.DistractorStrategy;
import es.vargontoc.educational.framework.game.model.recognition.RecognitionDifficultyConfig;
import es.vargontoc.educational.framework.game.model.recognition.RoundParameters;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecognitionDifficultyServiceTest {

    private final RecognitionDifficultyService service =
            new RecognitionDifficultyService(new RecognitionDifficultyConfig(new RecognitionProperties()));

    @Test
    void resolveRoundParameters_easyLetter_returnsEasyTierWithoutNonChromaticKey() {
        RoundParameters result = service.resolveRoundParameters(
                DifficultyCode.EASY, RecognitionCategory.LETTER, ColorVisionMode.NONE);

        assertEquals(2, result.optionCount());
        assertEquals(DistractorStrategy.SEMANTICALLY_FAR, result.distractorStrategy());
        assertTrue(result.guideChromEnabled());
        assertEquals(500, result.touchEnableDelayMs());
        assertFalse(result.nonChromaticKeyRequired());
    }

    @Test
    void resolveRoundParameters_mediumAnimal_returnsMediumTierWithoutNonChromaticKey() {
        RoundParameters result = service.resolveRoundParameters(
                DifficultyCode.MEDIUM, RecognitionCategory.ANIMAL, ColorVisionMode.NONE);

        assertEquals(3, result.optionCount());
        assertEquals(DistractorStrategy.SAME_CATEGORY, result.distractorStrategy());
        assertFalse(result.guideChromEnabled());
        assertEquals(800, result.touchEnableDelayMs());
        assertFalse(result.nonChromaticKeyRequired());
    }

    @Test
    void resolveRoundParameters_hardNumber_returnsHardTierWithoutNonChromaticKey() {
        RoundParameters result = service.resolveRoundParameters(
                DifficultyCode.HARD, RecognitionCategory.NUMBER, ColorVisionMode.NONE);

        assertTrue(result.optionCount() >= 3 && result.optionCount() <= 4);
        assertEquals(DistractorStrategy.SIMILAR_OUTLINE, result.distractorStrategy());
        assertFalse(result.guideChromEnabled());
        assertEquals(0, result.touchEnableDelayMs());
        assertFalse(result.nonChromaticKeyRequired());
    }

    @Test
    void resolveRoundParameters_easyColorWithoutVisionPreference_doesNotRequireNonChromaticKey() {
        RoundParameters result = service.resolveRoundParameters(
                DifficultyCode.EASY, RecognitionCategory.COLOR, ColorVisionMode.NONE);

        assertFalse(result.nonChromaticKeyRequired());
    }

    @Test
    void resolveRoundParameters_mediumColorWithDeuteranopia_requiresNonChromaticKey() {
        RoundParameters result = service.resolveRoundParameters(
                DifficultyCode.MEDIUM, RecognitionCategory.COLOR, ColorVisionMode.DEUTERANOPIA);

        assertTrue(result.nonChromaticKeyRequired());
    }

    @Test
    void resolveRoundParameters_hardColorWithAchromatopsia_requiresNonChromaticKey() {
        RoundParameters result = service.resolveRoundParameters(
                DifficultyCode.HARD, RecognitionCategory.COLOR, ColorVisionMode.ACHROMATOPSIA);

        assertTrue(result.nonChromaticKeyRequired());
    }

    @Test
    void resolveRoundParameters_shape_behavesLikeOtherNonColorCategories() {
        RoundParameters shapeResult = service.resolveRoundParameters(
                DifficultyCode.MEDIUM, RecognitionCategory.SHAPE, ColorVisionMode.NONE);
        RoundParameters animalResult = service.resolveRoundParameters(
                DifficultyCode.MEDIUM, RecognitionCategory.ANIMAL, ColorVisionMode.NONE);

        assertEquals(animalResult, shapeResult);
    }

    @Test
    void resolveRoundParameters_usesCustomRecognitionProperties() {
        RecognitionProperties properties = new RecognitionProperties();
        properties.getDifficulty().setEasy(new RecognitionProperties.Tier(5, 1234, false));
        RecognitionDifficultyService customService =
                new RecognitionDifficultyService(new RecognitionDifficultyConfig(properties));

        RoundParameters result = customService.resolveRoundParameters(
                DifficultyCode.EASY, RecognitionCategory.LETTER, ColorVisionMode.NONE);

        assertEquals(5, result.optionCount());
        assertEquals(1234, result.touchEnableDelayMs());
        assertFalse(result.guideChromEnabled());
        assertEquals(DistractorStrategy.SEMANTICALLY_FAR, result.distractorStrategy());
    }
    @Test
    void resolveRoundParameters_easyAndMedium_showIcon() {
        assertTrue(service.resolveRoundParameters(
                DifficultyCode.EASY, RecognitionCategory.COLOR, ColorVisionMode.NONE).showIcon());
        assertTrue(service.resolveRoundParameters(
                DifficultyCode.MEDIUM, RecognitionCategory.COLOR, ColorVisionMode.NONE).showIcon());
    }

    @Test
    void resolveRoundParameters_hard_doesNotShowIcon() {
        assertFalse(service.resolveRoundParameters(
                DifficultyCode.HARD, RecognitionCategory.COLOR, ColorVisionMode.NONE).showIcon());
    }

    @Test
    void resolveRoundParameters_color_optionCountPerDifficulty() {
        assertEquals(2, service.resolveRoundParameters(
                DifficultyCode.EASY, RecognitionCategory.COLOR, ColorVisionMode.NONE).optionCount());
        assertEquals(3, service.resolveRoundParameters(
                DifficultyCode.MEDIUM, RecognitionCategory.COLOR, ColorVisionMode.NONE).optionCount());
        int hardOptions = service.resolveRoundParameters(
                DifficultyCode.HARD, RecognitionCategory.COLOR, ColorVisionMode.NONE).optionCount();
        assertTrue(hardOptions >= 3 && hardOptions <= 4, "HARD ladder is 3-4 options, was " + hardOptions);
    }

    @Test
    void resolveRoundParameters_comparison_followsTheSizeLadder() {
        RoundParameters easy = service.resolveRoundParameters(
                DifficultyCode.EASY, RecognitionCategory.COMPARISON, ColorVisionMode.NONE);
        RoundParameters medium = service.resolveRoundParameters(
                DifficultyCode.MEDIUM, RecognitionCategory.COMPARISON, ColorVisionMode.NONE);
        RoundParameters hard = service.resolveRoundParameters(
                DifficultyCode.HARD, RecognitionCategory.COMPARISON, ColorVisionMode.NONE);

        assertEquals(java.util.List.of(100.0, 40.0), easy.comparisonScales());
        assertEquals(2, easy.optionCount());
        assertEquals(java.util.List.of(100.0, 65.0), medium.comparisonScales());
        assertEquals(2, medium.optionCount());
        assertEquals(java.util.List.of(100.0, 75.0, 50.0), hard.comparisonScales());
        assertEquals(3, hard.optionCount());
    }

    @Test
    void resolveRoundParameters_comparison_ignoresColourVisionProfile() {
        RoundParameters result = service.resolveRoundParameters(
                DifficultyCode.EASY, RecognitionCategory.COMPARISON, ColorVisionMode.PROTANOPIA);

        assertFalse(result.nonChromaticKeyRequired());
    }

    @Test
    void resolveRoundParameters_otherCategories_haveNoComparisonScales() {
        for (RecognitionCategory category : RecognitionCategory.values()) {
            if (category == RecognitionCategory.COMPARISON) {
                continue;
            }
            assertEquals(null, service.resolveRoundParameters(
                    DifficultyCode.EASY, category, ColorVisionMode.NONE).comparisonScales(), category.name());
        }
    }
}
