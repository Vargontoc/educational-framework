package es.vargontoc.educational.framework.tracking.validation;

import es.vargontoc.educational.framework.tracking.model.GameSessionFinalStatus;
import es.vargontoc.educational.framework.shared.exception.ValidationException;

import java.time.LocalDateTime;

public class GameSessionSummaryValidator {

    public void validate(
            Long childProfileId,
            Long childSessionId,
            Long activityId,
            Long difficultyLevelStartId,
            Long difficultyLevelEndId,
            Integer score,
            Integer totalAttempts,
            Integer totalCorrect,
            Integer totalTimeouts,
            LocalDateTime startedAt,
            LocalDateTime endedAt,
            GameSessionFinalStatus finalStatus) {

        if (childProfileId == null) {
            throw new ValidationException("childProfileId is required");
        }
        if (childSessionId == null) {
            throw new ValidationException("childSessionId is required");
        }
        if (activityId == null) {
            throw new ValidationException("activityId is required");
        }
        if (difficultyLevelStartId == null) {
            throw new ValidationException("difficultyLevelStartId is required");
        }
        if (difficultyLevelEndId == null) {
            throw new ValidationException("difficultyLevelEndId is required");
        }
        if (score == null) {
            throw new ValidationException("score is required");
        }
        if (totalAttempts == null) {
            throw new ValidationException("totalAttempts is required");
        }
        if (totalCorrect == null) {
            throw new ValidationException("totalCorrect is required");
        }
        if (totalTimeouts == null) {
            throw new ValidationException("totalTimeouts is required");
        }
        if (startedAt == null) {
            throw new ValidationException("startedAt is required");
        }
        if (endedAt == null) {
            throw new ValidationException("endedAt is required");
        }
        if (finalStatus == null) {
            throw new ValidationException("finalStatus is required");
        }
        if (finalStatus != GameSessionFinalStatus.COMPLETED && finalStatus != GameSessionFinalStatus.ABANDONED) {
            throw new ValidationException("finalStatus must be COMPLETED or ABANDONED");
        }
        if (endedAt.isBefore(startedAt)) {
            throw new ValidationException("endedAt cannot be before startedAt");
        }
    }
}