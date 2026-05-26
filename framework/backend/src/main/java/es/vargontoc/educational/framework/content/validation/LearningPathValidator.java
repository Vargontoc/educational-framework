package es.vargontoc.educational.framework.content.validation;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import es.vargontoc.educational.framework.shared.validation.AbstractValidator;

public class LearningPathValidator extends AbstractValidator<LearningPathValidator.LearningPathValidationInput> {

    @Override
    public void validate(LearningPathValidationInput target) {
        requireNonBlank(target.name(), "name");
        requireMaxLength(target.name(), 200, "name");
        requireNonBlank(target.locale(), "locale");
        requireNonNull(target.status(), "status");
        validateAgeRange(target.minAge(), target.maxAge());
    }

    public void validateForCreate(String name, Integer minAge, Integer maxAge, String locale, ContentStatus status) {
        validate(new LearningPathValidationInput(name, minAge, maxAge, locale, status));
    }

    public void validateForUpdate(String name, Integer minAge, Integer maxAge, String locale, ContentStatus status) {
        validate(new LearningPathValidationInput(name, minAge, maxAge, locale, status));
    }

    private void validateAgeRange(Integer minAge, Integer maxAge) {
        if (minAge != null && minAge < 0) {
            throw new ValidationException("minAge must be greater than or equal to 0");
        }
        if (maxAge != null && minAge != null && maxAge < minAge) {
            throw new ValidationException("maxAge must be greater than or equal to minAge");
        }
    }

    public record LearningPathValidationInput(String name, Integer minAge, Integer maxAge, String locale, ContentStatus status) {}
}
