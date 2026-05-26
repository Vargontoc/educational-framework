package es.vargontoc.educational.framework.content.validation;

import es.vargontoc.educational.framework.shared.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LearningPathStepValidatorTest {

    private final LearningPathStepValidator validator = new LearningPathStepValidator();

    @Test
    void validStep_passes() {
        assertDoesNotThrow(() -> validator.validateForCreate(1L, 1L, 1));
    }

    @Test
    void nullLearningPathId_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(null, 1L, 1));
    }

    @Test
    void nullActivityId_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(1L, null, 1));
    }

    @Test
    void nullStepOrder_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(1L, 1L, null));
    }

    @Test
    void stepOrderZero_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(1L, 1L, 0));
    }

    @Test
    void stepOrderNegative_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(1L, 1L, -1));
    }

    @Test
    void stepOrderOne_passes() {
        assertDoesNotThrow(() -> validator.validateForCreate(1L, 1L, 1));
    }
}
