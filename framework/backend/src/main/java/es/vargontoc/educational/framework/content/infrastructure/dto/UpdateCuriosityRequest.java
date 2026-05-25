package es.vargontoc.educational.framework.content.infrastructure.dto;

import java.util.List;

public record UpdateCuriosityRequest(
    String text,
    Long topicId,
    Integer minAge,
    Integer maxAge,
    List<String> tags,
    String locale,
    String phoneticHint,
    String status
) {}
