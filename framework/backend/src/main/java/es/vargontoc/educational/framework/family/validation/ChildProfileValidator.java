package es.vargontoc.educational.framework.family.validation;

import es.vargontoc.educational.framework.shared.exception.ValidationException;
import es.vargontoc.educational.framework.shared.validation.AbstractValidator;

import java.time.LocalDate;

public class ChildProfileValidator extends AbstractValidator<ChildProfileValidator.ChildProfileValidationInput> {

    @Override
    public void validate(ChildProfileValidationInput target) {
        requireNonBlank(target.name(), "name");
        requireMaxLength(target.name(), 100, "name");
        requireNonNull(target.birthday(), "birthday");

        var now = LocalDate.now();
        var minDate = now.minusYears(18);
        if (target.birthday().isAfter(now)) {
            throw new ValidationException("birthday must not be in the future");
        }
        if (target.birthday().isBefore(minDate)) {
            throw new ValidationException("birthday must not be older than 18 years");
        }

        requireMaxLength(target.avatar(), 100, "avatar");
    }

    public record ChildProfileValidationInput(String name, LocalDate birthday, String avatar) {}
}
