package es.vargontoc.educational.framework.content.validation;

import es.vargontoc.educational.framework.content.model.ResourceType;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ActivityResourceValidatorTest {

    private final ActivityResourceValidator validator = new ActivityResourceValidator();

    @Test
    void validResource_passes() {
        assertDoesNotThrow(() -> validator.validateForCreate(1L, ResourceType.IMAGE, "/images/game1.png"));
    }

    @Test
    void nullActivityId_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(null, ResourceType.IMAGE, "/images/game1.png"));
    }

    @Test
    void nullResourceType_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(1L, null, "/images/game1.png"));
    }

    @Test
    void blankPath_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(1L, ResourceType.IMAGE, " "));
    }

    @Test
    void nullPath_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(1L, ResourceType.IMAGE, null));
    }
}
