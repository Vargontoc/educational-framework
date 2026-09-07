package es.vargontoc.educational.framework.avatar.domain;

import java.util.Map;
import es.vargontoc.educational.framework.avatar.domain.enums.AvatarEventType;

public record AvatarEventResult(

    AvatarEventType eventType,
    String text,
    boolean audioAvailable,
    Map<String, Object> audioMetadata,
    boolean suppressed,
    byte[] audioData
){ 

    public static AvatarEventResult suppresed(AvatarEventType type) {
        return new AvatarEventResult(type, null, false, null, true, null);
    }
}