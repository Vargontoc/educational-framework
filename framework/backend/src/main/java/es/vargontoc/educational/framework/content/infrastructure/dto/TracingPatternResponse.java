package es.vargontoc.educational.framework.content.infrastructure.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TracingPatternResponse(
    Long id,
    Long topicId,
    String name,
    String description,
    String patternType,
    List<List<Double>> points,
    Integer minAge,
    Integer maxAge,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
