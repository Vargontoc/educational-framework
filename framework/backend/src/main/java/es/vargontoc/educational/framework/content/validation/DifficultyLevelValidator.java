package es.vargontoc.educational.framework.content.validation;

import es.vargontoc.educational.framework.content.model.DifficultyCode;
import es.vargontoc.educational.framework.shared.validation.AbstractValidator;

public class DifficultyLevelValidator extends AbstractValidator<DifficultyLevelValidator.DifficultyLevelValidationInput> {

    @Override
    public void validate(DifficultyLevelValidationInput target) {
        requireNonNull(target.activityId(), "activityId");
        requireNonNull(target.difficultyCode(), "difficultyCode");
    }

    public void validateForCreate(Long activityId, DifficultyCode difficultyCode) {
        validate(new DifficultyLevelValidationInput(activityId, difficultyCode));
    }

    public void validateForUpdate(Long activityId, DifficultyCode difficultyCode) {
        validate(new DifficultyLevelValidationInput(activityId, difficultyCode));
    }

    public record DifficultyLevelValidationInput(Long activityId, DifficultyCode difficultyCode) {}
}
