package es.vargontoc.educational.framework.game.exception;

public class GameNotFoundException extends GameLifecycleException {

    public GameNotFoundException(Long gameId) {
        super("Game not found with id: " + gameId);
    }
}
