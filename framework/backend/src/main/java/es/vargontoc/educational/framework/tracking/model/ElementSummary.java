package es.vargontoc.educational.framework.tracking.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ElementSummary {

    private Long id;
    private Long childProfileId;
    private Long elementId;
    private Integer totalAttempts;
    private Integer totalCorrect;
    private Integer totalIncorrect;
    private BigDecimal successRatePercent;
    private Integer averageResponseTimeMs;
    private LocalDateTime lastSeenAt;
    private ElementMasteryState masteryState;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ElementSummary() {
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

    public ElementMasteryState getMasteryState() {
        return masteryState;
    }

    public void setMasteryState(ElementMasteryState masteryState) {
        this.masteryState = masteryState;
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
