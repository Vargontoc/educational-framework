package es.vargontoc.educational.framework.game.infrastructure.websocket;

public enum GameErrorCode {
    GAME_NOT_FOUND,
    INVALID_ACTION,
    GAME_NOT_IN_PROGRESS,
    INVALID_STATE_TRANSITION,
    NO_ACTIVE_GAME,
    ENGINE_ERROR,
    PARSING_ERROR
}
