package es.vargontoc.educational.framework.tracking.model;

import java.time.LocalDateTime;

public class ChildLearningProgress {

    private Long id;
    private Long childProfileId;
    private Long learningPathId;
    private Long currentLearningPathStepId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ChildLearningProgress() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Long getCurrentLearningPathStepId() {
        return currentLearningPathStepId;
    }

    public void setCurrentLearningPathStepId(Long currentLearningPathStepId) {
        this.currentLearningPathStepId = currentLearningPathStepId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
