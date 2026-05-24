package es.vargontoc.educational.framework.content.infrastructure.dto;

import java.time.LocalDateTime;

public record CategoryResponse(
    Long id,
    String name,
    String description,
    String status,
    Integer displayOrder,
    String iconUrl,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
