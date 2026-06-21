package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import es.vargontoc.educational.framework.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_session_summary")
public class GameSessionSummaryJpaEntity extends BaseEntity {

    @Column(name = "child_profile_id", nullable = false)
    private Long childProfileId;

    @Column(name = "child_session_id", nullable = false)
    private Long childSessionId;

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    @Column(name = "difficulty_level_start_id", nullable = false)
    private Long difficultyLevelStartId;

    @Column(name = "difficulty_level_end_id", nullable = false)
    private Long difficultyLevelEndId;

    @Column(nullable = false)
    private Integer score;

    @Column(name = "total_attempts", nullable = false)
    private Integer totalAttempts;

    @Column(name = "total_correct", nullable = false)
    private Integer totalCorrect;

    @Column(name = "total_timeouts", nullable = false)
    private Integer totalTimeouts;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at", nullable = false)
    private LocalDateTime endedAt;

    @Column(name = "final_status", nullable = false, length = 20)
    private String finalStatus;

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

    public String getFinalStatus() {
        return finalStatus;
    }

    public void setFinalStatus(String finalStatus) {
        this.finalStatus = finalStatus;
    }
}