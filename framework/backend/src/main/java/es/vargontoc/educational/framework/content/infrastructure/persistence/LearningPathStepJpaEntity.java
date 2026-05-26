package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "learning_path_step", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"learning_path_id", "step_order"})
})
public class LearningPathStepJpaEntity extends BaseEntity {

    @Column(name = "learning_path_id", nullable = false)
    private Long learningPathId;

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(name = "unlock_condition", length = 100)
    private String unlockCondition;

    @Column(name = "visual_metadata", columnDefinition = "TEXT")
    private String visualMetadata;

    @Column(nullable = false, length = 20)
    private String status;

    public Long getLearningPathId() {
        return learningPathId;
    }

    public void setLearningPathId(Long learningPathId) {
        this.learningPathId = learningPathId;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public Integer getStepOrder() {
        return stepOrder;
    }

    public void setStepOrder(Integer stepOrder) {
        this.stepOrder = stepOrder;
    }

    public String getUnlockCondition() {
        return unlockCondition;
    }

    public void setUnlockCondition(String unlockCondition) {
        this.unlockCondition = unlockCondition;
    }

    public String getVisualMetadata() {
        return visualMetadata;
    }

    public void setVisualMetadata(String visualMetadata) {
        this.visualMetadata = visualMetadata;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
