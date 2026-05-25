package es.vargontoc.educational.framework.content.infrastructure.dto;

public record UpdateAvatarEventCatalogRequest(
    String eventType,
    String tone,
    String locale,
    String messageText,
    String status
) {}
