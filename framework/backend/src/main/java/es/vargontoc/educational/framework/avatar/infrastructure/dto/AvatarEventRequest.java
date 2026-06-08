package es.vargontoc.educational.framework.avatar.infrastructure.dto;

import es.vargontoc.educational.framework.content.model.AvatarEventType;

import java.util.Map;

public record AvatarEventRequest(
    Long childSessionId,
    AvatarEventType eventType,
    String locale,
    Map<String, Object> context
) {
    public AvatarEventRequest {
        if (context == null) {
            context = Map.of();
        }
    }
}