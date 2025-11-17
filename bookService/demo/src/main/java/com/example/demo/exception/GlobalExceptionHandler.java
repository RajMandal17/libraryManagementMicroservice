package com.example.demo.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler - Centralized exception handling
 * 
 * SOLID Principles Applied:
 * 
 * 1. Single Responsibility (S):
 *    - ONLY handles exceptions (converts to HTTP responses)
 *    - Controller doesn't need try-catch blocks
 * 
 * 2. Open/Closed (O):
 *    - Can add new exception handlers without modifying existing ones
 * 
 * 3. Dependency Inversion (D):
 *    - Controllers depend on throwing exceptions, not handling them
 * 
 * @RestControllerAdvice - Applies to all @RestController classes
 * @ExceptionHandler - Catches specific exception types
 * 
 * Benefits:
 * ✅ Cleaner controller code (no try-catch)
 * ✅ Consistent error responses
 * ✅ Centralized logging
 * ✅ Easy to add new exception types
 * 
 * Example Flow:
 * 
 * BookController.getBook() 
 *   → BookService.getBook()
 *   → throws ResourceNotFoundException
 *   → GlobalExceptionHandler.handleResourceNotFound()
 *   → Returns HTTP 404 + ErrorResponse
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle ResourceNotFoundException
     * 
     * Triggered when:
     * - Book not found by ID/ISBN
     * - User not found (from User Service)
     * 
     * Returns: HTTP 404 Not Found
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            WebRequest request) {
        
        log.error("Resource not found: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle IllegalStateException
     * 
     * Triggered when:
     * - User not eligible to borrow (max limit reached)
     * - Book out of stock
     * - Invalid business operation
     * 
     * Returns: HTTP 409 Conflict
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(
            IllegalStateException ex,
            WebRequest request) {
        
        log.error("Illegal state: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handle IllegalArgumentException
     * 
     * Triggered when:
     * - Invalid method arguments
     * - Invalid business rule violations
     * 
     * Returns: HTTP 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            WebRequest request) {
        
        log.error("Illegal argument: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle validation errors
     * 
     * Triggered when @Valid validation fails:
     * - @NotBlank violated
     * - @Min violated
     * - @Email violated
     * 
     * Returns: HTTP 400 Bad Request with field-level errors
     * 
     * Example Response:
     * {
     *   "isbn": "ISBN is required",
     *   "title": "Title is required",
     *   "totalCopies": "Total copies must be at least 0"
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        
        log.error("Validation failed: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle all other unexpected exceptions
     * 
     * Triggered when:
     * - Unexpected runtime errors
     * - Null pointer exceptions
     * - Database connection issues
     * 
     * Returns: HTTP 500 Internal Server Error
     * 
     * Note: Don't expose internal error details to clients (security risk)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex,
            WebRequest request) {
        
        log.error("Unexpected error: ", ex);

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "An unexpected error occurred. Please try again later.",
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
