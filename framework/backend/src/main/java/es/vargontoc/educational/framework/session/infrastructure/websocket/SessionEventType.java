package es.vargontoc.educational.framework.session.infrastructure.websocket;

public enum SessionEventType {

    GAME_STATE_UPDATE("State restore payload sent to game clients after reconnect."),
    SESSION_EXPIRED("Child session expired because inactivity grace window was exceeded."),
    SESSION_INVALIDATED("Family session was revoked externally, usually after a PIN change."),
    CHILD_EXPELLED("Parent or admin explicitly expelled the child from the active session."),
    PARENT_BLOCK("Child is blocked by parental control and cannot reconnect until unblocked."),
    HEARTBEAT_ACK("Acknowledgement emitted by server after receiving heartbeat.");

    private final String description;

    SessionEventType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
