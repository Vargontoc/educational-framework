package es.vargontoc.educational.framework.tracking.infrastructure.dto;

import java.util.List;

public class DifficultyEvolutionResponse {

    private Long childProfileId;
    private Long activityId;
    private Long currentDifficultyLevelId;
    private Integer attemptsSinceLastChange;
    private List<DifficultyChangeRecord> history;

    public Long getChildProfileId() {
        return childProfileId;
    }

    public void setChildProfileId(Long childProfileId) {
        this.childProfileId = childProfileId;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public Long getCurrentDifficultyLevelId() {
        return currentDifficultyLevelId;
    }

    public void setCurrentDifficultyLevelId(Long currentDifficultyLevelId) {
        this.currentDifficultyLevelId = currentDifficultyLevelId;
    }

    public Integer getAttemptsSinceLastChange() {
        return attemptsSinceLastChange;
    }

    public void setAttemptsSinceLastChange(Integer attemptsSinceLastChange) {
        this.attemptsSinceLastChange = attemptsSinceLastChange;
    }

    public List<DifficultyChangeRecord> getHistory() {
        return history;
    }

    public void setHistory(List<DifficultyChangeRecord> history) {
        this.history = history;
    }
}
