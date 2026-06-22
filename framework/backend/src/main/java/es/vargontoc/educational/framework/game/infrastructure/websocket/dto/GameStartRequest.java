package es.vargontoc.educational.framework.game.infrastructure.websocket.dto;

public record GameStartRequest(
    Long activityId
) {
    public Long activityIdOrThrow() {
        if (activityId == null) {
            throw new IllegalArgumentException("activityId is required");
        }
        return activityId;
    }
}
