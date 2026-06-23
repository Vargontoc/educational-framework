package es.vargontoc.educational.framework.tracking.ports.in;

import es.vargontoc.educational.framework.tracking.model.ChildLearningProgressResponse;

public interface RegisterLearningPathStepProgressUseCase {

    ChildLearningProgressResponse registerLearningPathStepProgress(
            Long childProfileId,
            Long learningPathId,
            Long learningPathStepId);
}
