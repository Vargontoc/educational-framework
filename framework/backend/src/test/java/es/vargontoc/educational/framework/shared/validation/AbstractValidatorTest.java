package es.vargontoc.educational.framework.shared.validation;

import es.vargontoc.educational.framework.shared.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbstractValidatorTest {

    private TestValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TestValidator();
    }

    @Test
    void requireNonNull_throwsOnNull() {
        assertThrows(ValidationException.class, () -> validator.checkNonNull(null, "field"));
    }

    @Test
    void requireNonNull_passesOnValue() {
        assertDoesNotThrow(() -> validator.checkNonNull("value", "field"));
    }

    @Test
    void requireNonBlank_throwsOnNull() {
        assertThrows(ValidationException.class, () -> validator.checkNonBlank(null, "field"));
    }

    @Test
    void requireNonBlank_throwsOnBlank() {
        assertThrows(ValidationException.class, () -> validator.checkNonBlank("   ", "field"));
    }

    @Test
    void requireNonBlank_passesOnNonBlank() {
        assertDoesNotThrow(() -> validator.checkNonBlank("ok", "field"));
    }

    @Test
    void requireMaxLength_throwsWhenExceeded() {
        var ex = assertThrows(ValidationException.class,
            () -> validator.checkMaxLength("toolong", 3, "field"));
        assertTrue(ex.getMessage().contains("3"));
    }

    @Test
    void requireMaxLength_passesOnExactLength() {
        assertDoesNotThrow(() -> validator.checkMaxLength("abc", 3, "field"));
    }

    @Test
    void requirePositive_throwsOnZero() {
        assertThrows(ValidationException.class, () -> validator.checkPositive(0, "field"));
    }

    @Test
    void requirePositive_throwsOnNegative() {
        assertThrows(ValidationException.class, () -> validator.checkPositive(-1, "field"));
    }

    @Test
    void requirePositive_passesOnPositive() {
        assertDoesNotThrow(() -> validator.checkPositive(1, "field"));
    }

    private static class TestValidator extends AbstractValidator<Void> {
        @Override
        public void validate(Void target) {}

        void checkNonNull(Object v, String f)       { requireNonNull(v, f); }
        void checkNonBlank(String v, String f)      { requireNonBlank(v, f); }
        void checkMaxLength(String v, int m, String f) { requireMaxLength(v, m, f); }
        void checkPositive(Number v, String f)      { requirePositive(v, f); }
    }
}
