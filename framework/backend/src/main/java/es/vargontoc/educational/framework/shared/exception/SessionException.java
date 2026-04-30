package es.vargontoc.educational.framework.shared.exception;

import org.springframework.http.HttpStatus;

public class SessionException extends AppException {

    public SessionException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
