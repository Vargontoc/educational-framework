package es.vargontoc.educational.framework.content.validation;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.SituationType;
import es.vargontoc.educational.framework.content.model.Tone;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import es.vargontoc.educational.framework.shared.validation.AbstractValidator;

public class WorldNarrativeSituationValidator extends AbstractValidator<WorldNarrativeSituationValidator.WorldNarrativeSituationValidationInput> {

    @Override
    public void validate(WorldNarrativeSituationValidationInput target) {
        requireNonBlank(target.code(), "code");
        requireNonBlank(target.displayText(), "displayText");
        requireNonNull(target.situationType(), "situationType");
        requireNonNull(target.status(), "status");
        validateAgeRange(target.minAge(), target.maxAge());
    }

    public void validateForCreate(String code, String displayText, SituationType situationType,
                                  Tone tone, Integer minAge, Integer maxAge, ContentStatus status) {
        validate(new WorldNarrativeSituationValidationInput(code, displayText, situationType, tone, minAge, maxAge, status));
    }

    private void validateAgeRange(Integer minAge, Integer maxAge) {
        if (minAge != null && minAge < 0) {
            throw new ValidationException("minAge must be greater than or equal to 0");
        }
        if (maxAge != null && minAge != null && maxAge < minAge) {
            throw new ValidationException("maxAge must be greater than or equal to minAge");
        }
    }

    public record WorldNarrativeSituationValidationInput(String code, String displayText, SituationType situationType,
                                                          Tone tone, Integer minAge, Integer maxAge,
                                                          ContentStatus status) {}
}
