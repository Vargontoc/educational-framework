package es.vargontoc.educational.framework.session.infrastructure.persistence;

import es.vargontoc.educational.framework.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "child_session")
public class ChildSessionJpaEntity extends BaseEntity {

    @Column(name = "child_profile_id", nullable = false)
    private Long childProfileId;

    @Column(name = "family_id", nullable = false)
    private Long familyId;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "last_activity_at", nullable = false)
    private LocalDateTime lastActivityAt;

    @Column(name = "heartbeat_interval_seconds", nullable = false)
    private int heartbeatIntervalSeconds;

    @Column(name = "connection_meta", columnDefinition = "TEXT")
    private String connectionMeta;

    @Column(name = "persisted_game_state_ref", length = 255)
    private String persistedGameStateRef;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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
