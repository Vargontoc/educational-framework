package es.vargontoc.educational.framework.avatar.validation;

import es.vargontoc.educational.framework.avatar.domain.AvatarEventRequest;
import es.vargontoc.educational.framework.avatar.domain.enums.AvatarEventType;
import es.vargontoc.educational.framework.avatar.infrastructure.validation.AvatarValidator;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AvatarValidatorTest {

    private final AvatarValidator validator = new AvatarValidator();

    @Test
    void validRequest_passes() {
        AvatarEventRequest request = new AvatarEventRequest(1L, AvatarEventType.ACTIVITY_COMPLETED, null);
        assertDoesNotThrow(() -> validator.validateForProcess(request));
    }

    @Test
    void nullChildSessionId_throwsValidationException() {
        AvatarEventRequest request = new AvatarEventRequest(null, AvatarEventType.ACTIVITY_COMPLETED, null);
        assertThrows(ValidationException.class, () -> validator.validateForProcess(request));
    }

    @Test
    void nullEventType_throwsValidationException() {
        AvatarEventRequest request = new AvatarEventRequest(1L, null, null);
        assertThrows(ValidationException.class, () -> validator.validateForProcess(request));
    }

    @Test
    void allEventTypes_pass() {
        for (AvatarEventType eventType : AvatarEventType.values()) {
            AvatarEventRequest request = new AvatarEventRequest(1L, eventType, null);
            assertDoesNotThrow(() -> validator.validateForProcess(request));
        }
    }
}
