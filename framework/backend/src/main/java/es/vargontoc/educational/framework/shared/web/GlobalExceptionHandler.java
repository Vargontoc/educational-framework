package es.vargontoc.educational.framework.shared.web;

import es.vargontoc.educational.framework.shared.api.ApiResponse;
import es.vargontoc.educational.framework.shared.exception.AppException;
import es.vargontoc.educational.framework.shared.exception.ConflictException;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import es.vargontoc.educational.framework.shared.exception.SessionException;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String INTERNAL_ERROR_MESSAGE = "An unexpected error occurred";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException exception) {
        return ResponseEntity
            .status(exception.getStatus())
            .body(ApiResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(ValidationException exception) {
        return ResponseEntity
            .status(exception.getStatus())
            .body(ApiResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(ConflictException exception) {
        return ResponseEntity
            .status(exception.getStatus())
            .body(ApiResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(SessionException.class)
    public ResponseEntity<ApiResponse<Void>> handleSession(SessionException exception) {
        return ResponseEntity
            .status(exception.getStatus())
            .body(ApiResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException exception) {
        return ResponseEntity
            .status(exception.getStatus())
            .body(ApiResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnhandledException(Exception exception) {
        LOGGER.error("Unhandled exception", exception);
        return ResponseEntity
            .internalServerError()
            .body(ApiResponse.error(INTERNAL_ERROR_MESSAGE));
    }
}
