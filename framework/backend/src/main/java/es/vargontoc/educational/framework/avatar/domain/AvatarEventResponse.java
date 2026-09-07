package es.vargontoc.educational.framework.avatar.domain;



import java.util.Map;

import es.vargontoc.educational.framework.avatar.domain.enums.AvatarEventType;

public record AvatarEventResponse(
    AvatarEventType eventType,
    String text,
    boolean audioAvailable,
    Map<String, Object> audioMetadata,
    boolean suppressed
) {
    public static AvatarEventResponse fromResult(AvatarEventResult result) {
        return new AvatarEventResponse(
            result.eventType(),
            result.text(),
            result.audioAvailable(),
            result.audioMetadata(),
            result.suppressed()
        );
    }
}