package es.vargontoc.educational.framework.tracking.infrastructure.dto;

public class AbandonmentSignalResponse {

    private Long activityId;
    private Integer abandonmentCount;

    public AbandonmentSignalResponse() {
    }

    public AbandonmentSignalResponse(Long activityId, Integer abandonmentCount) {
        this.activityId = activityId;
        this.abandonmentCount = abandonmentCount;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public Integer getAbandonmentCount() {
        return abandonmentCount;
    }

    public void setAbandonmentCount(Integer abandonmentCount) {
        this.abandonmentCount = abandonmentCount;
    }
}
