package es.vargontoc.educational.framework.tracking.model;

import java.time.LocalDateTime;

public class ChildLearningCompletedStep {

    private Long id;
    private Long childProfileId;
    private Long learningPathId;
    private Long learningPathStepId;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ChildLearningCompletedStep() {
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

    public Long getLearningPathStepId() {
        return learningPathStepId;
    }

    public void setLearningPathStepId(Long learningPathStepId) {
        this.learningPathStepId = learningPathStepId;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
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
