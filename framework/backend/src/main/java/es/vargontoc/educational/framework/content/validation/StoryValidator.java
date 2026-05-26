package es.vargontoc.educational.framework.content.validation;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import es.vargontoc.educational.framework.shared.validation.AbstractValidator;

public class StoryValidator extends AbstractValidator<StoryValidator.StoryValidationInput> {

    @Override
    public void validate(StoryValidationInput target) {
        requireNonBlank(target.title(), "title");
        requireMaxLength(target.title(), 200, "title");
        requireNonNull(target.status(), "status");
        validateAgeRange(target.minAge(), target.maxAge());
        validateDuration(target.estimatedDurationMinutes());
    }

    public void validateForCreate(String title, Integer minAge, Integer maxAge, Integer estimatedDurationMinutes, ContentStatus status) {
        validate(new StoryValidationInput(title, minAge, maxAge, estimatedDurationMinutes, status));
    }

    public void validateForUpdate(String title, Integer minAge, Integer maxAge, Integer estimatedDurationMinutes, ContentStatus status) {
        validate(new StoryValidationInput(title, minAge, maxAge, estimatedDurationMinutes, status));
    }

    private void validateAgeRange(Integer minAge, Integer maxAge) {
        if (minAge != null && minAge < 0) {
            throw new ValidationException("minAge must be greater than or equal to 0");
        }
        if (maxAge != null && minAge != null && maxAge < minAge) {
            throw new ValidationException("maxAge must be greater than or equal to minAge");
        }
    }

    private void validateDuration(Integer duration) {
        if (duration != null && duration < 1) {
            throw new ValidationException("estimatedDurationMinutes must be greater than 0");
        }
    }

    public record StoryValidationInput(String title, Integer minAge, Integer maxAge, Integer estimatedDurationMinutes, ContentStatus status) {}
}
