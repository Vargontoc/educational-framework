package es.vargontoc.educational.framework.game.ports.in;

import es.vargontoc.educational.framework.game.model.ActionResult;
import es.vargontoc.educational.framework.game.model.GameState;

public interface GameEnginePort {

    void initGame(GameState gameState, String engineParams);

    ActionResult processAction(GameState gameState, String actionPayload);

    String getNextElement(GameState gameState);

    boolean isGameComplete(GameState gameState);

    ActionResult buildSummary(GameState gameState);
}
