package es.vargontoc.educational.framework.content.infrastructure.dto;

public record CreateDifficultyLevelRequest(
    Long activityId,
    String difficultyCode,
    String engineParams,
    String adaptiveThresholdConfig
) {}
