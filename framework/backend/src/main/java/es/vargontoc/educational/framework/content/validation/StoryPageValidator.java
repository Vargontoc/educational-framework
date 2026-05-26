package es.vargontoc.educational.framework.content.validation;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import es.vargontoc.educational.framework.shared.validation.AbstractValidator;

public class StoryPageValidator extends AbstractValidator<StoryPageValidator.StoryPageValidationInput> {

    @Override
    public void validate(StoryPageValidationInput target) {
        requireNonNull(target.storyId(), "storyId");
        requireNonNull(target.pageOrder(), "pageOrder");
        requireNonNull(target.status(), "status");
        validatePageOrder(target.pageOrder());
        validateText(target.text());
    }

    public void validateForCreate(Long storyId, Integer pageOrder, String text, ContentStatus status) {
        validate(new StoryPageValidationInput(storyId, pageOrder, text, status));
    }

    public void validateForUpdate(Long storyId, Integer pageOrder, String text, ContentStatus status) {
        validate(new StoryPageValidationInput(storyId, pageOrder, text, status));
    }

    private void validatePageOrder(Integer pageOrder) {
        if (pageOrder != null && pageOrder < 1) {
            throw new ValidationException("pageOrder must be greater than or equal to 1");
        }
    }

    private void validateText(String text) {
        if (text != null && text.length() > 1000) {
            throw new ValidationException("text must not exceed 1000 characters");
        }
    }

    public record StoryPageValidationInput(Long storyId, Integer pageOrder, String text, ContentStatus status) {}
}
