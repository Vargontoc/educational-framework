package es.vargontoc.educational.framework.game.ports.in;

import es.vargontoc.educational.framework.game.model.GameState;

public interface GameOrchestrator {

    GameState startGame(Long childProfileId, Long activityId);

    GameState readyGame(Long gameId);

    GameState processAction(Long gameId, String actionPayload);

    GameState abandonGame(Long gameId);
}
