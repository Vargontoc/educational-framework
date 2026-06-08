package es.vargontoc.educational.framework.avatar.infrastructure.dto;

import es.vargontoc.educational.framework.content.model.AvatarEventType;

import java.util.Map;

public record AvatarEventResponse(
    AvatarEventType eventType,
    String text,
    boolean audioAvailable,
    Map<String, Object> audioMetadata,
    boolean suppressed
) {
    public static AvatarEventResponse fromResult(es.vargontoc.educational.framework.avatar.model.AvatarEventResult result) {
        return new AvatarEventResponse(
            result.getEventType(),
            result.getText(),
            result.isAudioAvailable(),
            result.getAudioMetadata(),
            result.isSuppressed()
        );
    }
}