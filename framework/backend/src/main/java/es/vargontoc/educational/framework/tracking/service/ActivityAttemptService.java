package es.vargontoc.educational.framework.tracking.service;

import es.vargontoc.educational.framework.tracking.model.ActivityAttempt;
import es.vargontoc.educational.framework.tracking.model.AttemptRegistrationResult;
import es.vargontoc.educational.framework.tracking.model.AttemptResult;
import es.vargontoc.educational.framework.tracking.ports.in.RegisterActivityAttemptUseCase;
import es.vargontoc.educational.framework.tracking.ports.out.ActivityAttemptRepository;
import es.vargontoc.educational.framework.tracking.validation.ActivityAttemptValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ActivityAttemptService implements RegisterActivityAttemptUseCase {

    private final ActivityAttemptRepository repository;
    private final ActivityAttemptValidator validator;

    public ActivityAttemptService(ActivityAttemptRepository repository) {
        this.repository = repository;
        this.validator = new ActivityAttemptValidator();
    }

    @Override
    public AttemptRegistrationResult register(
            Long childProfileId,
            Long activityId,
            Long childSessionId,
            Long topicId,
            Long difficultyLevelId,
            AttemptResult result,
            Integer responseTimeMs,
            String attemptContext) {

        validator.validate(childProfileId, activityId, childSessionId, topicId, difficultyLevelId, result);

        var attempt = new ActivityAttempt();
        attempt.setChildProfileId(childProfileId);
        attempt.setActivityId(activityId);
        attempt.setChildSessionId(childSessionId);
        attempt.setTopicId(topicId);
        attempt.setDifficultyLevelId(difficultyLevelId);
        attempt.setResult(result);
        attempt.setResponseTimeMs(responseTimeMs);
        attempt.setAttemptContext(attemptContext);

        var saved = repository.save(attempt);

        return new AttemptRegistrationResult(saved.getId(), saved.getCreatedAt());
    }
}
