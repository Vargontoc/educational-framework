package es.vargontoc.educational.framework.content.infrastructure.dto;

import es.vargontoc.educational.framework.content.model.Activity;
import es.vargontoc.educational.framework.content.model.DifficultyLevel;

public record GameCatalogReadiness(
    Activity activity,
    DifficultyLevel difficultyLevel,
    boolean isNewToActivity
) {
}