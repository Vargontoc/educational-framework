package es.vargontoc.educational.framework.family.validation;

import es.vargontoc.educational.framework.shared.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdultProfileValidatorTest {

    private final AdultProfileValidator validator = new AdultProfileValidator();

    @Test
    void validInputs_passes() {
        var input = new AdultProfileValidator.AdultProfileValidationInput(
            "Parent",
            LocalDate.now().minusYears(30),
            "avatar"
        );
        assertDoesNotThrow(() -> validator.validate(input));
    }

    @Test
    void birthdayInFuture_throws() {
        var input = new AdultProfileValidator.AdultProfileValidationInput(
            "Parent",
            LocalDate.now().plusDays(1),
            null
        );
        assertThrows(ValidationException.class, () -> validator.validate(input));
    }

    @Test
    void blankName_throws() {
        var input = new AdultProfileValidator.AdultProfileValidationInput(
            " ",
            LocalDate.now().minusYears(25),
            null
        );
        assertThrows(ValidationException.class, () -> validator.validate(input));
    }
}
