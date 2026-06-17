package es.vargontoc.educational.framework.tracking.ports.out;

import es.vargontoc.educational.framework.tracking.model.ChildLearningCompletedStep;

import java.util.List;
import java.util.Optional;

public interface ChildLearningCompletedStepRepository {

    List<ChildLearningCompletedStep> findByChildProfileIdAndLearningPathId(Long childProfileId, Long learningPathId);

    Optional<ChildLearningCompletedStep> findByChildProfileIdAndLearningPathIdAndStepId(
            Long childProfileId, Long learningPathId, Long stepId);

    ChildLearningCompletedStep save(ChildLearningCompletedStep step);
}
