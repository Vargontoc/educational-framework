package es.vargontoc.educational.framework.game.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActionResultTest {

    @Test
    void actionResult_correctResult() {
        var result = new ActionResult();
        result.setResultType(ActionResultType.CORRECT);
        result.setResponseTimeMs(1500);
        result.setRecommendedAvatarEventType("CORRECT_ANSWER");
        result.setCompleted(false);
        result.setAttemptContext("{\"detail\":\"motor-specific data\"}");

        var newState = new GameState();
        newState.setAttempts(1);
        newState.setCorrectAttempts(1);
        result.setNewState(newState);

        assertEquals(ActionResultType.CORRECT, result.getResultType());
        assertEquals(1500, result.getResponseTimeMs());
        assertEquals("CORRECT_ANSWER", result.getRecommendedAvatarEventType());
        assertFalse(result.isCompleted());
        assertEquals("{\"detail\":\"motor-specific data\"}", result.getAttemptContext());
        assertNotNull(result.getNewState());
        assertEquals(1, result.getNewState().getCorrectAttempts());
    }

    @Test
    void actionResult_timeoutResult() {
        var result = new ActionResult();
        result.setResultType(ActionResultType.TIMEOUT);
        result.setResponseTimeMs(30000);
        result.setRecommendedAvatarEventType("TRY_AGAIN");
        result.setCompleted(false);

        var newState = new GameState();
        newState.setTimeoutAttempts(1);
        newState.setStatus(GameStatus.IN_PROGRESS);
        result.setNewState(newState);

        assertEquals(ActionResultType.TIMEOUT, result.getResultType());
        assertEquals(30000, result.getResponseTimeMs());
        assertFalse(result.isCompleted());
        assertEquals(GameStatus.IN_PROGRESS, result.getNewState().getStatus());
    }

    @Test
    void actionResult_completedResult() {
        var result = new ActionResult();
        result.setResultType(ActionResultType.CORRECT);
        result.setRecommendedAvatarEventType("GAME_COMPLETE");
        result.setCompleted(true);

        var newState = new GameState();
        newState.setStatus(GameStatus.COMPLETED);
        result.setNewState(newState);

        assertTrue(result.isCompleted());
        assertEquals(GameStatus.COMPLETED, result.getNewState().getStatus());
    }

    @Test
    void actionResult_incorrectResult() {
        var result = new ActionResult();
        result.setResultType(ActionResultType.INCORRECT);
        result.setResponseTimeMs(2000);
        result.setRecommendedAvatarEventType("INCORRECT_HINT");
        result.setCompleted(false);

        var newState = new GameState();
        newState.setIncorrectAttempts(1);
        result.setNewState(newState);

        assertEquals(ActionResultType.INCORRECT, result.getResultType());
        assertEquals(1, result.getNewState().getIncorrectAttempts());
    }
}
