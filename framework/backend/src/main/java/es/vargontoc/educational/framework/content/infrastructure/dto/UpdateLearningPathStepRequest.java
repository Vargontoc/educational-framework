package es.vargontoc.educational.framework.content.infrastructure.dto;

public record UpdateLearningPathStepRequest(
    Long activityId,
    Integer stepOrder,
    String unlockCondition,
    String visualMetadata
) {}
