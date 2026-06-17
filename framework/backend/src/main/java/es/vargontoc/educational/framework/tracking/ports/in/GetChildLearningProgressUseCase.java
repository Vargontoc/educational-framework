package es.vargontoc.educational.framework.tracking.ports.in;

import es.vargontoc.educational.framework.tracking.model.ChildLearningProgressResponse;

public interface GetChildLearningProgressUseCase {

    ChildLearningProgressResponse getChildLearningProgress(Long childProfileId, Long learningPathId);
}
