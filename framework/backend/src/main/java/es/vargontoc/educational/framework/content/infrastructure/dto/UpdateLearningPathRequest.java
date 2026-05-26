package es.vargontoc.educational.framework.content.infrastructure.dto;

public record UpdateLearningPathRequest(
    String name,
    String description,
    Integer minAge,
    Integer maxAge,
    String locale,
    String status
) {}
