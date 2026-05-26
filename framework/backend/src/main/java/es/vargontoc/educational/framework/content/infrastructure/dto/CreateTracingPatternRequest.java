package es.vargontoc.educational.framework.content.infrastructure.dto;

import java.util.List;

public record CreateTracingPatternRequest(
    Long topicId,
    String name,
    String description,
    List<List<Double>> points,
    String status
) {}
