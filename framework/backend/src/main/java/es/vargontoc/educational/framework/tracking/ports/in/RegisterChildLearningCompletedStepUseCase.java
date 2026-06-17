package es.vargontoc.educational.framework.tracking.ports.in;

public interface RegisterChildLearningCompletedStepUseCase {

    void registerCompletedStep(Long childProfileId, Long learningPathId, Long stepId);
}
