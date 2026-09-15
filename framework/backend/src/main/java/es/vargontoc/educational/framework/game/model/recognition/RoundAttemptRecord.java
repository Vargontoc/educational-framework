package es.vargontoc.educational.framework.game.model.recognition;

import es.vargontoc.educational.framework.tracking.model.AttemptResult;

public record RoundAttemptRecord(
        Long topicId,
        Long elementId,
        Long difficultyLevelId,
        AttemptResult result,
        Integer responseTimeMs,
        String attemptContext) {
}
