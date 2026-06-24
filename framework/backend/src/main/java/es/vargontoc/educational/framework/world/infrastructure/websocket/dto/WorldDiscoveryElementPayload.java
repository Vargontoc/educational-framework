package es.vargontoc.educational.framework.world.infrastructure.websocket.dto;

public record WorldDiscoveryElementPayload(
    String proposalRuntimeId,
    Long discoveryElementId,
    String code,
    String displayName,
    String elementType,
    String visualAssetKey,
    String interactionCueType,
    boolean hasActivity
) {
}