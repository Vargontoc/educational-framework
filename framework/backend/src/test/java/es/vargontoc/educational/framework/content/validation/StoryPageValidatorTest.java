package es.vargontoc.educational.framework.content.validation;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StoryPageValidatorTest {

    private final StoryPageValidator validator = new StoryPageValidator();

    @Test
    void validPage_passes() {
        assertDoesNotThrow(() -> validator.validateForCreate(1L, 1, "Once upon a time...", ContentStatus.ACTIVE));
    }

    @Test
    void nullStoryId_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(null, 1, "Text", ContentStatus.ACTIVE));
    }

    @Test
    void nullPageOrder_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(1L, null, "Text", ContentStatus.ACTIVE));
    }

    @Test
    void pageOrderZero_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(1L, 0, "Text", ContentStatus.ACTIVE));
    }

    @Test
    void pageOrderNegative_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(1L, -1, "Text", ContentStatus.ACTIVE));
    }

    @Test
    void nullStatus_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(1L, 1, "Text", null));
    }

    @Test
    void textExceeds1000Chars_throwsValidationException() {
        String longText = "a".repeat(1001);
        assertThrows(ValidationException.class, () -> validator.validateForCreate(1L, 1, longText, ContentStatus.ACTIVE));
    }

    @Test
    void textExactly1000Chars_passes() {
        String text1000 = "a".repeat(1000);
        assertDoesNotThrow(() -> validator.validateForCreate(1L, 1, text1000, ContentStatus.ACTIVE));
    }

    @Test
    void nullText_passes() {
        assertDoesNotThrow(() -> validator.validateForCreate(1L, 1, null, ContentStatus.ACTIVE));
    }
}
