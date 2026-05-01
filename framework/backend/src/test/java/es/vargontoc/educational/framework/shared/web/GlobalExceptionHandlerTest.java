package es.vargontoc.educational.framework.shared.web;

import es.vargontoc.educational.framework.shared.exception.AppException;
import es.vargontoc.educational.framework.shared.exception.ConflictException;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import es.vargontoc.educational.framework.shared.exception.SessionException;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleResourceNotFound_returns404AndErrorResponse() {
        var response = handler.handleResourceNotFound(new ResourceNotFoundException("resource missing"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().success());
        assertEquals("resource missing", response.getBody().message());
        assertNull(response.getBody().data());
    }

    @Test
    void handleValidation_returns400AndErrorResponse() {
        var response = handler.handleValidation(new ValidationException("invalid input"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().success());
        assertEquals("invalid input", response.getBody().message());
    }

    @Test
    void handleConflict_returns409AndErrorResponse() {
        var response = handler.handleConflict(new ConflictException("already exists"));
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertFalse(response.getBody().success());
        assertEquals("already exists", response.getBody().message());
    }

    @Test
    void handleSession_returns401AndErrorResponse() {
        var response = handler.handleSession(new SessionException("auth required"));
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse(response.getBody().success());
        assertEquals("auth required", response.getBody().message());
    }

    @Test
    void handleAppException_returnsConfiguredStatusAndErrorResponse() {
        var exception = new AppException("domain failure", HttpStatus.UNPROCESSABLE_ENTITY);
        var response = handler.handleAppException(exception);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertFalse(response.getBody().success());
        assertEquals("domain failure", response.getBody().message());
    }

    @Test
    void handleUnhandledException_returns500AndSafeMessage() {
        var response = handler.handleUnhandledException(new RuntimeException("secret detail"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().success());
        assertEquals("An unexpected error occurred", response.getBody().message());
    }
}
