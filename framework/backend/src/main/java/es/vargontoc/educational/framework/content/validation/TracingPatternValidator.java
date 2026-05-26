package es.vargontoc.educational.framework.content.validation;

import es.vargontoc.educational.framework.content.model.ContentStatus;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import es.vargontoc.educational.framework.shared.validation.AbstractValidator;

import java.util.List;

public class TracingPatternValidator extends AbstractValidator<TracingPatternValidator.TracingPatternValidationInput> {

    @Override
    public void validate(TracingPatternValidationInput target) {
        requireNonNull(target.topicId(), "topicId");
        requireNonBlank(target.name(), "name");
        requireMaxLength(target.name(), 200, "name");
        requireNonNull(target.status(), "status");
        validatePoints(target.points());
    }

    public void validateForCreate(Long topicId, String name, List<List<Double>> points, ContentStatus status) {
        validate(new TracingPatternValidationInput(topicId, name, points, status));
    }

    public void validateForUpdate(Long topicId, String name, List<List<Double>> points, ContentStatus status) {
        validate(new TracingPatternValidationInput(topicId, name, points, status));
    }

    private void validatePoints(List<List<Double>> points) {
        if (points == null || points.isEmpty()) {
            throw new ValidationException("points must not be empty");
        }
        for (int i = 0; i < points.size(); i++) {
            List<Double> point = points.get(i);
            if (point == null || point.size() != 2) {
                throw new ValidationException("Each point must have exactly 2 coordinates [x, y]");
            }
            double x = point.get(0);
            double y = point.get(1);
            if (x < 0.0 || x > 1.0) {
                throw new ValidationException("Point x coordinate must be normalized between 0.0 and 1.0");
            }
            if (y < 0.0 || y > 1.0) {
                throw new ValidationException("Point y coordinate must be normalized between 0.0 and 1.0");
            }
        }
    }

    public record TracingPatternValidationInput(Long topicId, String name, List<List<Double>> points, ContentStatus status) {}
}
