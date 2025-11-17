package com.library.user.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ErrorResponse - Standard error response structure
 * 
 * SOLID Principle: Single Responsibility
 * - Only represents error response data
 * 
 * Sent to client when any error occurs
 * Provides consistent error format across all endpoints
 * 
 * Example JSON Response:
 * {
 *   "timestamp": "2024-11-17T10:30:00",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "User not found with id: 123",
 *   "path": "/api/users/123"
 * }
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {

    /**
     * When the error occurred
     */
    private LocalDateTime timestamp;

    /**
     * HTTP status code (404, 400, 500, etc.)
     */
    private int status;

    /**
     * HTTP status text (Not Found, Bad Request, etc.)
     */
    private String error;

    /**
     * Detailed error message
     */
    private String message;

    /**
     * Request path where error occurred
     */
    private String path;
}
