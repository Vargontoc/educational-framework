package es.vargontoc.educational.framework.world.infrastructure.websocket.dto;

public record WorldStateSyncPayload(
    String status,
    WorldDestinationPayload destination
) {
}