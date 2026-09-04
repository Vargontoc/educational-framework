package es.vargontoc.educational.framework.game.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameSessionTest {

    @Test
    void gameSession_canAccumulateGameStates() {
        var session = new GameSession();
        session.setSessionId(1L);
        session.setChildProfileId(100L);
        session.setActivityId(10L);
        session.setDifficultyLevelStartId(5L);
        session.setStatus(GameStatus.IN_PROGRESS);
        session.setCreatedAt(LocalDateTime.now());

        var state1 = new GameState();
        state1.setGameId(1L);
        state1.setSequenceNumber(1);
        state1.setAttempts(1);
        state1.setCorrectAttempts(1);

        var state2 = new GameState();
        state2.setGameId(2L);
        state2.setSequenceNumber(2);
        state2.setAttempts(1);
        state2.setCorrectAttempts(1);

        session.addGameState(state1);
        session.addGameState(state2);

        assertEquals(2, session.getGameStates().size());
        assertEquals(1L, session.getGameStates().get(0).getGameId());
        assertEquals(2L, session.getGameStates().get(1).getGameId());
    }

    @Test
    void gameSession_tracksDifficultyStartAndEnd() {
        var session = new GameSession();
        session.setDifficultyLevelStartId(3L);
        session.setDifficultyLevelEndId(5L);

        assertEquals(3L, session.getDifficultyLevelStartId());
        assertEquals(5L, session.getDifficultyLevelEndId());
    }

    @Test
    void gameSession_tracksStartAndEndTimestamps() {
        var session = new GameSession();
        var start = LocalDateTime.now().minusMinutes(10);
        var end = LocalDateTime.now();
        session.setCreatedAt(start);
        session.setEndedAt(end);

        assertEquals(start, session.getCreatedAt());
        assertEquals(end, session.getEndedAt());
        assertTrue(session.getEndedAt().isAfter(session.getCreatedAt()));
    }

    @Test
    void gameSession_canTrackTotals() {
        var session = new GameSession();
        session.setTotalScore(new BigDecimal("250.00"));
        session.setTotalAttempts(10);
        session.setTotalCorrect(7);
        session.setTotalIncorrect(2);
        session.setTotalTimeouts(1);

        assertEquals(new BigDecimal("250.00"), session.getTotalScore());
        assertEquals(10, session.getTotalAttempts());
        assertEquals(7, session.getTotalCorrect());
        assertEquals(2, session.getTotalIncorrect());
        assertEquals(1, session.getTotalTimeouts());
    }

    @Test
    void gameSession_defaultsToEmptyGameStatesList() {
        var session = new GameSession();

        assertNotNull(session.getGameStates());
        assertTrue(session.getGameStates().isEmpty());
    }
}
