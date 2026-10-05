package es.vargontoc.educational.framework.session.infrastructure.websocket;

public class SynchronousSessionMessageDispatcher extends SessionMessageDispatcher {

    public SynchronousSessionMessageDispatcher(int pendingLimit, WebSocketMetrics metrics) {
        super(pendingLimit, metrics);
    }

    @Override
    public boolean dispatch(Long childSessionId, Runnable task) {
        try {
            task.run();
        } catch (Exception e) {
            // swallow like async version
        }
        return true;
    }

    @Override
    public void dispatchOrClose(Long childSessionId, Runnable task, Runnable onLimitExceeded) {
        try {
            task.run();
        } catch (Exception e) {
            // swallow
        }
    }

    @Override
    public void dispatchHeartbeat(Long childSessionId, Runnable task) {
        try {
            task.run();
        } catch (Exception e) {
            // swallow
        }
    }

    @Override
    public boolean awaitQuiescence(long timeoutMs) {
        return true;
    }
}
