package es.vargontoc.educational.framework.tracking.infrastructure.dto;

import java.time.LocalDateTime;

public class DifficultyChangeRecord {

    private Long difficultyLevelId;
    private String direction;
    private LocalDateTime changedAt;

    public DifficultyChangeRecord() {
    }

    public DifficultyChangeRecord(Long difficultyLevelId, String direction, LocalDateTime changedAt) {
        this.difficultyLevelId = difficultyLevelId;
        this.direction = direction;
        this.changedAt = changedAt;
    }

    public Long getDifficultyLevelId() {
        return difficultyLevelId;
    }

    public void setDifficultyLevelId(Long difficultyLevelId) {
        this.difficultyLevelId = difficultyLevelId;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}
