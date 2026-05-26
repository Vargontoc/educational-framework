package es.vargontoc.educational.framework.content.infrastructure.dto;

import java.time.LocalDateTime;

public record StoryPageResponse(
    Long id,
    Long storyId,
    Integer pageOrder,
    String text,
    String imageResourceRef,
    String audioResourceRef,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
