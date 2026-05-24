package es.vargontoc.educational.framework.content.infrastructure.dto;

public record UpdateDifficultyLevelRequest(
    String difficultyCode,
    String engineParams,
    String adaptiveThresholdConfig
) {}
