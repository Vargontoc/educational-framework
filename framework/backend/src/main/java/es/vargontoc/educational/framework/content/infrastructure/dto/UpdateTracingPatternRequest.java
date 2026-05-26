package es.vargontoc.educational.framework.content.infrastructure.dto;

import java.util.List;

public record UpdateTracingPatternRequest(
    Long topicId,
    String name,
    String description,
    String patternType,
    List<List<Double>> points,
    Integer minAge,
    Integer maxAge,
    String status
) {}
