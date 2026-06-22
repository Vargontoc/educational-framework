package es.vargontoc.educational.framework.game.ports.out;

import es.vargontoc.educational.framework.game.model.GameState;

import java.util.Optional;

public interface GameStateRegistry {

    void save(GameState state);

    Optional<GameState> findByGameId(Long gameId);

    Optional<GameState> findByChildSessionId(Long childSessionId);

    void remove(Long gameId);

    boolean hasActiveGameForChildSession(Long childSessionId);
}
