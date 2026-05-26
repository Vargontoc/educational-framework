package es.vargontoc.educational.framework.content.infrastructure.dto;

import java.util.List;

public record UpdateStoryRequest(
    String title,
    String description,
    Integer minAge,
    Integer maxAge,
    Integer estimatedDurationMinutes,
    List<Long> topicIds,
    String backgroundMusicResourceRef,
    String status
) {}
