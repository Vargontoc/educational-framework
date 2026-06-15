package es.vargontoc.educational.framework.avatar.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import es.vargontoc.educational.framework.session.infrastructure.websocket.SessionEventType;

public record GameAvatarEvent(
    @JsonProperty("event") SessionEventType event,
    @JsonProperty("sessionId") Long sessionId,
    @JsonProperty("eventType") String eventType,
    @JsonProperty("audioAvailable") boolean audioAvailable,
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty("audioId") String audioId,
    @JsonProperty("text") String text
) {
    public static GameAvatarEvent welcome(Long sessionId, boolean audioAvailable, String audioId, String text) {
        return new GameAvatarEvent(SessionEventType.GAME_AVATAR_EVENT, sessionId, "SESSION_CONNECTED", audioAvailable, audioId, text);
    }

    public static GameAvatarEvent farewell(Long sessionId, boolean audioAvailable, String audioId, String text) {
        return new GameAvatarEvent(SessionEventType.GAME_AVATAR_EVENT, sessionId, "SESSION_DISCONNECTED", audioAvailable, audioId, text);
    }
}