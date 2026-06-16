package es.vargontoc.educational.framework.tracking.ports.in;

import es.vargontoc.educational.framework.tracking.model.AttemptRegistrationResult;
import es.vargontoc.educational.framework.tracking.model.AttemptResult;

public interface RegisterActivityAttemptUseCase {

    AttemptRegistrationResult register(
            Long childProfileId,
            Long activityId,
            Long childSessionId,
            Long topicId,
            Long difficultyLevelId,
            AttemptResult result,
            Integer responseTimeMs,
            String attemptContext);
}
