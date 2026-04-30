package es.vargontoc.educational.framework.shared.validation;

import es.vargontoc.educational.framework.shared.exception.ValidationException;

public abstract class AbstractValidator<T> implements IValidator<T> {

    protected void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new ValidationException(fieldName + " must not be null");
        }
    }

    protected void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " must not be blank");
        }
    }

    protected void requireMaxLength(String value, int max, String fieldName) {
        if (value != null && value.length() > max) {
            throw new ValidationException(
                fieldName + " must not exceed " + max + " characters"
            );
        }
    }

    protected void requirePositive(Number value, String fieldName) {
        if (value == null || value.doubleValue() <= 0) {
            throw new ValidationException(fieldName + " must be a positive number");
        }
    }
}
