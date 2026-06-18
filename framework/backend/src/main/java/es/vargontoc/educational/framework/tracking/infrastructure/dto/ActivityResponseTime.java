package es.vargontoc.educational.framework.tracking.infrastructure.dto;

public class ActivityResponseTime {

    private Long activityId;
    private Integer averageResponseTimeMs;
    private Integer minResponseTimeMs;
    private Integer maxResponseTimeMs;

    public ActivityResponseTime() {
    }

    public ActivityResponseTime(Long activityId, Integer averageResponseTimeMs, Integer minResponseTimeMs, Integer maxResponseTimeMs) {
        this.activityId = activityId;
        this.averageResponseTimeMs = averageResponseTimeMs;
        this.minResponseTimeMs = minResponseTimeMs;
        this.maxResponseTimeMs = maxResponseTimeMs;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public Integer getAverageResponseTimeMs() {
        return averageResponseTimeMs;
    }

    public void setAverageResponseTimeMs(Integer averageResponseTimeMs) {
        this.averageResponseTimeMs = averageResponseTimeMs;
    }

    public Integer getMinResponseTimeMs() {
        return minResponseTimeMs;
    }

    public void setMinResponseTimeMs(Integer minResponseTimeMs) {
        this.minResponseTimeMs = minResponseTimeMs;
    }

    public Integer getMaxResponseTimeMs() {
        return maxResponseTimeMs;
    }

    public void setMaxResponseTimeMs(Integer maxResponseTimeMs) {
        this.maxResponseTimeMs = maxResponseTimeMs;
    }
}
