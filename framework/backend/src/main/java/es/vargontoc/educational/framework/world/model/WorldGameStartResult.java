package es.vargontoc.educational.framework.world.model;

import es.vargontoc.educational.framework.game.model.enums.EngineType;

public class WorldGameStartResult {

    private Long childSessionId;
    private Long activityId;
    private Long gameId;
    private WorldGameStartStatus status;
    private WorldDestination safeFallbackDestination;
    private EngineType engine;

    public WorldGameStartResult() {
    }

    public WorldGameStartResult(Long childSessionId, Long activityId, Long gameId, WorldGameStartStatus status,
                             WorldDestination safeFallbackDestination, EngineType engine) {
        this.childSessionId = childSessionId;
        this.activityId = activityId;
        this.gameId = gameId;
        this.status = status;
        this.safeFallbackDestination = safeFallbackDestination;
        this.engine = engine;
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

    public EngineType getEngine() {
        return engine;
    }

    public void setEngine(EngineType engine) {
        this.engine = engine;
    }

    
}