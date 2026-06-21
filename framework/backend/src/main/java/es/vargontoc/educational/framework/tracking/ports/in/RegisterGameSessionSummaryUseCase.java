package es.vargontoc.educational.framework.tracking.ports.in;

import es.vargontoc.educational.framework.tracking.model.GameSessionFinalStatus;
import es.vargontoc.educational.framework.tracking.model.GameSessionSummaryResult;

import java.time.LocalDateTime;

public interface RegisterGameSessionSummaryUseCase {

    GameSessionSummaryResult registerGameSessionSummary(
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
            GameSessionFinalStatus finalStatus);
}