package es.vargontoc.educational.framework.content.validation;


import es.vargontoc.educational.framework.audio.domain.enums.TonePreset;
import es.vargontoc.educational.framework.avatar.domain.enums.AvatarEventType;
import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.shared.validation.AbstractValidator;

public class AvatarEventCatalogValidator extends AbstractValidator<AvatarEventCatalogValidator.AvatarEventCatalogValidationInput> {

    @Override
    public void validate(AvatarEventCatalogValidationInput target) {
        requireNonNull(target.eventType(), "eventType");
        requireNonNull(target.tone(), "tone");
        requireNonBlank(target.messageText(), "messageText");
        requireMaxLength(target.messageText(), 300, "messageText");
        requireNonBlank(target.locale(), "locale");
        requireNonNull(target.status(), "status");
    }

    public void validateForCreate(AvatarEventType eventType, TonePreset tone, String messageText, String locale, ContentStatus status) {
        validate(new AvatarEventCatalogValidationInput(eventType, tone, messageText, locale, status));
    }

    public void validateForUpdate(AvatarEventType eventType, TonePreset tone, String messageText, String locale, ContentStatus status) {
        validate(new AvatarEventCatalogValidationInput(eventType, tone, messageText, locale, status));
    }

    public record AvatarEventCatalogValidationInput(AvatarEventType eventType, TonePreset tone, String messageText, String locale, ContentStatus status) {}
}
