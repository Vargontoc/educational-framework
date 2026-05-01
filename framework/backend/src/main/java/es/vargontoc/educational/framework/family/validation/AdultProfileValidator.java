package es.vargontoc.educational.framework.family.validation;

import es.vargontoc.educational.framework.shared.exception.ValidationException;
import es.vargontoc.educational.framework.shared.validation.AbstractValidator;

import java.time.LocalDate;

public class AdultProfileValidator extends AbstractValidator<AdultProfileValidator.AdultProfileValidationInput> {

    @Override
    public void validate(AdultProfileValidationInput target) {
        requireNonBlank(target.name(), "name");
        requireMaxLength(target.name(), 100, "name");
        requireNonNull(target.birthday(), "birthday");
        if (target.birthday().isAfter(LocalDate.now())) {
            throw new ValidationException("birthday must not be in the future");
        }
        requireMaxLength(target.avatar(), 100, "avatar");
    }

    public record AdultProfileValidationInput(String name, LocalDate birthday, String avatar) {}
}
