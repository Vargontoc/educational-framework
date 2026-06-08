package es.vargontoc.educational.framework.avatar.validation;

import es.vargontoc.educational.framework.avatar.infrastructure.dto.AvatarEventRequest;
import es.vargontoc.educational.framework.content.model.AvatarEventType;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AvatarValidatorTest {

    private final AvatarValidator validator = new AvatarValidator();

    @Test
    void validRequest_passes() {
        AvatarEventRequest request = new AvatarEventRequest(1L, AvatarEventType.ACTIVITY_COMPLETED, "es-ES", null);
        assertDoesNotThrow(() -> validator.validateForProcess(request));
    }

    @Test
    void nullChildSessionId_throwsValidationException() {
        AvatarEventRequest request = new AvatarEventRequest(null, AvatarEventType.ACTIVITY_COMPLETED, "es-ES", null);
        assertThrows(ValidationException.class, () -> validator.validateForProcess(request));
    }

    @Test
    void nullEventType_throwsValidationException() {
        AvatarEventRequest request = new AvatarEventRequest(1L, null, "es-ES", null);
        assertThrows(ValidationException.class, () -> validator.validateForProcess(request));
    }

    @Test
    void blankLocale_throwsValidationException() {
        AvatarEventRequest request = new AvatarEventRequest(1L, AvatarEventType.ACTIVITY_COMPLETED, "  ", null);
        assertThrows(ValidationException.class, () -> validator.validateForProcess(request));
    }

    @Test
    void nullLocale_throwsValidationException() {
        AvatarEventRequest request = new AvatarEventRequest(1L, AvatarEventType.ACTIVITY_COMPLETED, null, null);
        assertThrows(ValidationException.class, () -> validator.validateForProcess(request));
    }

    @Test
    void allEventTypes_pass() {
        for (AvatarEventType eventType : AvatarEventType.values()) {
            AvatarEventRequest request = new AvatarEventRequest(1L, eventType, "es-ES", null);
            assertDoesNotThrow(() -> validator.validateForProcess(request));
        }
    }
}