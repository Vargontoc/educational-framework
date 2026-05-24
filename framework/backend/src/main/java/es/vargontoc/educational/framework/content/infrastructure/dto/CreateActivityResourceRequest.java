package es.vargontoc.educational.framework.content.infrastructure.dto;

public record CreateActivityResourceRequest(
    Long activityId,
    Long topicId,
    String resourceType,
    String path,
    String metadata
) {}
