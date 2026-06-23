package es.vargontoc.educational.framework.content.infrastructure.dto;

import java.util.List;

public record CompatibleActivityProjection(
    Long activityId,
    String displayName,
    String gameEngineType,
    List<Long> topicIds,
    Integer minAge,
    Integer maxAge,
    List<Long> difficultyLevelIds
) {}
