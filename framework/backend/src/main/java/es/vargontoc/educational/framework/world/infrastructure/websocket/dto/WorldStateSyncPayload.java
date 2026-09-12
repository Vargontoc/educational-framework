package es.vargontoc.educational.framework.world.infrastructure.websocket.dto;

public record WorldStateSyncPayload(
    String status,
    WorldDestinationPayload destination,
    Double positionX,
    Double positionY
) {
    public WorldStateSyncPayload(String status, WorldDestinationPayload destination) {
        this(status, destination, null, null);
    }
}