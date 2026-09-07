package es.vargontoc.educational.framework.content.validation;

import es.vargontoc.educational.framework.audio.domain.enums.TonePreset;
import es.vargontoc.educational.framework.avatar.domain.enums.AvatarEventType;
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
            AvatarEventType.ACTIVITY_COMPLETED, TonePreset.ADVENTURE, "Has completado la actividad!", "es-ES", ContentStatus.ACTIVE));
    }

    @Test
    void nullEventType_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
            null, TonePreset.ADVENTURE, "Some message", "es-ES", ContentStatus.ACTIVE));
    }

    @Test
    void nullTone_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
            AvatarEventType.ACTIVITY_COMPLETED, null, "Some message", "es-ES", ContentStatus.ACTIVE));
    }

    @Test
    void blankMessageText_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
            AvatarEventType.ACTIVITY_COMPLETED, TonePreset.ADVENTURE, " ", "es-ES", ContentStatus.ACTIVE));
    }

    @Test
    void nullMessageText_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
            AvatarEventType.ACTIVITY_COMPLETED, TonePreset.ADVENTURE, null, "es-ES", ContentStatus.ACTIVE));
    }

    @Test
    void messageTextExceeds300Chars_throwsValidationException() {
        String longMessage = "a".repeat(301);
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
            AvatarEventType.ACTIVITY_COMPLETED, TonePreset.ADVENTURE, longMessage, "es-ES", ContentStatus.ACTIVE));
    }

    @Test
    void messageTextExactly300Chars_passes() {
        String message300 = "a".repeat(300);
        assertDoesNotThrow(() -> validator.validateForCreate(
            AvatarEventType.ACTIVITY_COMPLETED, TonePreset.ADVENTURE, message300, "es-ES", ContentStatus.ACTIVE));
    }

    @Test
    void blankLocale_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
            AvatarEventType.ACTIVITY_COMPLETED, TonePreset.ADVENTURE, "Some message", " ", ContentStatus.ACTIVE));
    }

    @Test
    void nullStatus_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
            AvatarEventType.ACTIVITY_COMPLETED, TonePreset.ADVENTURE, "Some message", "es-ES", null));
    }

    @Test
    void allEventTypes_pass() {
        for (AvatarEventType eventType : AvatarEventType.values()) {
            assertDoesNotThrow(() -> validator.validateForCreate(
                eventType, TonePreset.NEUTRAL, "Some message", "es-ES", ContentStatus.ACTIVE));
        }
    }

    @Test
    void allTones_pass() {
        for (TonePreset tone : TonePreset.values()) {
            assertDoesNotThrow(() -> validator.validateForCreate(
                AvatarEventType.ACTIVITY_COMPLETED, tone, "Some message", "es-ES", ContentStatus.ACTIVE));
        }
    }
}
