package es.vargontoc.educational.framework.content.validation;

import es.vargontoc.educational.framework.content.model.Biome;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorldHostValidatorTest {

    private WorldHostValidator validator;

    @BeforeEach
    void setUp() {
        validator = new WorldHostValidator();
    }

    @Test
    void validWorldHost_passes() {
        assertDoesNotThrow(() -> validator.validateForCreate(
                "MEADOW_DOG",
                "Dog",
                Biome.MEADOW,
                3,
                4,
                ContentStatus.ACTIVE
        ));
    }

    @Test
    void nullCode_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
                null,
                "Dog",
                Biome.MEADOW,
                3,
                4,
                ContentStatus.ACTIVE
        ));
    }

    @Test
    void blankCode_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
                "   ",
                "Dog",
                Biome.MEADOW,
                3,
                4,
                ContentStatus.ACTIVE
        ));
    }

    @Test
    void nullDisplayName_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
                "MEADOW_DOG",
                null,
                Biome.MEADOW,
                3,
                4,
                ContentStatus.ACTIVE
        ));
    }

    @Test
    void nullBiome_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
                "MEADOW_DOG",
                "Dog",
                null,
                3,
                4,
                ContentStatus.ACTIVE
        ));
    }

    @Test
    void nullStatus_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
                "MEADOW_DOG",
                "Dog",
                Biome.MEADOW,
                3,
                4,
                null
        ));
    }

    @Test
    void maxAgeLessThanMinAge_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
                "MEADOW_DOG",
                "Dog",
                Biome.MEADOW,
                5,
                4,
                ContentStatus.ACTIVE
        ));
    }

    @Test
    void negativeMinAge_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
                "MEADOW_DOG",
                "Dog",
                Biome.MEADOW,
                -1,
                4,
                ContentStatus.ACTIVE
        ));
    }

    @Test
    void nullMinAge_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
                "MEADOW_DOG",
                "Dog",
                Biome.MEADOW,
                null,
                4,
                ContentStatus.ACTIVE
        ));
    }

    @Test
    void nullMaxAge_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
                "MEADOW_DOG",
                "Dog",
                Biome.MEADOW,
                3,
                null,
                ContentStatus.ACTIVE
        ));
    }

    @Test
    void sameMinAndMaxAge_passes() {
        assertDoesNotThrow(() -> validator.validateForCreate(
                "MEADOW_DOG",
                "Dog",
                Biome.MEADOW,
                4,
                4,
                ContentStatus.ACTIVE
        ));
    }
}
