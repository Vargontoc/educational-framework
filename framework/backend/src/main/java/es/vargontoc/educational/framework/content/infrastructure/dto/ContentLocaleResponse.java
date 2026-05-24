package es.vargontoc.educational.framework.content.infrastructure.dto;

import java.time.LocalDateTime;

public record ContentLocaleResponse(
    Long id,
    String entityType,
    Long entityId,
    String localeCode,
    String name,
    String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
