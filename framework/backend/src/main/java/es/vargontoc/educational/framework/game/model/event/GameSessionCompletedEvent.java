package es.vargontoc.educational.framework.game.model.event;

import es.vargontoc.educational.framework.tracking.model.GameSessionFinalStatus;

import java.time.LocalDateTime;

public record GameSessionCompletedEvent(
        Long gameId,
        Long childSessionId,
        Long activityId,
        GameSessionFinalStatus finalStatus,
        LocalDateTime occurredAt
) {
}
