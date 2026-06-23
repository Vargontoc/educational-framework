package es.vargontoc.educational.framework.content.validation;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.SituationType;
import es.vargontoc.educational.framework.content.model.Tone;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorldNarrativeSituationValidatorTest {

    private WorldNarrativeSituationValidator validator;

    @BeforeEach
    void setUp() {
        validator = new WorldNarrativeSituationValidator();
    }

    @Test
    void validWorldNarrativeSituation_passes() {
        assertDoesNotThrow(() -> validator.validateForCreate(
                "HOST_FOUND_SOMETHING",
                "The dog found something interesting!",
                SituationType.FOUND_OBJECT,
                Tone.JOYFUL,
                3,
                4,
                ContentStatus.ACTIVE
        ));
    }

    @Test
    void nullCode_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
                null,
                "The dog found something interesting!",
                SituationType.FOUND_OBJECT,
                Tone.JOYFUL,
                3,
                4,
                ContentStatus.ACTIVE
        ));
    }

    @Test
    void blankCode_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
                "   ",
                "The dog found something interesting!",
                SituationType.FOUND_OBJECT,
                Tone.JOYFUL,
                3,
                4,
                ContentStatus.ACTIVE
        ));
    }

    @Test
    void nullDisplayText_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
                "HOST_FOUND_SOMETHING",
                null,
                SituationType.FOUND_OBJECT,
                Tone.JOYFUL,
                3,
                4,
                ContentStatus.ACTIVE
        ));
    }

    @Test
    void nullSituationType_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
                "HOST_FOUND_SOMETHING",
                "The dog found something interesting!",
                null,
                Tone.JOYFUL,
                3,
                4,
                ContentStatus.ACTIVE
        ));
    }

    @Test
    void nullStatus_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
                "HOST_FOUND_SOMETHING",
                "The dog found something interesting!",
                SituationType.FOUND_OBJECT,
                Tone.JOYFUL,
                3,
                4,
                null
        ));
    }

    @Test
    void maxAgeLessThanMinAge_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
                "HOST_FOUND_SOMETHING",
                "The dog found something interesting!",
                SituationType.FOUND_OBJECT,
                Tone.JOYFUL,
                5,
                4,
                ContentStatus.ACTIVE
        ));
    }

    @Test
    void negativeMinAge_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(
                "HOST_FOUND_SOMETHING",
                "The dog found something interesting!",
                SituationType.FOUND_OBJECT,
                Tone.JOYFUL,
                -1,
                4,
                ContentStatus.ACTIVE
        ));
    }

    @Test
    void nullTone_passes() {
        assertDoesNotThrow(() -> validator.validateForCreate(
                "HOST_FOUND_SOMETHING",
                "The dog found something interesting!",
                SituationType.FOUND_OBJECT,
                null,
                3,
                4,
                ContentStatus.ACTIVE
        ));
    }
}
