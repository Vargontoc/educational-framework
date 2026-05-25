package es.vargontoc.educational.framework.content.infrastructure.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CuriosityResponse(
    Long id,
    String text,
    Long topicId,
    Integer minAge,
    Integer maxAge,
    List<String> tags,
    String locale,
    String phoneticHint,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
