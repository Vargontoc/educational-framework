package es.vargontoc.educational.framework.game.exception;

import es.vargontoc.educational.framework.game.model.enums.GameUnavailableReason;

/**
 * The game exists but the child profile cannot play it. Not an error: the client should return
 * to the world map (the discovery element remains visible).
 */
public class GameUnavailableException extends GameLifecycleException {

    private final Long gameId;
    private final Long activityId;
    private final GameUnavailableReason reason;

    public GameUnavailableException(Long gameId, Long activityId, GameUnavailableReason reason) {
        super("Game " + gameId + " is unavailable for this profile: " + reason);
        this.gameId = gameId;
        this.activityId = activityId;
        this.reason = reason;
    }

    public Long getGameId() {
        return gameId;
    }

    public Long getActivityId() {
        return activityId;
    }

    public GameUnavailableReason getReason() {
        return reason;
    }
}
