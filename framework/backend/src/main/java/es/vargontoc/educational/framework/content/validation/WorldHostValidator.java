package es.vargontoc.educational.framework.content.validation;

import es.vargontoc.educational.framework.content.model.Biome;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import es.vargontoc.educational.framework.shared.validation.AbstractValidator;

public class WorldHostValidator extends AbstractValidator<WorldHostValidator.WorldHostValidationInput> {

    @Override
    public void validate(WorldHostValidationInput target) {
        requireNonBlank(target.code(), "code");
        requireNonBlank(target.displayName(), "displayName");
        requireNonNull(target.biome(), "biome");
        requireNonNull(target.status(), "status");
        validateAgeRange(target.minAge(), target.maxAge());
    }

    public void validateForCreate(String code, String displayName, Biome biome,
                                   Integer minAge, Integer maxAge, ContentStatus status) {
        validate(new WorldHostValidationInput(code, displayName, biome, minAge, maxAge, status));
    }

    private void validateAgeRange(Integer minAge, Integer maxAge) {
        requireNonNull(minAge, "minAge");
        requireNonNull(maxAge, "maxAge");
        if (minAge < 0) {
            throw new ValidationException("minAge must be greater than or equal to 0");
        }
        if (maxAge < minAge) {
            throw new ValidationException("maxAge must be greater than or equal to minAge");
        }
    }

    public record WorldHostValidationInput(String code, String displayName, Biome biome,
                                           Integer minAge, Integer maxAge, ContentStatus status) {}
}
