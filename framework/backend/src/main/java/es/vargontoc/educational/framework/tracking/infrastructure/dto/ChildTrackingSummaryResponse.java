package es.vargontoc.educational.framework.tracking.infrastructure.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ChildTrackingSummaryResponse {

    private Long childProfileId;
    private Integer totalActivities;
    private Integer totalTopics;
    private Integer totalAttempts;
    private Integer totalCorrect;
    private Integer totalIncorrect;
    private Integer totalTimeouts;
    private BigDecimal overallSuccessRate;
    private Integer totalAchievements;
    private Integer totalLearningPathsStarted;
    private Integer totalLearningStepsCompleted;
    private LocalDateTime lastActivityAt;

    public Long getChildProfileId() {
        return childProfileId;
    }

    public void setChildProfileId(Long childProfileId) {
        this.childProfileId = childProfileId;
    }

    public Integer getTotalActivities() {
        return totalActivities;
    }

    public void setTotalActivities(Integer totalActivities) {
        this.totalActivities = totalActivities;
    }

    public Integer getTotalTopics() {
        return totalTopics;
    }

    public void setTotalTopics(Integer totalTopics) {
        this.totalTopics = totalTopics;
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

    public BigDecimal getOverallSuccessRate() {
        return overallSuccessRate;
    }

    public void setOverallSuccessRate(BigDecimal overallSuccessRate) {
        this.overallSuccessRate = overallSuccessRate;
    }

    public Integer getTotalAchievements() {
        return totalAchievements;
    }

    public void setTotalAchievements(Integer totalAchievements) {
        this.totalAchievements = totalAchievements;
    }

    public Integer getTotalLearningPathsStarted() {
        return totalLearningPathsStarted;
    }

    public void setTotalLearningPathsStarted(Integer totalLearningPathsStarted) {
        this.totalLearningPathsStarted = totalLearningPathsStarted;
    }

    public Integer getTotalLearningStepsCompleted() {
        return totalLearningStepsCompleted;
    }

    public void setTotalLearningStepsCompleted(Integer totalLearningStepsCompleted) {
        this.totalLearningStepsCompleted = totalLearningStepsCompleted;
    }

    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(LocalDateTime lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }
}
