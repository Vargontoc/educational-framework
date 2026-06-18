package es.vargontoc.educational.framework.tracking.ports.out;

import es.vargontoc.educational.framework.tracking.model.ChildLearningProgress;

import java.util.List;
import java.util.Optional;

public interface ChildLearningProgressRepository {

    Optional<ChildLearningProgress> findByChildProfileIdAndLearningPathId(Long childProfileId, Long learningPathId);

    List<ChildLearningProgress> findByChildProfileId(Long childProfileId);

    ChildLearningProgress save(ChildLearningProgress progress);
}
