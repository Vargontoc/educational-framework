package es.vargontoc.educational.framework.content.validation;

import es.vargontoc.educational.framework.content.model.EntityType;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ContentLocaleValidatorTest {

    private final ContentLocaleValidator validator = new ContentLocaleValidator();

    @Test
    void validLocale_passes() {
        assertDoesNotThrow(() -> validator.validateForCreate(EntityType.CATEGORY, 1L, "es-ES", "Matemáticas"));
    }

    @Test
    void nullEntityType_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(null, 1L, "es-ES", "Matemáticas"));
    }

    @Test
    void nullEntityId_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(EntityType.CATEGORY, null, "es-ES", "Matemáticas"));
    }

    @Test
    void blankLocaleCode_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(EntityType.CATEGORY, 1L, " ", "Matemáticas"));
    }

    @Test
    void blankName_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(EntityType.CATEGORY, 1L, "es-ES", " "));
    }

    @Test
    void unsupportedLocale_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(EntityType.CATEGORY, 1L, "en-US", "Mathematics"));
    }

    @Test
    void invalidLocaleFormat_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(EntityType.CATEGORY, 1L, "es", "Mathematics"));
    }
}
