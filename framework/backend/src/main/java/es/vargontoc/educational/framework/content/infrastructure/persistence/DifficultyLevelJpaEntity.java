package es.vargontoc.educational.framework.content.infrastructure.persistence;

import es.vargontoc.educational.framework.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "difficulty_level")
public class DifficultyLevelJpaEntity extends BaseEntity {

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    @Column(name = "difficulty_code", nullable = false, length = 20)
    private String difficultyCode;

    @Column(name = "engine_params", columnDefinition = "JSONB")
    private String engineParams;

    @Column(name = "adaptive_threshold_config", columnDefinition = "JSONB")
    private String adaptiveThresholdConfig;

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public String getDifficultyCode() {
        return difficultyCode;
    }

    public void setDifficultyCode(String difficultyCode) {
        this.difficultyCode = difficultyCode;
    }

    public String getEngineParams() {
        return engineParams;
    }

    public void setEngineParams(String engineParams) {
        this.engineParams = engineParams;
    }

    public String getAdaptiveThresholdConfig() {
        return adaptiveThresholdConfig;
    }

    public void setAdaptiveThresholdConfig(String adaptiveThresholdConfig) {
        this.adaptiveThresholdConfig = adaptiveThresholdConfig;
    }
}
