package es.vargontoc.educational.framework.game.engine;

import es.vargontoc.educational.framework.game.model.ActionResult;
import es.vargontoc.educational.framework.game.model.ActionResultType;
import es.vargontoc.educational.framework.game.model.GameState;
import es.vargontoc.educational.framework.game.model.GameStatus;
import es.vargontoc.educational.framework.game.ports.in.GameEnginePort;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FakeGameEngine implements GameEnginePort {

    private static final int DEFAULT_ACTIONS_TO_COMPLETE = 3;
    private final int actionsToComplete;

    public FakeGameEngine() {
        this(DEFAULT_ACTIONS_TO_COMPLETE);
    }

    public FakeGameEngine(int actionsToComplete) {
        this.actionsToComplete = actionsToComplete;
    }

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
        gameState.setSequenceNumber(gameState.getSequenceNumber() + 1);
        gameState.setAttempts(gameState.getAttempts() + 1);

        ActionResult result = new ActionResult();
        result.setAttemptContext("fake-engine-context-" + gameState.getSequenceNumber());

        if (actionPayload == null || actionPayload.isEmpty()) {
            result.setResultType(ActionResultType.TIMEOUT);
            result.setResponseTimeMs(30000);
            result.setRecommendedAvatarEventType("TRY_AGAIN");
            gameState.setTimeoutAttempts(gameState.getTimeoutAttempts() + 1);
            result.setNewState(cloneState(gameState));
            result.setCompleted(false);
            return result;
        }

        if (actionPayload.startsWith("CORRECT:")) {
            result.setResultType(ActionResultType.CORRECT);
            result.setResponseTimeMs(2000);
            result.setRecommendedAvatarEventType("CORRECT_ANSWER");
            gameState.setCorrectAttempts(gameState.getCorrectAttempts() + 1);
            gameState.setCurrentScore(gameState.getCurrentScore().add(BigDecimal.TEN));
            gameState.setCurrentStreak(gameState.getCurrentStreak() + 1);
        } else if (actionPayload.startsWith("INCORRECT:")) {
            result.setResultType(ActionResultType.INCORRECT);
            result.setResponseTimeMs(1500);
            result.setRecommendedAvatarEventType("INCORRECT_HINT");
            gameState.setIncorrectAttempts(gameState.getIncorrectAttempts() + 1);
            gameState.setCurrentStreak(0);
        } else if (actionPayload.startsWith("TIMEOUT")) {
            result.setResultType(ActionResultType.TIMEOUT);
            result.setResponseTimeMs(30000);
            result.setRecommendedAvatarEventType("TRY_AGAIN");
            gameState.setTimeoutAttempts(gameState.getTimeoutAttempts() + 1);
        } else {
            result.setResultType(ActionResultType.INCORRECT);
            result.setResponseTimeMs(1000);
            result.setRecommendedAvatarEventType("INCORRECT_HINT");
            gameState.setIncorrectAttempts(gameState.getIncorrectAttempts() + 1);
        }

        boolean isComplete = gameState.getCorrectAttempts() >= actionsToComplete;
        result.setCompleted(isComplete);

        if (isComplete) {
            gameState.setStatus(GameStatus.COMPLETED);
            gameState.setCompletedAt(LocalDateTime.now());
            gameState.setStarsEarned(calculateStars(gameState));
            result.setRecommendedAvatarEventType("GAME_COMPLETE");
        }

        result.setNewState(cloneState(gameState));
        return result;
    }

    @Override
    public String getNextElement(GameState gameState) {
        return "{\"type\":\"question\",\"content\":\"Fake Question " + (gameState.getSequenceNumber() + 1) + "\"}";
    }

    @Override
    public boolean isGameComplete(GameState gameState) {
        return gameState.getCorrectAttempts() >= actionsToComplete;
    }

    @Override
    public ActionResult buildSummary(GameState gameState) {
        ActionResult summary = new ActionResult();
        summary.setResultType(ActionResultType.CORRECT);
        summary.setCompleted(true);
        summary.setAttemptContext("fake-engine-summary");
        summary.setNewState(cloneState(gameState));
        return summary;
    }

    private int calculateStars(GameState gameState) {
        if (gameState.getIncorrectAttempts() == 0 && gameState.getTimeoutAttempts() == 0) {
            return 3;
        } else if (gameState.getIncorrectAttempts() <= 2) {
            return 2;
        }
        return 1;
    }

    private GameState cloneState(GameState source) {
        GameState clone = new GameState();
        clone.setGameId(source.getGameId());
        clone.setChildSessionId(source.getChildSessionId());
        clone.setActivityId(source.getActivityId());
        clone.setDifficultyLevelId(source.getDifficultyLevelId());
        clone.setStatus(source.getStatus());
        clone.setMetricType(source.getMetricType());
        clone.setCurrentScore(source.getCurrentScore());
        clone.setCurrentStreak(source.getCurrentStreak());
        clone.setStarsEarned(source.getStarsEarned());
        clone.setAttempts(source.getAttempts());
        clone.setCorrectAttempts(source.getCorrectAttempts());
        clone.setIncorrectAttempts(source.getIncorrectAttempts());
        clone.setTimeoutAttempts(source.getTimeoutAttempts());
        clone.setStartedAt(source.getStartedAt());
        clone.setLastActivityAt(source.getLastActivityAt());
        clone.setCompletedAt(source.getCompletedAt());
        clone.setSequenceNumber(source.getSequenceNumber());
        clone.setSystemEventPending(source.isSystemEventPending());
        clone.setEnginePayload(source.getEnginePayload());
        clone.setEngine(source.getEngine());
        return clone;
    }
}
