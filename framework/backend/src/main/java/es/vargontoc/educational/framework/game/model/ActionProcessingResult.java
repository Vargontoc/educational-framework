package es.vargontoc.educational.framework.game.model;

import es.vargontoc.educational.framework.tracking.model.UnlockedAchievement;

import java.util.ArrayList;
import java.util.List;

public record ActionProcessingResult(
    ActionResultType resultType,
    Integer responseTimeMs,
    GameState updatedState,
    boolean difficultyChanged,
    Long newDifficultyLevelId,
    boolean gameCompleted,
    List<UnlockedAchievement> unlockedAchievements,
    String attemptContext
) {
    public ActionProcessingResult(
        ActionResultType resultType,
        Integer responseTimeMs,
        GameState updatedState,
        boolean difficultyChanged,
        Long newDifficultyLevelId,
        boolean gameCompleted,
        String attemptContext
    ) {
        this(resultType, responseTimeMs, updatedState, difficultyChanged, newDifficultyLevelId, gameCompleted,
             new ArrayList<>(), attemptContext);
    }
}
