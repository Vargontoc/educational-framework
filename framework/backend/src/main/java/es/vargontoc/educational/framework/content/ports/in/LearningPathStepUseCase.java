package es.vargontoc.educational.framework.content.ports.in;

import es.vargontoc.educational.framework.content.model.LearningPathStep;

import java.util.List;

public interface LearningPathStepUseCase {

    LearningPathStep createStep(Long learningPathId, Long activityId, Integer stepOrder, String unlockCondition, String visualMetadata);

    LearningPathStep getStep(Long id);

    List<LearningPathStep> listStepsByLearningPath(Long learningPathId);

    LearningPathStep updateStep(Long id, Long learningPathId, Long activityId, Integer stepOrder, String unlockCondition, String visualMetadata);
}
