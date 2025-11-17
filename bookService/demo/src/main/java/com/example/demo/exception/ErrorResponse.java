package com.example.demo.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ErrorResponse - Standardized error response format
 * 
 * SOLID Principles Applied:
 * 
 * 1. Single Responsibility (S):
 *    - ONLY represents error response structure
 *    - Consistent error format across all APIs
 * 
 * Purpose:
 * - Provide consistent error messages to clients
 * - Include timestamp, status, error details
 * - Help with debugging and logging
 * 
 * Example Response:
 * 
 * {
 *   "timestamp": "2025-11-17T10:30:00.123",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Book not found with ISBN: 978-0134685991",
 *   "path": "/api/books/978-0134685991"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /**
     * Timestamp when error occurred
     */
    private LocalDateTime timestamp;

    /**
     * HTTP status code
     * 
     * Common codes:
     * - 400 Bad Request (invalid input)
     * - 404 Not Found (resource doesn't exist)
     * - 409 Conflict (duplicate resource)
     * - 500 Internal Server Error (unexpected error)
     */
    private int status;

    /**
     * HTTP status text
     * 
     * Examples: "Not Found", "Bad Request", "Internal Server Error"
     */
    private String error;

    /**
     * Detailed error message
     * 
     * Should be user-friendly and descriptive
     */
    private String message;

    /**
     * Request path that caused the error
     * 
     * Example: "/api/books/978-0134685991"
     */
    private String path;

    /**
     * Factory method to create ErrorResponse
     * 
     * @param status HTTP status code
     * @param error Error type
     * @param message Error message
     * @param path Request path
     * @return ErrorResponse object
     */
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status,
                error,
                message,
                path
        );
    }
}
