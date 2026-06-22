package es.vargontoc.educational.framework.game.infrastructure.websocket.dto;

public record GameActionRequest(
    Long gameId,
    String action,
    Long topicId,
    Integer responseTimeMs
) {
    public Long gameIdOrThrow() {
        if (gameId == null) {
            throw new IllegalArgumentException("gameId is required");
        }
        return gameId;
    }

    public String actionOrThrow() {
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("action is required");
        }
        return action;
    }
}
