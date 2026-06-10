package es.vargontoc.educational.framework.avatar.infrastructure.tts;

public class TtsException extends RuntimeException {

    private final String errorCode;
    private final boolean retryable;

    public TtsException(String message, String errorCode, boolean retryable) {
        super(message);
        this.errorCode = errorCode;
        this.retryable = retryable;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public boolean isRetryable() {
        return retryable;
    }
}