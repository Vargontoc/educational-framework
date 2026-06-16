package es.vargontoc.educational.framework.tracking.validation;

import es.vargontoc.educational.framework.tracking.model.AttemptResult;
import es.vargontoc.educational.framework.shared.exception.ValidationException;

public class ActivityAttemptValidator {

    public void validate(
            Long childProfileId,
            Long activityId,
            Long childSessionId,
            Long topicId,
            Long difficultyLevelId,
            AttemptResult result) {

        if (childProfileId == null) {
            throw new ValidationException("childProfileId is required");
        }
        if (activityId == null) {
            throw new ValidationException("activityId is required");
        }
        if (childSessionId == null) {
            throw new ValidationException("childSessionId is required");
        }
        if (topicId == null) {
            throw new ValidationException("topicId is required");
        }
        if (difficultyLevelId == null) {
            throw new ValidationException("difficultyLevelId is required");
        }
        if (result == null) {
            throw new ValidationException("result is required");
        }
    }
}
