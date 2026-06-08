package es.vargontoc.educational.framework.content.infrastructure.dto;

import java.time.LocalDateTime;

import es.vargontoc.educational.framework.content.model.ContentStatus;

public record LearningPathStepResponse(
    Long id,
    Long learningPathId,
    Long activityId,
    Integer stepOrder,
    String unlockCondition,
    String visualMetadata,
    ContentStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
