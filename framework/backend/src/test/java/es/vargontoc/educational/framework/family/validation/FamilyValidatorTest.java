package es.vargontoc.educational.framework.family.validation;

import es.vargontoc.educational.framework.shared.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FamilyValidatorTest {

    private final FamilyValidator validator = new FamilyValidator();

    @Test
    void validNameAndPin_passes() {
        assertDoesNotThrow(() -> validator.validateForCreate("My Family", "1234"));
    }

    @Test
    void blankName_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(" ", "1234"));
    }

    @Test
    void pinNotFourDigits_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate("My Family", "123"));
    }

    @Test
    void pinWithLetters_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate("My Family", "12a4"));
    }

    @Test
    void updateWithNullName_doesNotThrow() {
        assertDoesNotThrow(() -> validator.validateForUpdate(null, null));
    }

    @Test
    void updateWithValidName_doesNotThrow() {
        assertDoesNotThrow(() -> validator.validateForUpdate("Updated Name", null));
    }

    @Test
    void updateWithInvalidPin_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForUpdate(null, "123"));
    }

    @Test
    void updateWithValidPin_doesNotThrow() {
        assertDoesNotThrow(() -> validator.validateForUpdate(null, "5678"));
    }
}
