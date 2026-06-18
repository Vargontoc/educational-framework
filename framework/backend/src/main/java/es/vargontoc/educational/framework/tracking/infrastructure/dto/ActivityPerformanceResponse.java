package es.vargontoc.educational.framework.tracking.infrastructure.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ActivityPerformanceResponse {

    private Long activityId;
    private Integer totalAttempts;
    private Integer totalCorrect;
    private Integer totalIncorrect;
    private Integer totalTimeouts;
    private BigDecimal successRatePercent;
    private Integer averageResponseTimeMs;
    private Long currentDifficultyLevelId;
    private Integer attemptsSinceLastDifficultyChange;
    private LocalDateTime lastAttemptAt;

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public Integer getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(Integer totalAttempts) {
        this.totalAttempts = totalAttempts;
    }

    public Integer getTotalCorrect() {
        return totalCorrect;
    }

    public void setTotalCorrect(Integer totalCorrect) {
        this.totalCorrect = totalCorrect;
    }

    public Integer getTotalIncorrect() {
        return totalIncorrect;
    }

    public void setTotalIncorrect(Integer totalIncorrect) {
        this.totalIncorrect = totalIncorrect;
    }

    public Integer getTotalTimeouts() {
        return totalTimeouts;
    }

    public void setTotalTimeouts(Integer totalTimeouts) {
        this.totalTimeouts = totalTimeouts;
    }

    public BigDecimal getSuccessRatePercent() {
        return successRatePercent;
    }

    public void setSuccessRatePercent(BigDecimal successRatePercent) {
        this.successRatePercent = successRatePercent;
    }

    public Integer getAverageResponseTimeMs() {
        return averageResponseTimeMs;
    }

    public void setAverageResponseTimeMs(Integer averageResponseTimeMs) {
        this.averageResponseTimeMs = averageResponseTimeMs;
    }

    public Long getCurrentDifficultyLevelId() {
        return currentDifficultyLevelId;
    }

    public void setCurrentDifficultyLevelId(Long currentDifficultyLevelId) {
        this.currentDifficultyLevelId = currentDifficultyLevelId;
    }

    public Integer getAttemptsSinceLastDifficultyChange() {
        return attemptsSinceLastDifficultyChange;
    }

    public void setAttemptsSinceLastDifficultyChange(Integer attemptsSinceLastDifficultyChange) {
        this.attemptsSinceLastDifficultyChange = attemptsSinceLastDifficultyChange;
    }

    public LocalDateTime getLastAttemptAt() {
        return lastAttemptAt;
    }

    public void setLastAttemptAt(LocalDateTime lastAttemptAt) {
        this.lastAttemptAt = lastAttemptAt;
    }
}
