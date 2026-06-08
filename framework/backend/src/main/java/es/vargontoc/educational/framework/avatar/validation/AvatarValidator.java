package es.vargontoc.educational.framework.avatar.validation;

import es.vargontoc.educational.framework.avatar.infrastructure.dto.AvatarEventRequest;
import es.vargontoc.educational.framework.shared.validation.AbstractValidator;

public class AvatarValidator extends AbstractValidator<AvatarValidator.AvatarValidationInput> {

    @Override
    public void validate(AvatarValidationInput target) {
        requireNonNull(target.childSessionId(), "childSessionId");
        requireNonNull(target.eventType(), "eventType");
        requireNonBlank(target.locale(), "locale");
    }

    public void validateForProcess(AvatarEventRequest request) {
        validate(new AvatarValidationInput(
            request.childSessionId(),
            request.eventType(),
            request.locale()
        ));
    }

    public record AvatarValidationInput(Long childSessionId, Object eventType, String locale) {}
}