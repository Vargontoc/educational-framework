package es.vargontoc.educational.framework.avatar.domain;



import java.util.Map;

import es.vargontoc.educational.framework.avatar.domain.enums.AvatarEventType;

public record AvatarEventRequest(
    Long childSessionId,
    AvatarEventType eventType,
    Map<String, Object> context
) {
    public AvatarEventRequest {
        if (context == null) {
            context = Map.of();
        }
    }
}