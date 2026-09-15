package es.vargontoc.educational.framework.game.model.event;

import java.time.LocalDateTime;

public record GameSessionDiscardedEvent(
        Long gameId,
        Long childSessionId,
        Long activityId,
        LocalDateTime occurredAt
) {
}
