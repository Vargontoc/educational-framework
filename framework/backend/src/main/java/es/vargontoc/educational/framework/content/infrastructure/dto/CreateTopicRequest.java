package es.vargontoc.educational.framework.content.infrastructure.dto;

import java.util.List;

public record CreateTopicRequest(
    String name,
    String description,
    Long categoryId,
    String status,
    Integer minAge,
    Integer maxAge,
    List<String> compatibleVariants
) {}
