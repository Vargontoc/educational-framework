package es.vargontoc.educational.framework.content.validation;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TracingPatternValidatorTest {

    private final TracingPatternValidator validator = new TracingPatternValidator();

    @Test
    void validTracingPattern_passes() {
        var points = List.of(List.of(0.0, 0.0), List.of(0.5, 0.5), List.of(1.0, 1.0));
        assertDoesNotThrow(() -> validator.validateForCreate(1L, "Square Pattern", points, ContentStatus.ACTIVE));
    }

    @Test
    void nullTopicId_throwsValidationException() {
        var points = List.of(List.of(0.0, 0.0), List.of(1.0, 1.0));
        assertThrows(ValidationException.class, () -> validator.validateForCreate(null, "Pattern", points, ContentStatus.ACTIVE));
    }

    @Test
    void blankName_throwsValidationException() {
        var points = List.of(List.of(0.0, 0.0), List.of(1.0, 1.0));
        assertThrows(ValidationException.class, () -> validator.validateForCreate(1L, " ", points, ContentStatus.ACTIVE));
    }

    @Test
    void nullName_throwsValidationException() {
        var points = List.of(List.of(0.0, 0.0), List.of(1.0, 1.0));
        assertThrows(ValidationException.class, () -> validator.validateForCreate(1L, null, points, ContentStatus.ACTIVE));
    }

    @Test
    void nameExceeds200Chars_throwsValidationException() {
        String longName = "a".repeat(201);
        var points = List.of(List.of(0.0, 0.0), List.of(1.0, 1.0));
        assertThrows(ValidationException.class, () -> validator.validateForCreate(1L, longName, points, ContentStatus.ACTIVE));
    }

    @Test
    void nullStatus_throwsValidationException() {
        var points = List.of(List.of(0.0, 0.0), List.of(1.0, 1.0));
        assertThrows(ValidationException.class, () -> validator.validateForCreate(1L, "Pattern", points, null));
    }

    @Test
    void nullPoints_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(1L, "Pattern", null, ContentStatus.ACTIVE));
    }

    @Test
    void emptyPoints_throwsValidationException() {
        assertThrows(ValidationException.class, () -> validator.validateForCreate(1L, "Pattern", Collections.emptyList(), ContentStatus.ACTIVE));
    }

    @Test
    void pointWithOneCoordinate_throwsValidationException() {
        var points = List.of(List.of(0.5));
        assertThrows(ValidationException.class, () -> validator.validateForCreate(1L, "Pattern", points, ContentStatus.ACTIVE));
    }

    @Test
    void pointWithThreeCoordinates_throwsValidationException() {
        var points = List.of(List.of(0.5, 0.5, 0.5));
        assertThrows(ValidationException.class, () -> validator.validateForCreate(1L, "Pattern", points, ContentStatus.ACTIVE));
    }

    @Test
    void pointXBelowZero_throwsValidationException() {
        var points = List.of(List.of(-0.1, 0.5));
        assertThrows(ValidationException.class, () -> validator.validateForCreate(1L, "Pattern", points, ContentStatus.ACTIVE));
    }

    @Test
    void pointXAboveOne_throwsValidationException() {
        var points = List.of(List.of(1.1, 0.5));
        assertThrows(ValidationException.class, () -> validator.validateForCreate(1L, "Pattern", points, ContentStatus.ACTIVE));
    }

    @Test
    void pointYBelowZero_throwsValidationException() {
        var points = List.of(List.of(0.5, -0.1));
        assertThrows(ValidationException.class, () -> validator.validateForCreate(1L, "Pattern", points, ContentStatus.ACTIVE));
    }

    @Test
    void pointYAboveOne_throwsValidationException() {
        var points = List.of(List.of(0.5, 1.1));
        assertThrows(ValidationException.class, () -> validator.validateForCreate(1L, "Pattern", points, ContentStatus.ACTIVE));
    }

    @Test
    void boundaryPoints_passes() {
        var points = List.of(List.of(0.0, 0.0), List.of(1.0, 1.0));
        assertDoesNotThrow(() -> validator.validateForCreate(1L, "Pattern", points, ContentStatus.ACTIVE));
    }
}
