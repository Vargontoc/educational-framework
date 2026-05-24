package es.vargontoc.educational.framework.content.infrastructure.dto;

public record UpdateCategoryRequest(
    String name,
    String description,
    String status,
    Integer displayOrder,
    String iconUrl
) {}
