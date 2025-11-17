package com.library.user.exception;

/**
 * ResourceNotFoundException - Custom exception for resource not found scenarios
 * 
 * SOLID Principle: Single Responsibility
 * - Only represents "resource not found" errors
 * 
 * Thrown when:
 * - User not found by ID
 * - User not found by email
 * - Any other resource lookup fails
 * 
 * This exception will be caught by GlobalExceptionHandler
 * and converted to HTTP 404 response
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructor with error message
     * 
     * @param message description of what resource was not found
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor with message and cause
     * 
     * @param message description of error
     * @param cause underlying exception
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
