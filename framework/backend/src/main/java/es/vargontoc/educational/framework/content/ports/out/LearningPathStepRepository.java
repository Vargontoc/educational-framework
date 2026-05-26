package es.vargontoc.educational.framework.content.ports.out;

import es.vargontoc.educational.framework.content.model.LearningPathStep;

import java.util.List;
import java.util.Optional;

public interface LearningPathStepRepository {

    Optional<LearningPathStep> findById(Long id);

    List<LearningPathStep> findByLearningPathId(Long learningPathId);

    boolean existsByLearningPathIdAndStepOrder(Long learningPathId, Integer stepOrder);

    LearningPathStep save(LearningPathStep step);
}
