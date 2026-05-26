package es.vargontoc.educational.framework.content.infrastructure.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TracingPatternResponse(
    Long id,
    Long topicId,
    String name,
    String description,
    List<List<Double>> points,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
