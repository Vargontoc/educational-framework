package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import es.vargontoc.educational.framework.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "child_learning_completed_step")
public class ChildLearningCompletedStepJpaEntity extends BaseEntity {

    @Column(name = "child_profile_id", nullable = false)
    private Long childProfileId;

    @Column(name = "learning_path_id", nullable = false)
    private Long learningPathId;

    @Column(name = "learning_path_step_id", nullable = false)
    private Long learningPathStepId;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

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
}
