package es.vargontoc.educational.framework.content.infrastructure.dto;

public record CreateLearningPathStepRequest(
    Long activityId,
    Integer stepOrder,
    String unlockCondition,
    String visualMetadata
) {}
