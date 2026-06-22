package es.vargontoc.educational.framework.tracking.model;

import java.time.LocalDateTime;
import java.util.List;

public record AttemptRegistrationResult(
        Long attemptId,
        LocalDateTime createdAt,
        List<UnlockedAchievement> unlockedAchievements,
        boolean difficultyChanged,
        Long newDifficultyLevelId) {

    public AttemptRegistrationResult(Long attemptId, LocalDateTime createdAt, List<UnlockedAchievement> unlockedAchievements) {
        this(attemptId, createdAt, unlockedAchievements, false, null);
    }
}