package es.vargontoc.educational.framework.content.validation;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import es.vargontoc.educational.framework.shared.validation.AbstractValidator;

public class CategoryValidator extends AbstractValidator<CategoryValidator.CategoryValidationInput> {

    @Override
    public void validate(CategoryValidationInput target) {
        requireNonBlank(target.name(), "name");
        requireMaxLength(target.name(), 200, "name");
        requireNonNull(target.status(), "status");
    }

    public void validateForCreate(String name, ContentStatus status) {
        validate(new CategoryValidationInput(name, status));
    }

    public void validateForUpdate(String name, ContentStatus status) {
        validate(new CategoryValidationInput(name, status));
    }

    public record CategoryValidationInput(String name, ContentStatus status) {}
}
