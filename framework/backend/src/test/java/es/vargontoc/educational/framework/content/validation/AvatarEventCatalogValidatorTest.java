package es.vargontoc.educational.framework.content.validation;

import es.vargontoc.educational.framework.content.model.AvatarEventType;
import es.vargontoc.educational.framework.content.model.AvatarTone;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AvatarEventCatalogValidatorTest {

    private final AvatarEventCatalogValidator validator = new AvatarEventCatalogValidator();

    @Test
    void validAvatarEvent_passes() {
        assertDoesNotThrow(() -> validator.validateForCreate(
            AvatarEventType.ACTIVITY_COMPLETED, AvatarTone.JOYFUL, "Has completado la actividad!", "es-ES", ContentStatus.ACTIVE));
    }

    @Test
    void nullEventType_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
            null, AvatarTone.JOYFUL, "Some message", "es-ES", ContentStatus.ACTIVE));
    }

    @Test
    void nullTone_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
            AvatarEventType.ACTIVITY_COMPLETED, null, "Some message", "es-ES", ContentStatus.ACTIVE));
    }

    @Test
    void blankMessageText_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
            AvatarEventType.ACTIVITY_COMPLETED, AvatarTone.JOYFUL, " ", "es-ES", ContentStatus.ACTIVE));
    }

    @Test
    void nullMessageText_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
            AvatarEventType.ACTIVITY_COMPLETED, AvatarTone.JOYFUL, null, "es-ES", ContentStatus.ACTIVE));
    }

    @Test
    void messageTextExceeds300Chars_throwsValidationException() {
        String longMessage = "a".repeat(301);
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
            AvatarEventType.ACTIVITY_COMPLETED, AvatarTone.JOYFUL, longMessage, "es-ES", ContentStatus.ACTIVE));
    }

    @Test
    void messageTextExactly300Chars_passes() {
        String message300 = "a".repeat(300);
        assertDoesNotThrow(() -> validator.validateForCreate(
            AvatarEventType.ACTIVITY_COMPLETED, AvatarTone.JOYFUL, message300, "es-ES", ContentStatus.ACTIVE));
    }

    @Test
    void blankLocale_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
            AvatarEventType.ACTIVITY_COMPLETED, AvatarTone.JOYFUL, "Some message", " ", ContentStatus.ACTIVE));
    }

    @Test
    void nullStatus_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
            AvatarEventType.ACTIVITY_COMPLETED, AvatarTone.JOYFUL, "Some message", "es-ES", null));
    }

    @Test
    void allEventTypes_pass() {
        for (AvatarEventType eventType : AvatarEventType.values()) {
            assertDoesNotThrow(() -> validator.validateForCreate(
                eventType, AvatarTone.NEUTRAL, "Some message", "es-ES", ContentStatus.ACTIVE));
        }
    }

    @Test
    void allTones_pass() {
        for (AvatarTone tone : AvatarTone.values()) {
            assertDoesNotThrow(() -> validator.validateForCreate(
                AvatarEventType.ACTIVITY_COMPLETED, tone, "Some message", "es-ES", ContentStatus.ACTIVE));
        }
    }
}
