package es.vargontoc.educational.framework.tracking.model;

public record AdaptiveDifficultyResult(
        AdaptiveDifficultyAction action,
        String reason,
        Long newDifficultyLevelId
) {
    public AdaptiveDifficultyResult(AdaptiveDifficultyAction action, String reason) {
        this(action, reason, null);
    }
}
