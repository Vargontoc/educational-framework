package es.vargontoc.educational.framework.game.engine;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import es.vargontoc.educational.framework.game.model.ActionResult;
import es.vargontoc.educational.framework.game.model.GameState;
import es.vargontoc.educational.framework.game.model.GameStatus;
import es.vargontoc.educational.framework.game.ports.in.GameEnginePort;

/**
 * Motor reconocimiento elementos
 * RecognitionEngine
 */
public class RecognitionEngine implements GameEnginePort{

    @Override
    public void initGame(GameState gameState, String engineParams) {
        gameState.setStatus(GameStatus.IN_PROGRESS);
        gameState.setAttempts(0);
        gameState.setCorrectAttempts(0);
        gameState.setIncorrectAttempts(0);
        gameState.setTimeoutAttempts(0);
        gameState.setCurrentScore(BigDecimal.ZERO);
        gameState.setCurrentStreak(0);
        gameState.setStarsEarned(0);
        gameState.setSequenceNumber(0);
        gameState.setSystemEventPending(false);
        gameState.setStartedAt(LocalDateTime.now());
        if (engineParams != null) {
            gameState.setEnginePayload(engineParams);
        }
    }

    @Override
    public ActionResult processAction(GameState gameState, String actionPayload) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'processAction'");
    }

    @Override
    public String getNextElement(GameState gameState) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getNextElement'");
    }

    @Override
    public boolean isGameComplete(GameState gameState) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isGameComplete'");
    }

    @Override
    public ActionResult buildSummary(GameState gameState) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buildSummary'");
    }
    
}
