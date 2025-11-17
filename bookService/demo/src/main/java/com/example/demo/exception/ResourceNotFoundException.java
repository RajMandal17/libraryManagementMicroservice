package com.example.demo.exception;

/**
 * ResourceNotFoundException - Custom exception for resource not found
 * 
 * SOLID Principles Applied:
 * 
 * 1. Single Responsibility (S):
 *    - ONLY represents "resource not found" scenario
 *    - Separate exception class for each error type
 * 
 * When to throw:
 * - Book not found by ID
 * - Book not found by ISBN
 * - User not found (when calling User Service)
 * 
 * Example Usage:
 * 
 * if (book == null) {
 *     throw new ResourceNotFoundException("Book not found with ISBN: " + isbn);
 * }
 * 
 * Result:
 * HTTP 404 Not Found
 * {
 *   "timestamp": "2025-11-17T10:30:00",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Book not found with ISBN: 978-0134685991",
 *   "path": "/api/books/978-0134685991"
 * }
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructor with custom message
     * 
     * @param message Error message describing what was not found
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor with message and cause
     * 
     * @param message Error message
     * @param cause Root cause exception
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
