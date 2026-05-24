package es.vargontoc.educational.framework.content.validation;

import es.vargontoc.educational.framework.content.model.EntityType;
import es.vargontoc.educational.framework.shared.validation.AbstractValidator;

public class ContentLocaleValidator extends AbstractValidator<ContentLocaleValidator.ContentLocaleValidationInput> {

    private static final String SUPPORTED_LOCALE = "es-ES";

    @Override
    public void validate(ContentLocaleValidationInput target) {
        requireNonNull(target.entityType(), "entityType");
        requireNonNull(target.entityId(), "entityId");
        requireNonBlank(target.localeCode(), "localeCode");
        requireNonBlank(target.name(), "name");
        if (!SUPPORTED_LOCALE.equals(target.localeCode().trim())) {
            throw new es.vargontoc.educational.framework.shared.exception.ValidationException(
                "Unsupported locale '" + target.localeCode() + "'. Only " + SUPPORTED_LOCALE + " is supported in v1");
        }
    }

    public void validateForCreate(EntityType entityType, Long entityId, String localeCode, String name) {
        validate(new ContentLocaleValidationInput(entityType, entityId, localeCode, name));
    }

    public void validateForUpdate(EntityType entityType, Long entityId, String localeCode, String name) {
        validate(new ContentLocaleValidationInput(entityType, entityId, localeCode, name));
    }

    public record ContentLocaleValidationInput(EntityType entityType, Long entityId, String localeCode, String name) {}
}
