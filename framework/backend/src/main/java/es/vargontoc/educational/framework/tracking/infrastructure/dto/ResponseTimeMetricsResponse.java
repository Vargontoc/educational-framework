package es.vargontoc.educational.framework.tracking.infrastructure.dto;

import java.util.List;

public class ResponseTimeMetricsResponse {

    private Long childProfileId;
    private Integer overallAverageResponseTimeMs;
    private Integer overallMinResponseTimeMs;
    private Integer overallMaxResponseTimeMs;
    private List<ActivityResponseTime> byActivity;

    public Long getChildProfileId() {
        return childProfileId;
    }

    public void setChildProfileId(Long childProfileId) {
        this.childProfileId = childProfileId;
    }

    public Integer getOverallAverageResponseTimeMs() {
        return overallAverageResponseTimeMs;
    }

    public void setOverallAverageResponseTimeMs(Integer overallAverageResponseTimeMs) {
        this.overallAverageResponseTimeMs = overallAverageResponseTimeMs;
    }

    public Integer getOverallMinResponseTimeMs() {
        return overallMinResponseTimeMs;
    }

    public void setOverallMinResponseTimeMs(Integer overallMinResponseTimeMs) {
        this.overallMinResponseTimeMs = overallMinResponseTimeMs;
    }

    public Integer getOverallMaxResponseTimeMs() {
        return overallMaxResponseTimeMs;
    }

    public void setOverallMaxResponseTimeMs(Integer overallMaxResponseTimeMs) {
        this.overallMaxResponseTimeMs = overallMaxResponseTimeMs;
    }

    public List<ActivityResponseTime> getByActivity() {
        return byActivity;
    }

    public void setByActivity(List<ActivityResponseTime> byActivity) {
        this.byActivity = byActivity;
    }
}
