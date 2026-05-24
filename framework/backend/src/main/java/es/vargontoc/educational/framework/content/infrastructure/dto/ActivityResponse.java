package es.vargontoc.educational.framework.content.infrastructure.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ActivityResponse(
    Long id,
    String name,
    String description,
    String gameEngineType,
    String status,
    Integer minAge,
    Integer maxAge,
    List<Long> topicIds,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
