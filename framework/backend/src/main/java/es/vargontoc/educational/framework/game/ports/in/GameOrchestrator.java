package es.vargontoc.educational.framework.game.ports.in;

import es.vargontoc.educational.framework.game.model.ActionProcessingResult;
import es.vargontoc.educational.framework.game.model.GameState;

public interface GameOrchestrator {

    GameState startGame(Long childProfileId, Long activityId);

    GameState readyGame(Long gameId);

    ActionProcessingResult processAction(Long gameId, String actionPayload, Long topicId, Integer responseTimeMs);

    GameState abandonGame(Long gameId);

    void abandonGameForSession(Long childSessionId);
}
