package es.vargontoc.educational.framework.content.infrastructure.dto;

import java.time.LocalDateTime;
import java.util.List;

public record StoryResponse(
    Long id,
    String title,
    String description,
    Integer minAge,
    Integer maxAge,
    Integer estimatedDurationMinutes,
    List<Long> topicIds,
    String backgroundMusicResourceRef,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
