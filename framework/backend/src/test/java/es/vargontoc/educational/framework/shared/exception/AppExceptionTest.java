package es.vargontoc.educational.framework.shared.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class AppExceptionTest {

    @Test
    void resourceNotFoundException_returns404() {
        var ex = new ResourceNotFoundException("not found");
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals("not found", ex.getMessage());
        assertInstanceOf(AppException.class, ex);
    }

    @Test
    void validationException_returns400() {
        var ex = new ValidationException("invalid");
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void sessionException_returns401() {
        var ex = new SessionException("unauthorized");
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }

    @Test
    void conflictException_returns409() {
        var ex = new ConflictException("conflict");
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }
}
