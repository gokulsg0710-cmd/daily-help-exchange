package com.Gokul.daily_help_exchange.Exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /*
     * Handles task or user not found errors
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            ResourceNotFoundException ex
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
    }

    /*
     * Handles invalid task operations
     *
     * Example:
     * Trying to claim an already claimed task.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(
            IllegalStateException ex
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
    }

    /*
     * Handles @Valid validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error ->
                        error.getField() + ": "
                                + error.getDefaultMessage()
                )
                .orElse("Validation failed");

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

    /*
     * Handles PostgreSQL constraint errors.
     *
     * Examples:
     * - null value in a required column
     * - duplicate email or phone number
     * - invalid foreign key
     * - text longer than the column limit
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(
            DataIntegrityViolationException ex
    ) {
        String rootMessage = getRootCauseMessage(ex);

        ex.printStackTrace();

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Database rejected the data: " + rootMessage
        );
    }

    /*
     * Handles JPA transaction commit errors
     */
    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<Map<String, Object>> handleTransactionError(
            TransactionSystemException ex
    ) {
        String rootMessage = getRootCauseMessage(ex);

        ex.printStackTrace();

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Could not commit JPA transaction: " + rootMessage
        );
    }

    /*
     * Handles all other unexpected errors
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(
            Exception ex
    ) {
        String rootMessage = getRootCauseMessage(ex);

        ex.printStackTrace();

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                rootMessage
        );
    }

    /*
     * Finds the final database/JPA error.
     */
    private String getRootCauseMessage(Throwable exception) {
        Throwable rootCause = exception;

        while (rootCause.getCause() != null
                && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }

        String message = rootCause.getMessage();

        if (message == null || message.isBlank()) {
            return rootCause.getClass().getSimpleName();
        }

        return message;
    }

    /*
     * Creates a common JSON error response
     */
    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status,
            String message
    ) {
        Map<String, Object> body = new LinkedHashMap<>();

        body.put("time", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        return ResponseEntity
                .status(status)
                .body(body);
    }
}