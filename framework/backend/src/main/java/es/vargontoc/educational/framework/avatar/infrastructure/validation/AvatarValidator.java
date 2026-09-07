package es.vargontoc.educational.framework.avatar.infrastructure.validation;

import es.vargontoc.educational.framework.avatar.domain.AvatarEventRequest;
import es.vargontoc.educational.framework.shared.validation.AbstractValidator;

public class AvatarValidator extends AbstractValidator<AvatarValidator.AvatarValidationInput> {

    @Override
    public void validate(AvatarValidationInput target) {
        requireNonNull(target.childSessionId(), "childSessionId");
        requireNonNull(target.eventType(), "eventType");
    }

    public void validateForProcess(AvatarEventRequest request) {
        validate(new AvatarValidationInput(
            request.childSessionId(),
            request.eventType()
        ));
    }

    public record AvatarValidationInput(Long childSessionId, Object eventType) {}
}