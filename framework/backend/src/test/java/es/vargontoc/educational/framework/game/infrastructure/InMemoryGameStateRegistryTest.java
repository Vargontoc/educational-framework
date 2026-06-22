package es.vargontoc.educational.framework.game.infrastructure;

import es.vargontoc.educational.framework.game.model.GameState;
import es.vargontoc.educational.framework.game.model.GameStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryGameStateRegistryTest {

    private InMemoryGameStateRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new InMemoryGameStateRegistry();
    }

    private GameState createGameState(Long gameId, Long childSessionId, GameStatus status) {
        GameState state = new GameState();
        state.setGameId(gameId);
        state.setChildSessionId(childSessionId);
        state.setStatus(status);
        return state;
    }

    @Test
    void save_and_findByGameId_returnsStoredState() {
        GameState state = createGameState(1L, 100L, GameStatus.IN_PROGRESS);
        registry.save(state);

        Optional<GameState> result = registry.findByGameId(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getGameId());
        assertEquals(100L, result.get().getChildSessionId());
    }

    @Test
    void findByGameId_returnsEmptyForUnknownId() {
        Optional<GameState> result = registry.findByGameId(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void findByChildSessionId_returnsActiveGame() {
        GameState state = createGameState(1L, 100L, GameStatus.IN_PROGRESS);
        registry.save(state);

        Optional<GameState> result = registry.findByChildSessionId(100L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getGameId());
    }

    @Test
    void findByChildSessionId_returnsEmptyWhenNoActiveGame() {
        GameState state = createGameState(1L, 100L, GameStatus.COMPLETED);
        registry.save(state);

        Optional<GameState> result = registry.findByChildSessionId(100L);

        assertFalse(result.isPresent());
    }

    @Test
    void findByChildSessionId_returnsEmptyForUnknownSession() {
        Optional<GameState> result = registry.findByChildSessionId(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void remove_deletesGameAndClearsChildSessionMapping() {
        GameState state = createGameState(1L, 100L, GameStatus.IN_PROGRESS);
        registry.save(state);

        registry.remove(1L);

        assertFalse(registry.findByGameId(1L).isPresent());
        assertFalse(registry.findByChildSessionId(100L).isPresent());
    }

    @Test
    void hasActiveGameForChildSession_returnsTrueWhenActive() {
        GameState state = createGameState(1L, 100L, GameStatus.IN_PROGRESS);
        registry.save(state);

        assertTrue(registry.hasActiveGameForChildSession(100L));
    }

    @Test
    void hasActiveGameForChildSession_returnsFalseWhenNoGame() {
        assertFalse(registry.hasActiveGameForChildSession(100L));
    }

    @Test
    void hasActiveGameForChildSession_returnsFalseWhenGameCompleted() {
        GameState state = createGameState(1L, 100L, GameStatus.COMPLETED);
        registry.save(state);

        assertFalse(registry.hasActiveGameForChildSession(100L));
    }

    @Test
    void hasActiveGameForChildSession_returnsFalseWhenGameAbandoned() {
        GameState state = createGameState(1L, 100L, GameStatus.ABANDONED);
        registry.save(state);

        assertFalse(registry.hasActiveGameForChildSession(100L));
    }

    @Test
    void newChildSession_storesGameWhenStatusIsWaiting() {
        GameState state = createGameState(1L, 100L, GameStatus.WAITING);
        registry.save(state);

        assertTrue(registry.hasActiveGameForChildSession(100L));
    }

    @Test
    void newChildSession_storesGameWhenStatusIsStarting() {
        GameState state = createGameState(1L, 100L, GameStatus.STARTING);
        registry.save(state);

        assertTrue(registry.hasActiveGameForChildSession(100L));
    }

    @Test
    void multipleGamesForSameChildSession_lastOneWins() {
        GameState state1 = createGameState(1L, 100L, GameStatus.IN_PROGRESS);
        registry.save(state1);

        GameState state2 = createGameState(2L, 100L, GameStatus.IN_PROGRESS);
        registry.save(state2);

        Optional<GameState> result = registry.findByChildSessionId(100L);
        assertTrue(result.isPresent());
        assertEquals(2L, result.get().getGameId());
    }
}
