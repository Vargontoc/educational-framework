package es.vargontoc.educational.framework.content.infrastructure.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TopicResponse(
    Long id,
    String name,
    String description,
    Long categoryId,
    String status,
    Integer minAge,
    Integer maxAge,
    List<String> compatibleVariants,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
