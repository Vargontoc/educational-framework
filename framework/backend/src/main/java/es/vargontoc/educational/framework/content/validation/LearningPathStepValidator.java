package es.vargontoc.educational.framework.content.validation;

import es.vargontoc.educational.framework.shared.exception.ValidationException;
import es.vargontoc.educational.framework.shared.validation.AbstractValidator;

public class LearningPathStepValidator extends AbstractValidator<LearningPathStepValidator.LearningPathStepValidationInput> {

    @Override
    public void validate(LearningPathStepValidationInput target) {
        requireNonNull(target.learningPathId(), "learningPathId");
        requireNonNull(target.activityId(), "activityId");
        requireNonNull(target.stepOrder(), "stepOrder");
        validateStepOrder(target.stepOrder());
    }

    public void validateForCreate(Long learningPathId, Long activityId, Integer stepOrder) {
        validate(new LearningPathStepValidationInput(learningPathId, activityId, stepOrder));
    }

    public void validateForUpdate(Long learningPathId, Long activityId, Integer stepOrder) {
        validate(new LearningPathStepValidationInput(learningPathId, activityId, stepOrder));
    }

    private void validateStepOrder(Integer stepOrder) {
        if (stepOrder != null && stepOrder < 1) {
            throw new ValidationException("stepOrder must be greater than or equal to 1");
        }
    }

    public record LearningPathStepValidationInput(Long learningPathId, Long activityId, Integer stepOrder) {}
}
