package es.vargontoc.educational.framework.content.infrastructure.dto;

public record CreateCategoryRequest(
    String name,
    String description,
    String status,
    Integer displayOrder,
    String iconUrl
) {}
