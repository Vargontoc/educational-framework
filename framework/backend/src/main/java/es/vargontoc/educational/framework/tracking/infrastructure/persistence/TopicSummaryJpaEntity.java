package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import es.vargontoc.educational.framework.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "topic_summary")
public class TopicSummaryJpaEntity extends BaseEntity {

    @Column(name = "child_profile_id", nullable = false)
    private Long childProfileId;

    @Column(name = "topic_id", nullable = false)
    private Long topicId;

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

    @Column(name = "failure_rate_percent", precision = 5, scale = 2)
    private BigDecimal failureRatePercent;

    @Column(name = "average_response_time_ms")
    private Integer averageResponseTimeMs;

    @Column(name = "performance_band", length = 20)
    private String performanceBand;

    public Long getChildProfileId() {
        return childProfileId;
    }

    public void setChildProfileId(Long childProfileId) {
        this.childProfileId = childProfileId;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
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

    public BigDecimal getFailureRatePercent() {
        return failureRatePercent;
    }

    public void setFailureRatePercent(BigDecimal failureRatePercent) {
        this.failureRatePercent = failureRatePercent;
    }

    public Integer getAverageResponseTimeMs() {
        return averageResponseTimeMs;
    }

    public void setAverageResponseTimeMs(Integer averageResponseTimeMs) {
        this.averageResponseTimeMs = averageResponseTimeMs;
    }

    public String getPerformanceBand() {
        return performanceBand;
    }

    public void setPerformanceBand(String performanceBand) {
        this.performanceBand = performanceBand;
    }
}
