package es.vargontoc.educational.framework.tracking.model;

import java.time.LocalDateTime;

public class CompletedStepInfo {

    private Long stepId;
    private LocalDateTime completedAt;

    public CompletedStepInfo() {
    }

    public CompletedStepInfo(Long stepId, LocalDateTime completedAt) {
        this.stepId = stepId;
        this.completedAt = completedAt;
    }

    public Long getStepId() {
        return stepId;
    }

    public void setStepId(Long stepId) {
        this.stepId = stepId;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
