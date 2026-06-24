package es.vargontoc.educational.framework.world.infrastructure.websocket.dto;

public record WorldNarrativeSituationPayload(
    Long id,
    String code,
    String displayText,
    String tone
) {
}