package es.vargontoc.educational.framework.content.validation;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TopicValidatorTest {

    private final TopicValidator validator = new TopicValidator();

    @Test
    void validTopic_passes() {
        assertDoesNotThrow(() -> validator.validateForCreate("Addition", 1L, ContentStatus.ACTIVE, 5, 10));
    }

    @Test
    void blankName_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(" ", 1L, ContentStatus.ACTIVE, 5, 10));
    }

    @Test
    void nullCategoryId_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate("Addition", null, ContentStatus.ACTIVE, 5, 10));
    }

    @Test
    void nullStatus_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate("Addition", 1L, null, 5, 10));
    }

    @Test
    void negativeMinAge_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate("Addition", 1L, ContentStatus.ACTIVE, -1, 10));
    }

    @Test
    void maxAgeLessThanMinAge_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate("Addition", 1L, ContentStatus.ACTIVE, 10, 5));
    }

    @Test
    void nullAges_passes() {
        assertDoesNotThrow(() -> validator.validateForCreate("Addition", 1L, ContentStatus.ACTIVE, null, null));
    }

    @Test
    void equalMinAndMaxAge_passes() {
        assertDoesNotThrow(() -> validator.validateForCreate("Addition", 1L, ContentStatus.ACTIVE, 5, 5));
    }
}
