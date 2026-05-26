package es.vargontoc.educational.framework.content.infrastructure.dto;

import java.time.LocalDateTime;

public record LearningPathStepResponse(
    Long id,
    Long learningPathId,
    Long activityId,
    Integer stepOrder,
    String unlockCondition,
    String visualMetadata,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
