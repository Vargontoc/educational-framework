package es.vargontoc.educational.framework.content.model;

import java.util.List;

public record CompatibleActivityProjection(
    Long activityId,
    String displayName,
    String engineType,
    List<Long> topicIds,
    Integer minAge,
    Integer maxAge,
    List<Long> difficultyLevelIds
) {}
