package es.vargontoc.educational.framework.session.infrastructure.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record SessionEvent(
    @JsonProperty("event") SessionEventType event,
    @JsonProperty("sessionId") Long sessionId,
    @JsonProperty("payload") Map<String, Object> payload
) {
    public static SessionEvent of(SessionEventType eventType, Long sessionId) {
        return new SessionEvent(eventType, sessionId, Map.of());
    }

    public static SessionEvent of(SessionEventType eventType, Long sessionId, Map<String, Object> payload) {
        return new SessionEvent(eventType, sessionId, payload);
    }
}
