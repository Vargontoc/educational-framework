package es.vargontoc.educational.framework.tracking.infrastructure.dto;

public class DiarySummaryResponse {

    private Integer playedTimeMinutes;
    private Integer uniqueActivitiesCompleted;

    public DiarySummaryResponse() {
    }

    public DiarySummaryResponse(Integer playedTimeMinutes, Integer uniqueActivitiesCompleted) {
        this.playedTimeMinutes = playedTimeMinutes;
        this.uniqueActivitiesCompleted = uniqueActivitiesCompleted;
    }

    public Integer getPlayedTimeMinutes() {
        return playedTimeMinutes;
    }

    public void setPlayedTimeMinutes(Integer playedTimeMinutes) {
        this.playedTimeMinutes = playedTimeMinutes;
    }

    public Integer getUniqueActivitiesCompleted() {
        return uniqueActivitiesCompleted;
    }

    public void setUniqueActivitiesCompleted(Integer uniqueActivitiesCompleted) {
        this.uniqueActivitiesCompleted = uniqueActivitiesCompleted;
    }
}
