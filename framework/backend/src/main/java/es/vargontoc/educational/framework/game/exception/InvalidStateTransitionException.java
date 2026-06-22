package es.vargontoc.educational.framework.game.exception;

import es.vargontoc.educational.framework.game.model.GameStatus;

public class InvalidStateTransitionException extends GameLifecycleException {

    public InvalidStateTransitionException(GameStatus current, GameStatus target) {
        super("Invalid state transition from " + current + " to " + target);
    }
}
