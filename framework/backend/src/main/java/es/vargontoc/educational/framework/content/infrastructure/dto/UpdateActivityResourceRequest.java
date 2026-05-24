package es.vargontoc.educational.framework.content.infrastructure.dto;

public record UpdateActivityResourceRequest(
    Long topicId,
    String resourceType,
    String path,
    String metadata
) {}
