package es.vargontoc.educational.framework.tracking.service;

import es.vargontoc.educational.framework.tracking.model.GameSessionFinalStatus;
import es.vargontoc.educational.framework.tracking.model.GameSessionSummary;
import es.vargontoc.educational.framework.tracking.model.GameSessionSummaryResult;
import es.vargontoc.educational.framework.tracking.ports.in.RegisterGameSessionSummaryUseCase;
import es.vargontoc.educational.framework.tracking.ports.out.GameSessionSummaryRepository;
import es.vargontoc.educational.framework.tracking.validation.GameSessionSummaryValidator;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Transactional
public class GameSessionSummaryService implements RegisterGameSessionSummaryUseCase {

    private final GameSessionSummaryRepository repository;
    private final GameSessionSummaryValidator validator;

    public GameSessionSummaryService(GameSessionSummaryRepository repository) {
        this.repository = repository;
        this.validator = new GameSessionSummaryValidator();
    }

    @Override
    public GameSessionSummaryResult registerGameSessionSummary(
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

        validator.validate(
                childProfileId,
                childSessionId,
                activityId,
                difficultyLevelStartId,
                difficultyLevelEndId,
                score,
                totalAttempts,
                totalCorrect,
                totalTimeouts,
                startedAt,
                endedAt,
                finalStatus);

        var summary = new GameSessionSummary();
        summary.setChildProfileId(childProfileId);
        summary.setChildSessionId(childSessionId);
        summary.setActivityId(activityId);
        summary.setDifficultyLevelStartId(difficultyLevelStartId);
        summary.setDifficultyLevelEndId(difficultyLevelEndId);
        summary.setScore(score);
        summary.setTotalAttempts(totalAttempts);
        summary.setTotalCorrect(totalCorrect);
        summary.setTotalTimeouts(totalTimeouts);
        summary.setStartedAt(startedAt);
        summary.setEndedAt(endedAt);
        summary.setFinalStatus(finalStatus);

        var saved = repository.save(summary);

        return new GameSessionSummaryResult(saved.getId(), saved.getCreatedAt());
    }
}