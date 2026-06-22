package es.vargontoc.educational.framework.game.infrastructure;

import es.vargontoc.educational.framework.game.model.GameState;
import es.vargontoc.educational.framework.game.model.GameStatus;
import es.vargontoc.educational.framework.game.ports.out.GameStateRegistry;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryGameStateRegistry implements GameStateRegistry {

    private final Map<Long, GameState> gameById = new ConcurrentHashMap<>();
    private final Map<Long, Long> gameIdByChildSession = new ConcurrentHashMap<>();

    @Override
    public void save(GameState state) {
        gameById.put(state.getGameId(), state);
        if (state.getChildSessionId() != null && isActive(state.getStatus())) {
            gameIdByChildSession.put(state.getChildSessionId(), state.getGameId());
        }
    }

    @Override
    public Optional<GameState> findByGameId(Long gameId) {
        return Optional.ofNullable(gameById.get(gameId));
    }

    @Override
    public Optional<GameState> findByChildSessionId(Long childSessionId) {
        Long gameId = gameIdByChildSession.get(childSessionId);
        if (gameId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(gameById.get(gameId))
            .filter(state -> isActive(state.getStatus()));
    }

    @Override
    public void remove(Long gameId) {
        GameState removed = gameById.remove(gameId);
        if (removed != null && removed.getChildSessionId() != null) {
            gameIdByChildSession.remove(removed.getChildSessionId());
        }
    }

    @Override
    public boolean hasActiveGameForChildSession(Long childSessionId) {
        return findByChildSessionId(childSessionId).isPresent();
    }

    private boolean isActive(GameStatus status) {
        return status == GameStatus.WAITING
            || status == GameStatus.STARTING
            || status == GameStatus.IN_PROGRESS;
    }
}
