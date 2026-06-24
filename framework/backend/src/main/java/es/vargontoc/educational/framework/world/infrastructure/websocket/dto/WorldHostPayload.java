package es.vargontoc.educational.framework.world.infrastructure.websocket.dto;

public record WorldHostPayload(
    Long id,
    String code,
    String displayName,
    String visualAssetKey
) {
}