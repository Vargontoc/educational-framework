package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import es.vargontoc.educational.framework.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "child_learning_progress")
public class ChildLearningProgressJpaEntity extends BaseEntity {

    @Column(name = "child_profile_id", nullable = false)
    private Long childProfileId;

    @Column(name = "learning_path_id", nullable = false)
    private Long learningPathId;

    @Column(name = "current_learning_path_step_id")
    private Long currentLearningPathStepId;

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
}
