package es.vargontoc.educational.framework.game.service;

import es.vargontoc.educational.framework.content.model.DifficultyCode;
import es.vargontoc.educational.framework.family.model.ColorVisionMode;
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
            new RecognitionDifficultyService(new RecognitionDifficultyConfig());

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
}
