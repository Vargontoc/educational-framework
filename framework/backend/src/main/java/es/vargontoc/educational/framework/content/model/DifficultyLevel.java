package es.vargontoc.educational.framework.content.model;

import java.time.LocalDateTime;

public class DifficultyLevel {

    private Long id;
    private Long activityId;
    private DifficultyCode difficultyCode;
    private String engineParams;
    private String adaptiveThresholdConfig;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public DifficultyCode getDifficultyCode() {
        return difficultyCode;
    }

    public void setDifficultyCode(DifficultyCode difficultyCode) {
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
