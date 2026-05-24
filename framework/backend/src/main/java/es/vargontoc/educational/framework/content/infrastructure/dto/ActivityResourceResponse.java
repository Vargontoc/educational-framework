package es.vargontoc.educational.framework.content.infrastructure.dto;

import java.time.LocalDateTime;

public record ActivityResourceResponse(
    Long id,
    Long activityId,
    Long topicId,
    String resourceType,
    String path,
    String metadata,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
