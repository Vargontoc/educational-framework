package es.vargontoc.educational.framework.game.ports.in;

import es.vargontoc.educational.framework.game.model.ActionProcessingResult;
import es.vargontoc.educational.framework.game.model.GameState;
import es.vargontoc.educational.framework.game.model.LaunchContext;

public interface GameOrchestrator {

    GameState startGame(Long childProfileId, Long activityId);

    GameState startGame(Long childProfileId, Long activityId, LaunchContext launchContext);

    GameState readyGame(Long gameId);

    ActionProcessingResult processAction(Long gameId, String actionPayload, Long topicId, Integer responseTimeMs);

    GameState abandonGame(Long gameId);

    void abandonGameForSession(Long childSessionId);

    void clearSessionData(Long childSessionId);
}
