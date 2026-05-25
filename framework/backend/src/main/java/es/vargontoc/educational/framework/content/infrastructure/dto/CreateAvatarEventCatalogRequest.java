package es.vargontoc.educational.framework.content.infrastructure.dto;

public record CreateAvatarEventCatalogRequest(
    String eventType,
    String tone,
    String locale,
    String messageText,
    String status
) {}
