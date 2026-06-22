package es.vargontoc.educational.framework.content.model;

public record GameCatalogReadiness(
    Activity activity,
    DifficultyLevel difficultyLevel,
    boolean isNewToActivity
) {
}
