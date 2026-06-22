package es.vargontoc.educational.framework.game.exception;

public class EngineNotAvailableException extends GameLifecycleException {

    public EngineNotAvailableException(String engineType) {
        super("Game engine not available: " + engineType + ". Configure a valid engine or run in development mode.");
    }
}
