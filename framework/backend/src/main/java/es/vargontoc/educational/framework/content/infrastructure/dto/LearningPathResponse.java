package es.vargontoc.educational.framework.content.infrastructure.dto;

import java.time.LocalDateTime;

public record LearningPathResponse(
    Long id,
    String name,
    String description,
    Integer minAge,
    Integer maxAge,
    String locale,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
