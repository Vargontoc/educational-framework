package es.vargontoc.educational.framework.content.validation;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import es.vargontoc.educational.framework.shared.validation.AbstractValidator;

public class TopicValidator extends AbstractValidator<TopicValidator.TopicValidationInput> {

    @Override
    public void validate(TopicValidationInput target) {
        requireNonBlank(target.name(), "name");
        requireMaxLength(target.name(), 200, "name");
        requireNonNull(target.categoryId(), "categoryId");
        requireNonNull(target.status(), "status");
        validateAgeRange(target.minAge(), target.maxAge());
    }

    public void validateForCreate(String name, Long categoryId, ContentStatus status, Integer minAge, Integer maxAge) {
        validate(new TopicValidationInput(name, categoryId, status, minAge, maxAge));
    }

    public void validateForUpdate(String name, Long categoryId, ContentStatus status, Integer minAge, Integer maxAge) {
        validate(new TopicValidationInput(name, categoryId, status, minAge, maxAge));
    }

    private void validateAgeRange(Integer minAge, Integer maxAge) {
        if (minAge != null && minAge < 0) {
            throw new ValidationException("minAge must be greater than or equal to 0");
        }
        if (maxAge != null && minAge != null && maxAge < minAge) {
            throw new ValidationException("maxAge must be greater than or equal to minAge");
        }
    }

    public record TopicValidationInput(String name, Long categoryId, ContentStatus status, Integer minAge, Integer maxAge) {}
}
