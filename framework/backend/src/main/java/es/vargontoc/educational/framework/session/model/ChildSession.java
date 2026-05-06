package es.vargontoc.educational.framework.session.model;

import java.time.LocalDateTime;

public class ChildSession {

    private Long id;
    private Long childProfileId;
    private Long familyId;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Integer durationSeconds;
    private ChildSessionStatus status = ChildSessionStatus.ACTIVE;
    private LocalDateTime lastActivityAt;
    private int heartbeatIntervalSeconds = 30;
    private String connectionMeta;
    private String persistedGameStateRef;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getChildProfileId() {
        return childProfileId;
    }

    public void setChildProfileId(Long childProfileId) {
        this.childProfileId = childProfileId;
    }

    public Long getFamilyId() {
        return familyId;
    }

    public void setFamilyId(Long familyId) {
        this.familyId = familyId;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public ChildSessionStatus getStatus() {
        return status;
    }

    public void setStatus(ChildSessionStatus status) {
        this.status = status;
    }

    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(LocalDateTime lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public int getHeartbeatIntervalSeconds() {
        return heartbeatIntervalSeconds;
    }

    public void setHeartbeatIntervalSeconds(int heartbeatIntervalSeconds) {
        this.heartbeatIntervalSeconds = heartbeatIntervalSeconds;
    }

    public String getConnectionMeta() {
        return connectionMeta;
    }

    public void setConnectionMeta(String connectionMeta) {
        this.connectionMeta = connectionMeta;
    }

    public String getPersistedGameStateRef() {
        return persistedGameStateRef;
    }

    public void setPersistedGameStateRef(String persistedGameStateRef) {
        this.persistedGameStateRef = persistedGameStateRef;
    }
}
