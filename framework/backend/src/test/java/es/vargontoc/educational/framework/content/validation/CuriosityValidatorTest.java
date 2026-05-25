package es.vargontoc.educational.framework.content.validation;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CuriosityValidatorTest {

    private final CuriosityValidator validator = new CuriosityValidator();

    @Test
    void validCuriosity_passes() {
        assertDoesNotThrow(() -> validator.validateForCreate("Las mariposas prueban con sus patas", 3, 6, "es-ES", ContentStatus.ACTIVE));
    }

    @Test
    void blankText_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(" ", 3, 6, "es-ES", ContentStatus.ACTIVE));
    }

    @Test
    void nullText_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(null, 3, 6, "es-ES", ContentStatus.ACTIVE));
    }

    @Test
    void textExceeds300Chars_throwsValidationException() {
        String longText = "a".repeat(301);
        assertThrows(ValidationException.class, () -> validator.validateForCreate(longText, 3, 6, "es-ES", ContentStatus.ACTIVE));
    }

    @Test
    void textExactly300Chars_passes() {
        String text300 = "a".repeat(300);
        assertDoesNotThrow(() -> validator.validateForCreate(text300, 3, 6, "es-ES", ContentStatus.ACTIVE));
    }

    @Test
    void blankLocale_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate("Some text", 3, 6, " ", ContentStatus.ACTIVE));
    }

    @Test
    void nullStatus_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate("Some text", 3, 6, "es-ES", null));
    }

    @Test
    void negativeMinAge_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate("Some text", -1, 6, "es-ES", ContentStatus.ACTIVE));
    }

    @Test
    void maxAgeLessThanMinAge_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate("Some text", 10, 5, "es-ES", ContentStatus.ACTIVE));
    }

    @Test
    void nullAges_passes() {
        assertDoesNotThrow(() -> validator.validateForCreate("Some text", null, null, "es-ES", ContentStatus.ACTIVE));
    }

    @Test
    void equalMinAndMaxAge_passes() {
        assertDoesNotThrow(() -> validator.validateForCreate("Some text", 5, 5, "es-ES", ContentStatus.ACTIVE));
    }
}
