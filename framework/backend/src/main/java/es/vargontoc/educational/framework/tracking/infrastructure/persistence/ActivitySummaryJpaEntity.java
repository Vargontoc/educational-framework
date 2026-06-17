package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import es.vargontoc.educational.framework.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "activity_summary")
public class ActivitySummaryJpaEntity extends BaseEntity {

    @Column(name = "child_profile_id", nullable = false)
    private Long childProfileId;

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    @Column(name = "total_attempts", nullable = false)
    private Integer totalAttempts;

    @Column(name = "total_correct", nullable = false)
    private Integer totalCorrect;

    @Column(name = "total_incorrect", nullable = false)
    private Integer totalIncorrect;

    @Column(name = "total_timeouts", nullable = false)
    private Integer totalTimeouts;

    @Column(name = "success_rate_percent", precision = 5, scale = 2)
    private BigDecimal successRatePercent;

    @Column(name = "average_response_time_ms")
    private Integer averageResponseTimeMs;

    @Column(name = "current_difficulty_level_id")
    private Long currentDifficultyLevelId;

    @Column(name = "attempts_since_last_difficulty_change", nullable = false)
    private Integer attemptsSinceLastDifficultyChange;

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
}
