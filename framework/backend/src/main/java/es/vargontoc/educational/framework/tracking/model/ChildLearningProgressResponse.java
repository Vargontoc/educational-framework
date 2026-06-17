package es.vargontoc.educational.framework.tracking.model;

import java.util.List;

public class ChildLearningProgressResponse {

    private Long childProfileId;
    private Long learningPathId;
    private Long currentStepId;
    private List<CompletedStepInfo> completedSteps;

    public Long getChildProfileId() {
        return childProfileId;
    }

    public void setChildProfileId(Long childProfileId) {
        this.childProfileId = childProfileId;
    }

    public Long getLearningPathId() {
        return learningPathId;
    }

    public void setLearningPathId(Long learningPathId) {
        this.learningPathId = learningPathId;
    }

    public Long getCurrentStepId() {
        return currentStepId;
    }

    public void setCurrentStepId(Long currentStepId) {
        this.currentStepId = currentStepId;
    }

    public List<CompletedStepInfo> getCompletedSteps() {
        return completedSteps;
    }

    public void setCompletedSteps(List<CompletedStepInfo> completedSteps) {
        this.completedSteps = completedSteps;
    }
}
