package es.vargontoc.educational.framework.session.infrastructure.websocket;

public enum SessionEventType {

    GAME_STATE_UPDATE("State restore payload sent to game clients after reconnect."),
    SESSION_EXPIRED("Child session expired because inactivity grace window was exceeded."),
    SESSION_INVALIDATED("Family session was revoked externally, usually after a PIN change."),
    CHILD_EXPELLED("Parent or admin explicitly expelled the child from the active session."),
    PARENT_BLOCK("Child is blocked by parental control and cannot reconnect until unblocked."),
    HEARTBEAT_ACK("Acknowledgement emitted by server after receiving heartbeat."),
    AUTH_ACK("WebSocket session bound to an active child session after auth message."),
    CHILD_TTS_ACTIVATED("TTS Service is activated for the child."),
    CHILD_TTS_DEACTIVATED("TTS service is deactivated for the child."),
    CHILD_AGENT_ACTIVATED(  "Agent Child model is activated for the child"),
    CHILD_AGENT_DEACTIVATED("Agent Child model is desactivated for the child"),
    GAME_AVATAR_EVENT("Avatar audio event sent to game client."),
    GAME_ACTION_RESULT("Result of a game action processed by the orchestrator."),
    GAME_COMPLETED("Game session has completed successfully."),
    GAME_ERROR("Game action failed with an error code."),
    GAME_STARTED("Game created and waiting to be ready."),
    GAME_READY("Game transitioned to IN_PROGRESS and ready for actions."),
    GAME_ABANDONED("Game was abandoned by client."),
    SESSION_CONNECTED("Game client authenticated and avatar welcome sent."),
    SESSION_DISCONNECTED("Backend-initiated session end with avatar farewell sent.");

    private final String description;

    SessionEventType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
