package es.vargontoc.educational.framework.avatar.validation;

import es.vargontoc.educational.framework.avatar.infrastructure.dto.NarrateStorytellerRequest;
import es.vargontoc.educational.framework.shared.validation.AbstractValidator;

public class NarrateStorytellerValidator extends AbstractValidator<NarrateStorytellerRequest> {

    @Override
    public void validate(NarrateStorytellerRequest request) {
        requireNonBlank(request.text(), "text");
    }

    public void validateForNarrate(NarrateStorytellerRequest request) {
        validate(request);
    }
}