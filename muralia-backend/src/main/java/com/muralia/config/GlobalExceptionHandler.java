package com.muralia.config;

import com.muralia.api.model.ErrorResponse;
import com.muralia.exception.CustomerNotFoundException;
import com.muralia.exception.EmptyFileException;
import com.muralia.exception.ImageNotFoundException;
import com.muralia.exception.InvalidFileTypeException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;

/**
 * Global exception handler that catches exceptions and returns proper HTTP responses.
 * Maps custom exceptions to appropriate HTTP status codes and ErrorResponse format.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ImageNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleImageNotFound(
            ImageNotFoundException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse();
        error.setTimestamp(OffsetDateTime.now());
        error.setStatus(HttpStatus.NOT_FOUND.value());
        error.setError(HttpStatus.NOT_FOUND.getReasonPhrase());
        error.setMessage(ex.getMessage());
        error.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(EmptyFileException.class)
    public ResponseEntity<ErrorResponse> handleEmptyFile(
            EmptyFileException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse();
        error.setTimestamp(OffsetDateTime.now());
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
        error.setMessage(ex.getMessage());
        error.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(InvalidFileTypeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFileType(
            InvalidFileTypeException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse();
        error.setTimestamp(OffsetDateTime.now());
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
        error.setMessage(ex.getMessage());
        error.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotFound(
            CustomerNotFoundException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse();
        error.setTimestamp(OffsetDateTime.now());
        error.setStatus(HttpStatus.NOT_FOUND.value());
        error.setError(HttpStatus.NOT_FOUND.getReasonPhrase());
        error.setMessage(ex.getMessage());
        error.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse();
        error.setTimestamp(OffsetDateTime.now());
        error.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.setError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        error.setMessage("An unexpected error occurred: " + ex.getMessage());
        error.setPath(request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
