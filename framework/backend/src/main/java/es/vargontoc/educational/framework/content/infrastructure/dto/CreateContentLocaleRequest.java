package es.vargontoc.educational.framework.content.infrastructure.dto;

public record CreateContentLocaleRequest(
    String entityType,
    Long entityId,
    String localeCode,
    String name,
    String description
) {}
