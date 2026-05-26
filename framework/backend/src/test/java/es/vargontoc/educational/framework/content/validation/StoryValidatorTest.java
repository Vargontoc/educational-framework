package es.vargontoc.educational.framework.content.validation;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StoryValidatorTest {

    private final StoryValidator validator = new StoryValidator();

    @Test
    void validStory_passes() {
        assertDoesNotThrow(() -> validator.validateForCreate("The Little Star", 3, 6, 5, ContentStatus.ACTIVE));
    }

    @Test
    void blankTitle_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(" ", 3, 6, 5, ContentStatus.ACTIVE));
    }

    @Test
    void nullTitle_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(null, 3, 6, 5, ContentStatus.ACTIVE));
    }

    @Test
    void titleExceeds200Chars_throwsValidationException() {
        String longTitle = "a".repeat(201);
        assertThrows(ValidationException.class, () -> validator.validateForCreate(longTitle, 3, 6, 5, ContentStatus.ACTIVE));
    }

    @Test
    void nullStatus_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate("Title", 3, 6, 5, null));
    }

    @Test
    void negativeMinAge_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate("Title", -1, 6, 5, ContentStatus.ACTIVE));
    }

    @Test
    void maxAgeLessThanMinAge_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate("Title", 10, 5, 5, ContentStatus.ACTIVE));
    }

    @Test
    void nullAges_passes() {
        assertDoesNotThrow(() -> validator.validateForCreate("Title", null, null, 5, ContentStatus.ACTIVE));
    }

    @Test
    void zeroDuration_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate("Title", 3, 6, 0, ContentStatus.ACTIVE));
    }

    @Test
    void negativeDuration_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate("Title", 3, 6, -1, ContentStatus.ACTIVE));
    }

    @Test
    void nullDuration_passes() {
        assertDoesNotThrow(() -> validator.validateForCreate("Title", 3, 6, null, ContentStatus.ACTIVE));
    }
}
