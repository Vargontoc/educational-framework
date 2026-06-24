package es.vargontoc.educational.framework.world.model;

public class WorldGameStartResult {

    private Long childSessionId;
    private Long activityId;
    private Long gameId;
    private WorldGameStartStatus status;
    private WorldDestination safeFallbackDestination;

    public WorldGameStartResult() {
    }

    public WorldGameStartResult(Long childSessionId, Long activityId, Long gameId, WorldGameStartStatus status,
                             WorldDestination safeFallbackDestination) {
        this.childSessionId = childSessionId;
        this.activityId = activityId;
        this.gameId = gameId;
        this.status = status;
        this.safeFallbackDestination = safeFallbackDestination;
    }

    public Long getChildSessionId() {
        return childSessionId;
    }

    public void setChildSessionId(Long childSessionId) {
        this.childSessionId = childSessionId;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public WorldGameStartStatus getStatus() {
        return status;
    }

    public void setStatus(WorldGameStartStatus status) {
        this.status = status;
    }

    public WorldDestination getSafeFallbackDestination() {
        return safeFallbackDestination;
    }

    public void setSafeFallbackDestination(WorldDestination safeFallbackDestination) {
        this.safeFallbackDestination = safeFallbackDestination;
    }
}