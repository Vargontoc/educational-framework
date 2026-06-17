package es.vargontoc.educational.framework.tracking.ports.in;

public interface UpdateChildLearningProgressUseCase {

    void updateCurrentStep(Long childProfileId, Long learningPathId, Long currentStepId);
}
