package es.vargontoc.educational.framework.tracking.infrastructure.dto;

import es.vargontoc.educational.framework.tracking.model.TopicPerformanceBand;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TopicPerformanceResponse {

    private Long topicId;
    private Integer totalAttempts;
    private Integer totalCorrect;
    private Integer totalIncorrect;
    private Integer totalTimeouts;
    private BigDecimal successRatePercent;
    private BigDecimal failureRatePercent;
    private Integer averageResponseTimeMs;
    private TopicPerformanceBand performanceBand;
    private LocalDateTime lastAttemptAt;

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

    public TopicPerformanceBand getPerformanceBand() {
        return performanceBand;
    }

    public void setPerformanceBand(TopicPerformanceBand performanceBand) {
        this.performanceBand = performanceBand;
    }

    public LocalDateTime getLastAttemptAt() {
        return lastAttemptAt;
    }

    public void setLastAttemptAt(LocalDateTime lastAttemptAt) {
        this.lastAttemptAt = lastAttemptAt;
    }
}
