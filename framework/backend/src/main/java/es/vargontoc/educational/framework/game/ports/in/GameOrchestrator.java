package es.vargontoc.educational.framework.game.ports.in;

import es.vargontoc.educational.framework.game.model.ActionProcessingResult;
import es.vargontoc.educational.framework.game.model.GameState;
import es.vargontoc.educational.framework.game.model.LaunchContext;

public interface GameOrchestrator {

    GameState startGame(Long childProfileId, Long activityId);

    GameState startGame(Long childProfileId, Long activityId, LaunchContext launchContext);

    GameState readyGame(Long gameId);

    /**
     * Same as {@link #readyGame(Long)}, optionally skipping the Nubi audio of the first round so the caller
     * can answer the client first and attach the audio afterwards with {@link #attachRoundAudio(Long)}
     * (TTS can take seconds and the round can already be drawn without it).
     */
    GameState readyGame(Long gameId, boolean withRoundAudio);

    /** Generates the Nubi audio of the current round and attaches it to the game state. */
    GameState attachRoundAudio(Long gameId);

    ActionProcessingResult processAction(Long gameId, String actionPayload, Long topicId, Integer responseTimeMs);

    GameState abandonGame(Long gameId);

    void discardGameForSession(Long childSessionId);

    void clearSessionData(Long childSessionId);
}
