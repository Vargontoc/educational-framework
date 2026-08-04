package es.vargontoc.educational.framework.contact.model;

import org.springframework.http.HttpStatus;

import es.vargontoc.educational.framework.shared.exception.AppException;

public class ContactSendException extends AppException {

    public ContactSendException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public ContactSendException(String message, Throwable cause) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
        if (cause != null) {
            this.initCause(cause);
        }
    }
}
