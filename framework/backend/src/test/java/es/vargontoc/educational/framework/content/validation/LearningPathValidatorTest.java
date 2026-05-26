package es.vargontoc.educational.framework.content.validation;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LearningPathValidatorTest {

    private final LearningPathValidator validator = new LearningPathValidator();

    @Test
    void validLearningPath_passes() {
        assertDoesNotThrow(() -> validator.validateForCreate("Math Basics", 3, 6, "es-ES", ContentStatus.ACTIVE));
    }

    @Test
    void blankName_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(" ", 3, 6, "es-ES", ContentStatus.ACTIVE));
    }

    @Test
    void nullName_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(null, 3, 6, "es-ES", ContentStatus.ACTIVE));
    }

    @Test
    void nameExceeds200Chars_throwsValidationException() {
        String longName = "a".repeat(201);
        assertThrows(ValidationException.class, () -> validator.validateForCreate(longName, 3, 6, "es-ES", ContentStatus.ACTIVE));
    }

    @Test
    void nameExactly200Chars_passes() {
        String name200 = "a".repeat(200);
        assertDoesNotThrow(() -> validator.validateForCreate(name200, 3, 6, "es-ES", ContentStatus.ACTIVE));
    }

    @Test
    void blankLocale_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate("Math Basics", 3, 6, " ", ContentStatus.ACTIVE));
    }

    @Test
    void nullStatus_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate("Math Basics", 3, 6, "es-ES", null));
    }

    @Test
    void negativeMinAge_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate("Math Basics", -1, 6, "es-ES", ContentStatus.ACTIVE));
    }

    @Test
    void maxAgeLessThanMinAge_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate("Math Basics", 10, 5, "es-ES", ContentStatus.ACTIVE));
    }

    @Test
    void nullAges_passes() {
        assertDoesNotThrow(() -> validator.validateForCreate("Math Basics", null, null, "es-ES", ContentStatus.ACTIVE));
    }

    @Test
    void equalMinAndMaxAge_passes() {
        assertDoesNotThrow(() -> validator.validateForCreate("Math Basics", 5, 5, "es-ES", ContentStatus.ACTIVE));
    }
}
