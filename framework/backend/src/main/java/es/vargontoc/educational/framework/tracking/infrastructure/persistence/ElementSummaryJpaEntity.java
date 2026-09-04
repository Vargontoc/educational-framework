package es.vargontoc.educational.framework.tracking.infrastructure.persistence;

import es.vargontoc.educational.framework.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "element_summary")
public class ElementSummaryJpaEntity extends BaseEntity {

    @Column(name = "child_profile_id", nullable = false)
    private Long childProfileId;

    @Column(name = "element_id", nullable = false)
    private Long elementId;

    @Column(name = "total_attempts", nullable = false)
    private Integer totalAttempts;

    @Column(name = "total_correct", nullable = false)
    private Integer totalCorrect;

    @Column(name = "total_incorrect", nullable = false)
    private Integer totalIncorrect;

    @Column(name = "success_rate_percent", precision = 5, scale = 2)
    private BigDecimal successRatePercent;

    @Column(name = "average_response_time_ms")
    private Integer averageResponseTimeMs;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @Column(name = "mastery_state", length = 20)
    private String masteryState;

    public Long getChildProfileId() {
        return childProfileId;
    }

    public void setChildProfileId(Long childProfileId) {
        this.childProfileId = childProfileId;
    }

    public Long getElementId() {
        return elementId;
    }

    public void setElementId(Long elementId) {
        this.elementId = elementId;
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

    public LocalDateTime getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(LocalDateTime lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public String getMasteryState() {
        return masteryState;
    }

    public void setMasteryState(String masteryState) {
        this.masteryState = masteryState;
    }
}
