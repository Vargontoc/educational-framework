package es.vargontoc.educational.framework.shared.web;

import es.vargontoc.educational.framework.shared.api.ApiResponse;
import es.vargontoc.educational.framework.shared.exception.AppException;
import es.vargontoc.educational.framework.shared.exception.ConflictException;
import es.vargontoc.educational.framework.shared.exception.ResourceNotFoundException;
import es.vargontoc.educational.framework.shared.exception.SessionException;
import es.vargontoc.educational.framework.shared.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final Marker EXCEPTION_MARKER = MarkerFactory.getMarker("EXCEPTION_MARKER");
    private static final String INTERNAL_ERROR_MESSAGE = "An unexpected error occurred";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(
            ResourceNotFoundException exception, HttpServletRequest request) {
        logDomainException(exception, request);
        return ResponseEntity
            .status(exception.getStatus())
            .body(ApiResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(
            ValidationException exception, HttpServletRequest request) {
        logDomainException(exception, request);
        return ResponseEntity
            .status(exception.getStatus())
            .body(ApiResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(
            ConflictException exception, HttpServletRequest request) {
        logDomainException(exception, request);
        return ResponseEntity
            .status(exception.getStatus())
            .body(ApiResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(SessionException.class)
    public ResponseEntity<ApiResponse<Void>> handleSession(
            SessionException exception, HttpServletRequest request) {
        logDomainException(exception, request);
        return ResponseEntity
            .status(exception.getStatus())
            .body(ApiResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(
            AppException exception, HttpServletRequest request) {
        logDomainException(exception, request);
        return ResponseEntity
            .status(exception.getStatus())
            .body(ApiResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMalformedRequestBody(
            HttpMessageNotReadableException exception, HttpServletRequest request) {
        putRequestContext(request, HttpStatus.BAD_REQUEST.value(), exception.getClass().getSimpleName());
        LOGGER.warn(EXCEPTION_MARKER, "Malformed request body at {} {}: {}",
            request.getMethod(), request.getRequestURI(), exception.getMessage());
        MDC.clear();
        return ResponseEntity
            .badRequest()
            .body(ApiResponse.error("Malformed or invalid request body"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        putRequestContext(request, HttpStatus.BAD_REQUEST.value(), exception.getClass().getSimpleName());
        LOGGER.warn(EXCEPTION_MARKER, "Validation failed at {} {}: {}",
            request.getMethod(), request.getRequestURI(), exception.getMessage());
        MDC.clear();
        return ResponseEntity
            .badRequest()
            .body(ApiResponse.error("Validation failed"));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(
            NoResourceFoundException exception, HttpServletRequest request) {
        HttpStatus status = request.getRequestURI().startsWith("/api/v1/dev/")
            ? HttpStatus.UNAUTHORIZED
            : HttpStatus.NOT_FOUND;
        putRequestContext(request, status.value(), exception.getClass().getSimpleName());
        LOGGER.warn(EXCEPTION_MARKER, "No resource found at {} {}",
            request.getMethod(), request.getRequestURI());
        MDC.clear();
        return ResponseEntity
            .status(status)
            .body(ApiResponse.error(status == HttpStatus.UNAUTHORIZED ? "Unauthorized" : "Resource not found"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnhandledException(
            Exception exception, HttpServletRequest request) {
        putRequestContext(request, 500, exception.getClass().getSimpleName());
        LOGGER.error(EXCEPTION_MARKER, "Unhandled exception at {} {}", request.getMethod(), request.getRequestURI(), exception);
        MDC.clear();
        return ResponseEntity
            .internalServerError()
            .body(ApiResponse.error(INTERNAL_ERROR_MESSAGE));
    }

    private void logDomainException(AppException exception, HttpServletRequest request) {
        putRequestContext(request, exception.getStatus().value(), exception.getClass().getSimpleName());
        LOGGER.warn(EXCEPTION_MARKER, "Domain exception at {} {}: {}",
            request.getMethod(), request.getRequestURI(), exception.getMessage());
        MDC.clear();
    }

    private void putRequestContext(HttpServletRequest request, int httpStatus, String exceptionType) {
        MDC.put("requestUri", request.getRequestURI());
        MDC.put("requestMethod", request.getMethod());
        MDC.put("httpStatus", String.valueOf(httpStatus));
        MDC.put("exceptionType", exceptionType);
    }
}
