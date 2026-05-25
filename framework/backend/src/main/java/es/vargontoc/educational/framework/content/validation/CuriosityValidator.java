package es.vargontoc.educational.framework.content.validation;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import es.vargontoc.educational.framework.shared.validation.AbstractValidator;

public class CuriosityValidator extends AbstractValidator<CuriosityValidator.CuriosityValidationInput> {

    @Override
    public void validate(CuriosityValidationInput target) {
        requireNonBlank(target.text(), "text");
        requireMaxLength(target.text(), 300, "text");
        requireNonBlank(target.locale(), "locale");
        requireNonNull(target.status(), "status");
        validateAgeRange(target.minAge(), target.maxAge());
    }

    public void validateForCreate(String text, Integer minAge, Integer maxAge, String locale, ContentStatus status) {
        validate(new CuriosityValidationInput(text, minAge, maxAge, locale, status));
    }

    public void validateForUpdate(String text, Integer minAge, Integer maxAge, String locale, ContentStatus status) {
        validate(new CuriosityValidationInput(text, minAge, maxAge, locale, status));
    }

    private void validateAgeRange(Integer minAge, Integer maxAge) {
        if (minAge != null && minAge < 0) {
            throw new ValidationException("minAge must be greater than or equal to 0");
        }
        if (maxAge != null && minAge != null && maxAge < minAge) {
            throw new ValidationException("maxAge must be greater than or equal to minAge");
        }
    }

    public record CuriosityValidationInput(String text, Integer minAge, Integer maxAge, String locale, ContentStatus status) {}
}
