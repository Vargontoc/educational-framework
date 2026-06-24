package es.vargontoc.educational.framework.world.model;

import java.time.LocalDateTime;

public class WorldHeartbeatResult {

    private Long childSessionId;
    private boolean worldStateFound;
    private LocalDateTime lastWorldActivityAt;
    private boolean childSessionActivityUpdated;
    private WorldInactivityStatus status;

    public WorldHeartbeatResult() {
    }

    public WorldHeartbeatResult(Long childSessionId, boolean worldStateFound, LocalDateTime lastWorldActivityAt,
                                boolean childSessionActivityUpdated) {
        this.childSessionId = childSessionId;
        this.worldStateFound = worldStateFound;
        this.lastWorldActivityAt = lastWorldActivityAt;
        this.childSessionActivityUpdated = childSessionActivityUpdated;
        this.status = WorldInactivityStatus.ACTIVE;
    }

    public WorldHeartbeatResult(Long childSessionId, boolean worldStateFound, LocalDateTime lastWorldActivityAt,
                                boolean childSessionActivityUpdated, WorldInactivityStatus status) {
        this.childSessionId = childSessionId;
        this.worldStateFound = worldStateFound;
        this.lastWorldActivityAt = lastWorldActivityAt;
        this.childSessionActivityUpdated = childSessionActivityUpdated;
        this.status = status;
    }

    public Long getChildSessionId() {
        return childSessionId;
    }

    public void setChildSessionId(Long childSessionId) {
        this.childSessionId = childSessionId;
    }

    public boolean isWorldStateFound() {
        return worldStateFound;
    }

    public void setWorldStateFound(boolean worldStateFound) {
        this.worldStateFound = worldStateFound;
    }

    public LocalDateTime getLastWorldActivityAt() {
        return lastWorldActivityAt;
    }

    public void setLastWorldActivityAt(LocalDateTime lastWorldActivityAt) {
        this.lastWorldActivityAt = lastWorldActivityAt;
    }

    public boolean isChildSessionActivityUpdated() {
        return childSessionActivityUpdated;
    }

    public void setChildSessionActivityUpdated(boolean childSessionActivityUpdated) {
        this.childSessionActivityUpdated = childSessionActivityUpdated;
    }

    public WorldInactivityStatus getStatus() {
        return status;
    }

    public void setStatus(WorldInactivityStatus status) {
        this.status = status;
    }
}