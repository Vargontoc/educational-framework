package es.vargontoc.educational.framework.content.validation;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CategoryValidatorTest {

    private final CategoryValidator validator = new CategoryValidator();

    @Test
    void validCategory_passes() {
        assertDoesNotThrow(() -> validator.validateForCreate("Mathematics", ContentStatus.ACTIVE));
    }

    @Test
    void blankName_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(" ", ContentStatus.ACTIVE));
    }

    @Test
    void nullName_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(null, ContentStatus.ACTIVE));
    }

    @Test
    void nameExceedsMaxLength_throwsValidationException() {
        String longName = "A".repeat(201);
        assertThrows(ValidationException.class, () -> validator.validateForCreate(longName, ContentStatus.ACTIVE));
    }

    @Test
    void nullStatus_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate("Mathematics", null));
    }

    @Test
    void nameAtMaxLength_passes() {
        String maxLengthName = "A".repeat(200);
        assertDoesNotThrow(() -> validator.validateForCreate(maxLengthName, ContentStatus.ACTIVE));
    }
}
