package es.vargontoc.educational.framework.content.infrastructure.dto;

import java.time.LocalDateTime;

public record AvatarEventCatalogResponse(
    Long id,
    String eventType,
    String tone,
    String locale,
    String messageText,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
