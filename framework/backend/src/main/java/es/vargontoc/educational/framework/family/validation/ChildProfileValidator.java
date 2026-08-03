package es.vargontoc.educational.framework.family.validation;

import es.vargontoc.educational.framework.shared.exception.ValidationException;
import es.vargontoc.educational.framework.shared.validation.AbstractValidator;

import java.time.LocalDate;

import es.vargontoc.educational.framework.family.infrastructure.dto.UpdateChildProfileRequest;

public class ChildProfileValidator extends AbstractValidator<ChildProfileValidator.ChildProfileValidationInput> {

    @Override
    public void validate(ChildProfileValidationInput target) {
        requireNonBlank(target.name(), "name");
        requireMaxLength(target.name(), 100, "name");
        requireNonNull(target.birthday(), "birthday");
        validateBirthday(target.birthday());

        requireMaxLength(target.avatar(), 100, "avatar");
    }

    public void validateForUpdate(UpdateChildProfileRequest request) {
        if(request.name() != null) {
            requireNonBlank(request.name(), "name");
            requireMaxLength(request.name(), 100, "name");
        }

        if(request.birthday() != null) {
            validateBirthday(request.birthday());
        }

        if(request.avatar() != null) {
            requireMaxLength(request.avatar(), 100, "avatar");
        }
    }

    private void validateBirthday(LocalDate value){
        requireNonNull(value, "birthday");

        var now = LocalDate.now();
        var minDate = now.minusYears(5);
        if (value.isAfter(now)) {
            throw new ValidationException("birthday must not be in the future");
        }
        if (value.isBefore(minDate)) {
            throw new ValidationException("birthday must not be older than 5 years");
        }
    }

    public record ChildProfileValidationInput(String name, LocalDate birthday, String avatar) {}
}
