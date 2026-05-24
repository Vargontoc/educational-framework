package es.vargontoc.educational.framework.content.validation;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ActivityValidatorTest {

    private final ActivityValidator validator = new ActivityValidator();

    @Test
    void validActivity_passes() {
        assertDoesNotThrow(() -> validator.validateForCreate("Counting Game", ContentStatus.ACTIVE, 5, 10));
    }

    @Test
    void blankName_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(" ", ContentStatus.ACTIVE, 5, 10));
    }

    @Test
    void nullStatus_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate("Counting Game", null, 5, 10));
    }

    @Test
    void negativeMinAge_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate("Counting Game", ContentStatus.ACTIVE, -1, 10));
    }

    @Test
    void maxAgeLessThanMinAge_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate("Counting Game", ContentStatus.ACTIVE, 10, 5));
    }

    @Test
    void nullAges_passes() {
        assertDoesNotThrow(() -> validator.validateForCreate("Counting Game", ContentStatus.ACTIVE, null, null));
    }
}
