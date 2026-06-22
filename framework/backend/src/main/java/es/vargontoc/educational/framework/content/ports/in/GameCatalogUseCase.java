package es.vargontoc.educational.framework.content.ports.in;

import es.vargontoc.educational.framework.content.model.GameCatalogReadiness;

public interface GameCatalogUseCase {

    GameCatalogReadiness getGameReadiness(Long childProfileId, Long activityId);
}