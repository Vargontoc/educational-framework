package es.vargontoc.educational.framework.content.infrastructure.dto;

import java.util.List;

public record CreateActivityRequest(
    String name,
    String description,
    String gameEngineType,
    String status,
    Integer minAge,
    Integer maxAge,
    List<Long> topicIds
) {}
