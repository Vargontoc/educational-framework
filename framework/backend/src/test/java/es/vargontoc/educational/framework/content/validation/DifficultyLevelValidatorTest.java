package es.vargontoc.educational.framework.content.validation;

import es.vargontoc.educational.framework.content.model.DifficultyCode;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DifficultyLevelValidatorTest {

    private final DifficultyLevelValidator validator = new DifficultyLevelValidator();

    @Test
    void validDifficultyLevel_passes() {
        assertDoesNotThrow(() -> validator.validateForCreate(1L, DifficultyCode.EASY));
    }

    @Test
    void nullActivityId_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(null, DifficultyCode.EASY));
    }

    @Test
    void nullDifficultyCode_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(1L, null));
    }
}
