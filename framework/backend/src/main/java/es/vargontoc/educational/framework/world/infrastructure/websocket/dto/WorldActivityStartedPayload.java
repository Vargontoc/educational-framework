package es.vargontoc.educational.framework.world.infrastructure.websocket.dto;

public record WorldActivityStartedPayload(
    Long gameId,
    Long activityId,
    String transition
) {
}