package es.vargontoc.educational.framework.game.model.event;

import es.vargontoc.educational.framework.game.model.enums.GameUnavailableReason;

/**
 * Payload of the GAME_UNAVAILABLE WebSocket event: tells the client that the requested game
 * cannot be played by this child profile.
 */
public record GameUnavailableEvent(Long gameId, Long activityId, GameUnavailableReason reason) {
}
