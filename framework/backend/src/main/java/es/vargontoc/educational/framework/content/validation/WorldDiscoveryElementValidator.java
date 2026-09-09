package es.vargontoc.educational.framework.content.validation;

import es.vargontoc.educational.framework.content.model.Biome;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.content.model.ElementType;
import es.vargontoc.educational.framework.content.model.InteractionCueType;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import es.vargontoc.educational.framework.shared.validation.AbstractValidator;

public class WorldDiscoveryElementValidator extends AbstractValidator<WorldDiscoveryElementValidator.WorldDiscoveryElementValidationInput> {

    @Override
    public void validate(WorldDiscoveryElementValidationInput target) {
        requireNonBlank(target.code(), "code");
        requireNonBlank(target.displayName(), "displayName");
        requireNonNull(target.elementType(), "elementType");
        requireNonNull(target.biome(), "biome");
        requireNonNull(target.status(), "status");
        validateAgeRange(target.minAge(), target.maxAge());
        validateActivityIdConstraint(target.elementType(), target.activityId());
        validateNormalizedPosition(target.positionX(), "positionX");
        validateNormalizedPosition(target.positionY(), "positionY");
    }

    public void validateForCreate(String code, String displayName, ElementType elementType,
                                  Biome biome, Integer minAge, Integer maxAge,
                                  ContentStatus status, Long activityId, Long topicId,
                                  InteractionCueType interactionCueType, Double positionX, Double positionY) {
        validate(new WorldDiscoveryElementValidationInput(code, displayName, elementType, biome,
                minAge, maxAge, status, activityId, topicId, interactionCueType, positionX, positionY));
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

    private void validateActivityIdConstraint(ElementType elementType, Long activityId) {
        if (elementType != ElementType.DISCOVERY && activityId != null) {
            throw new ValidationException("activityId is only allowed when elementType is DISCOVERY");
        }
    }

    private void validateNormalizedPosition(Double value, String fieldName) {
        if (value == null) {
            return;
        }
        if (value < 0.0 || value > 1.0) {
            throw new ValidationException(fieldName + " must be between 0.0 and 1.0");
        }
    }

    public record WorldDiscoveryElementValidationInput(String code, String displayName, ElementType elementType,
                                                        Biome biome, Integer minAge, Integer maxAge,
                                                        ContentStatus status, Long activityId, Long topicId,
                                                        InteractionCueType interactionCueType,
                                                        Double positionX, Double positionY) {}
}
