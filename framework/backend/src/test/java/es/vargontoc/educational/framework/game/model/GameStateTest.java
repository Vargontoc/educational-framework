package es.vargontoc.educational.framework.game.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameStateTest {

    @Test
    void gameState_canRepresentSequenceNumber() {
        var state = new GameState();
        state.setSequenceNumber(42);

        assertEquals(42, state.getSequenceNumber());
    }

    @Test
    void gameState_canRepresentSystemEventPending() {
        var state = new GameState();

        state.setSystemEventPending(true);
        assertTrue(state.isSystemEventPending());

        state.setSystemEventPending(false);
        assertFalse(state.isSystemEventPending());
    }

    @Test
    void gameState_canRepresentFullGameState() {
        var state = new GameState();
        state.setGameId(1L);
        state.setChildSessionId(10L);
        state.setActivityId(100L);
        state.setDifficultyLevelId(5L);
        state.setStatus(GameStatus.IN_PROGRESS);
        state.setMetricType(ProgressMetricType.SCORE);
        state.setCurrentScore(new BigDecimal("150.50"));
        state.setCurrentStreak(5);
        state.setStarsEarned(3);
        state.setAttempts(10);
        state.setCorrectAttempts(7);
        state.setIncorrectAttempts(2);
        state.setTimeoutAttempts(1);
        state.setStartedAt(LocalDateTime.now().minusMinutes(5));
        state.setLastActivityAt(LocalDateTime.now());
        state.setSequenceNumber(15);
        state.setSystemEventPending(false);
        state.setEnginePayload("{\"question\":\"What is 2+2?\",\"options\":[3,4,5,6]}");

        assertEquals(1L, state.getGameId());
        assertEquals(10L, state.getChildSessionId());
        assertEquals(100L, state.getActivityId());
        assertEquals(5L, state.getDifficultyLevelId());
        assertEquals(GameStatus.IN_PROGRESS, state.getStatus());
        assertEquals(ProgressMetricType.SCORE, state.getMetricType());
        assertEquals(new BigDecimal("150.50"), state.getCurrentScore());
        assertEquals(5, state.getCurrentStreak());
        assertEquals(3, state.getStarsEarned());
        assertEquals(10, state.getAttempts());
        assertEquals(7, state.getCorrectAttempts());
        assertEquals(2, state.getIncorrectAttempts());
        assertEquals(1, state.getTimeoutAttempts());
        assertNotNull(state.getStartedAt());
        assertNotNull(state.getLastActivityAt());
        assertEquals(15, state.getSequenceNumber());
        assertFalse(state.isSystemEventPending());
        assertEquals("{\"question\":\"What is 2+2?\",\"options\":[3,4,5,6]}", state.getEnginePayload());
    }

    @Test
    void gameState_timeoutDoesNotMarkGameFailed() {
        var state = new GameState();
        state.setStatus(GameStatus.IN_PROGRESS);
        state.setTimeoutAttempts(1);
        state.setAttempts(1);

        assertEquals(GameStatus.IN_PROGRESS, state.getStatus());
        assertEquals(1, state.getTimeoutAttempts());
    }
}
