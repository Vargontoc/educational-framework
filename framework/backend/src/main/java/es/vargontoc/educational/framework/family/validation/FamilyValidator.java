package es.vargontoc.educational.framework.family.validation;

import es.vargontoc.educational.framework.shared.exception.ValidationException;
import es.vargontoc.educational.framework.shared.validation.AbstractValidator;

public class FamilyValidator extends AbstractValidator<FamilyValidator.FamilyValidationInput> {

    private static final String PIN_REGEX = "^\\d{4}$";

    @Override
    public void validate(FamilyValidationInput target) {
        if (target.nameRequired()) {
            requireNonBlank(target.name(), "name");
        }
        if (target.name() != null) {
            requireMaxLength(target.name(), 100, "name");
        }
        validatePin(target.rawPin(), target.pinRequired());
    }

    public void validateForCreate(String name, String rawPin) {
        validate(new FamilyValidationInput(name, rawPin, true, true));
    }

    public void validateForUpdate(String name, String rawPin) {
        validate(new FamilyValidationInput(name, rawPin, false, false));
    }

    private void validatePin(String rawPin, boolean required) {
        if (!required && (rawPin == null || rawPin.isBlank())) {
            return;
        }
        requireNonBlank(rawPin, "rawPin");
        if (!rawPin.matches(PIN_REGEX)) {
            throw new ValidationException("rawPin must match ^\\d{4}$");
        }
    }

    public record FamilyValidationInput(String name, String rawPin, boolean pinRequired, boolean nameRequired) {}
}
