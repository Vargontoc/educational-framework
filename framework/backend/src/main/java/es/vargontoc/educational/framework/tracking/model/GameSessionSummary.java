package es.vargontoc.educational.framework.tracking.model;

import java.time.LocalDateTime;

public class GameSessionSummary {

    private Long id;
    private Long childProfileId;
    private Long childSessionId;
    private Long activityId;
    private Long difficultyLevelStartId;
    private Long difficultyLevelEndId;
    private Integer score;
    private Integer totalAttempts;
    private Integer totalCorrect;
    private Integer totalTimeouts;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private GameSessionFinalStatus finalStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public GameSessionSummary() {
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

    public Long getChildSessionId() {
        return childSessionId;
    }

    public void setChildSessionId(Long childSessionId) {
        this.childSessionId = childSessionId;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public Long getDifficultyLevelStartId() {
        return difficultyLevelStartId;
    }

    public void setDifficultyLevelStartId(Long difficultyLevelStartId) {
        this.difficultyLevelStartId = difficultyLevelStartId;
    }

    public Long getDifficultyLevelEndId() {
        return difficultyLevelEndId;
    }

    public void setDifficultyLevelEndId(Long difficultyLevelEndId) {
        this.difficultyLevelEndId = difficultyLevelEndId;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
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

    public Integer getTotalTimeouts() {
        return totalTimeouts;
    }

    public void setTotalTimeouts(Integer totalTimeouts) {
        this.totalTimeouts = totalTimeouts;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public GameSessionFinalStatus getFinalStatus() {
        return finalStatus;
    }

    public void setFinalStatus(GameSessionFinalStatus finalStatus) {
        this.finalStatus = finalStatus;
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