package es.vargontoc.educational.framework.family.validation;

import es.vargontoc.educational.framework.shared.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChildProfileValidatorTest {

    private final ChildProfileValidator validator = new ChildProfileValidator();

    @Test
    void validInputs_passes() {
        var input = new ChildProfileValidator.ChildProfileValidationInput(
            "Kid",
            LocalDate.now().minusYears(10),
            "avatar"
        );
        assertDoesNotThrow(() -> validator.validate(input));
    }

    @Test
    void birthdayInFuture_throws() {
        var input = new ChildProfileValidator.ChildProfileValidationInput(
            "Kid",
            LocalDate.now().plusDays(1),
            null
        );
        assertThrows(ValidationException.class, () -> validator.validate(input));
    }

    @Test
    void birthdayOlderThan18Years_throws() {
        var input = new ChildProfileValidator.ChildProfileValidationInput(
            "Kid",
            LocalDate.now().minusYears(19),
            null
        );
        assertThrows(ValidationException.class, () -> validator.validate(input));
    }

    @Test
    void blankName_throws() {
        var input = new ChildProfileValidator.ChildProfileValidationInput(
            " ",
            LocalDate.now().minusYears(7),
            null
        );
        assertThrows(ValidationException.class, () -> validator.validate(input));
    }
}
