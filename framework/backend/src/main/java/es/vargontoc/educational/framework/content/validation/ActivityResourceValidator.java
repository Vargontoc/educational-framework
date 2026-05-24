package es.vargontoc.educational.framework.content.validation;

import es.vargontoc.educational.framework.content.model.ResourceType;
import es.vargontoc.educational.framework.shared.validation.AbstractValidator;

public class ActivityResourceValidator extends AbstractValidator<ActivityResourceValidator.ActivityResourceValidationInput> {

    @Override
    public void validate(ActivityResourceValidationInput target) {
        requireNonNull(target.activityId(), "activityId");
        requireNonNull(target.resourceType(), "resourceType");
        requireNonBlank(target.path(), "path");
    }

    public void validateForCreate(Long activityId, ResourceType resourceType, String path) {
        validate(new ActivityResourceValidationInput(activityId, resourceType, path));
    }

    public void validateForUpdate(Long activityId, ResourceType resourceType, String path) {
        validate(new ActivityResourceValidationInput(activityId, resourceType, path));
    }

    public record ActivityResourceValidationInput(Long activityId, ResourceType resourceType, String path) {}
}
