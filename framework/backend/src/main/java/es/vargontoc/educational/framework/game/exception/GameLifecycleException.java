package es.vargontoc.educational.framework.game.exception;

public class GameLifecycleException extends RuntimeException {

    public GameLifecycleException(String message) {
        super(message);
    }

    public GameLifecycleException(String message, Throwable cause) {
        super(message, cause);
    }
}
