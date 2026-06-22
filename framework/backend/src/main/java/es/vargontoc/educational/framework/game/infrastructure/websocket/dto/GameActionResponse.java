package es.vargontoc.educational.framework.game.infrastructure.websocket.dto;

import es.vargontoc.educational.framework.game.model.ActionResultType;
import es.vargontoc.educational.framework.game.model.GameState;
import es.vargontoc.educational.framework.tracking.model.UnlockedAchievement;

import java.util.List;

public record GameActionResponse(
    ActionResultType resultType,
    GameState updatedState,
    boolean difficultyChanged,
    Long newDifficultyLevelId,
    boolean gameCompleted,
    List<UnlockedAchievement> unlockedAchievements,
    String attemptContext
) {
    public static GameActionResponse fromProcessingResult(
            es.vargontoc.educational.framework.game.model.ActionProcessingResult result) {
        return new GameActionResponse(
            result.resultType(),
            result.updatedState(),
            result.difficultyChanged(),
            result.newDifficultyLevelId(),
            result.gameCompleted(),
            result.unlockedAchievements(),
            result.attemptContext()
        );
    }
}
