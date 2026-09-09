package es.vargontoc.educational.framework.content.validation;

import es.vargontoc.educational.framework.content.model.Biome;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.ElementType;
import es.vargontoc.educational.framework.content.model.InteractionCueType;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorldDiscoveryElementValidatorTest {

    private WorldDiscoveryElementValidator validator;

    @BeforeEach
    void setUp() {
        validator = new WorldDiscoveryElementValidator();
    }

    @Test
    void validDiscoveryElementWithActivityId_passes() {
        assertDoesNotThrow(() -> validator.validateForCreate(
                "MEADOW_SHINY_FLOWER",
                "Shiny Flower",
                ElementType.DISCOVERY,
                Biome.MEADOW,
                3,
                4,
                ContentStatus.ACTIVE,
                1L,
                null,
                InteractionCueType.BREATHING_GLOW,
                null,
                null
        ));
    }

    @Test
    void validDecorativeElementWithoutActivityId_passes() {
        assertDoesNotThrow(() -> validator.validateForCreate(
                "MEADOW_ROCK",
                "Meadow Rock",
                ElementType.DECORATIVE,
                Biome.MEADOW,
                3,
                4,
                ContentStatus.ACTIVE,
                null,
                null,
                null,
                null,
                null
        ));
    }

    @Test
    void decorativeElementWithActivityId_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
                "MEADOW_ROCK",
                "Meadow Rock",
                ElementType.DECORATIVE,
                Biome.MEADOW,
                3,
                4,
                ContentStatus.ACTIVE,
                1L,
                null,
                null,
                null,
                null
        ));
    }

    @Test
    void simpleInteractiveElementWithActivityId_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
                "MEADOW_BUTTERFLY",
                "Meadow Butterfly",
                ElementType.SIMPLE_INTERACTIVE,
                Biome.MEADOW,
                3,
                4,
                ContentStatus.ACTIVE,
                1L,
                null,
                null,
                null,
                null
        ));
    }

    @Test
    void nullCode_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
                null,
                "Shiny Flower",
                ElementType.DISCOVERY,
                Biome.MEADOW,
                3,
                4,
                ContentStatus.ACTIVE,
                1L,
                null,
                InteractionCueType.BREATHING_GLOW,
                null,
                null
        ));
    }

    @Test
    void blankCode_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
                "   ",
                "Shiny Flower",
                ElementType.DISCOVERY,
                Biome.MEADOW,
                3,
                4,
                ContentStatus.ACTIVE,
                1L,
                null,
                InteractionCueType.BREATHING_GLOW,
                null,
                null
        ));
    }

    @Test
    void nullDisplayName_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
                "MEADOW_SHINY_FLOWER",
                null,
                ElementType.DISCOVERY,
                Biome.MEADOW,
                3,
                4,
                ContentStatus.ACTIVE,
                1L,
                null,
                InteractionCueType.BREATHING_GLOW,
                null,
                null
        ));
    }

    @Test
    void nullElementType_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
                "MEADOW_SHINY_FLOWER",
                "Shiny Flower",
                null,
                Biome.MEADOW,
                3,
                4,
                ContentStatus.ACTIVE,
                1L,
                null,
                InteractionCueType.BREATHING_GLOW,
                null,
                null
        ));
    }

    @Test
    void nullBiome_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
                "MEADOW_SHINY_FLOWER",
                "Shiny Flower",
                ElementType.DISCOVERY,
                null,
                3,
                4,
                ContentStatus.ACTIVE,
                1L,
                null,
                InteractionCueType.BREATHING_GLOW,
                null,
                null
        ));
    }

    @Test
    void nullStatus_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
                "MEADOW_SHINY_FLOWER",
                "Shiny Flower",
                ElementType.DISCOVERY,
                Biome.MEADOW,
                3,
                4,
                null,
                1L,
                null,
                InteractionCueType.BREATHING_GLOW,
                null,
                null
        ));
    }

    @Test
    void nullMinAge_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
                "MEADOW_SHINY_FLOWER",
                "Shiny Flower",
                ElementType.DISCOVERY,
                Biome.MEADOW,
                null,
                4,
                ContentStatus.ACTIVE,
                1L,
                null,
                InteractionCueType.BREATHING_GLOW,
                null,
                null
        ));
    }

    @Test
    void nullMaxAge_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
                "MEADOW_SHINY_FLOWER",
                "Shiny Flower",
                ElementType.DISCOVERY,
                Biome.MEADOW,
                3,
                null,
                ContentStatus.ACTIVE,
                1L,
                null,
                InteractionCueType.BREATHING_GLOW,
                null,
                null
        ));
    }

    @Test
    void maxAgeLessThanMinAge_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
                "MEADOW_SHINY_FLOWER",
                "Shiny Flower",
                ElementType.DISCOVERY,
                Biome.MEADOW,
                5,
                4,
                ContentStatus.ACTIVE,
                1L,
                null,
                InteractionCueType.BREATHING_GLOW,
                null,
                null
        ));
    }

    @Test
    void negativeMinAge_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
                "MEADOW_SHINY_FLOWER",
                "Shiny Flower",
                ElementType.DISCOVERY,
                Biome.MEADOW,
                -1,
                4,
                ContentStatus.ACTIVE,
                1L,
                null,
                InteractionCueType.BREATHING_GLOW,
                null,
                null
        ));
    }

    @Test
    void positionWithinNormalizedRange_passes() {
        assertDoesNotThrow(() -> validator.validateForCreate(
                "MEADOW_SHINY_FLOWER",
                "Shiny Flower",
                ElementType.DISCOVERY,
                Biome.MEADOW,
                3,
                4,
                ContentStatus.ACTIVE,
                1L,
                null,
                InteractionCueType.BREATHING_GLOW,
                0.0,
                1.0
        ));
    }

    @Test
    void positionXBelowZero_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
                "MEADOW_SHINY_FLOWER",
                "Shiny Flower",
                ElementType.DISCOVERY,
                Biome.MEADOW,
                3,
                4,
                ContentStatus.ACTIVE,
                1L,
                null,
                InteractionCueType.BREATHING_GLOW,
                -0.1,
                0.5
        ));
    }

    @Test
    void positionYAboveOne_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
                "MEADOW_SHINY_FLOWER",
                "Shiny Flower",
                ElementType.DISCOVERY,
                Biome.MEADOW,
                3,
                4,
                ContentStatus.ACTIVE,
                1L,
                null,
                InteractionCueType.BREATHING_GLOW,
                0.5,
                1.1
        ));
    }
}
