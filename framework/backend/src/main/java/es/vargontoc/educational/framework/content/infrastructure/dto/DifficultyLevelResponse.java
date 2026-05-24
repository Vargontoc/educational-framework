package es.vargontoc.educational.framework.content.infrastructure.dto;

import java.time.LocalDateTime;

public record DifficultyLevelResponse(
    Long id,
    Long activityId,
    String difficultyCode,
    String engineParams,
    String adaptiveThresholdConfig,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
