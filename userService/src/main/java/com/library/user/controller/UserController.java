package com.library.user.controller;

import com.library.user.model.User;
import com.library.user.model.UserDto;
import com.library.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * UserController - REST API endpoints for User operations
 * 
 * SOLID Principles Applied:
 * 
 * 1. Single Responsibility (S):
 *    - ONLY handles HTTP requests/responses
 *    - Business logic → delegated to UserService
 *    - Exception handling → delegated to GlobalExceptionHandler
 * 
 * 2. Dependency Inversion (D):
 *    - Depends on UserService INTERFACE, not implementation
 *    - Injected via constructor
 * 
 * 3. Open/Closed (O):
 *    - Can add new endpoints without modifying existing ones
 * 
 * @RestController - Combines @Controller + @ResponseBody
 * @RequestMapping - Base path for all endpoints
 * @RequiredArgsConstructor - Lombok generates constructor
 * @Slf4j - Lombok generates logger
 * 
 * REST API Best Practices:
 * - Use proper HTTP methods (GET, POST, PUT, DELETE)
 * - Use proper HTTP status codes (200, 201, 404, etc.)
 * - Use plural nouns in URLs (/users, not /user)
 * - Use path variables for IDs (/users/{id})
 * - Validate input using @Valid
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    // Dependency Injection (Dependency Inversion Principle)
    private final UserService userService;

    /**
     * Register a new user
     * 
     * POST /api/users
     * 
     * Request Body:
     * {
     *   "name": "John Doe",
     *   "email": "john@example.com",
     *   "phone": "1234567890",
     *   "membershipType": "REGULAR"
     * }
     * 
     * Response: HTTP 201 Created + User object
     */
    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody UserDto userDto) {
        log.info("POST /api/users - Creating user: {}", userDto.getEmail());
        User createdUser = userService.createUser(userDto);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    /**
     * Get all users
     * 
     * GET /api/users
     * 
     * Response: HTTP 200 OK + List of users
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("GET /api/users - Fetching all users");
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Get user by ID
     * 
     * GET /api/users/{id}
     * 
     * Response: 
     * - HTTP 200 OK + User object (if found)
     * - HTTP 404 Not Found (if not found)
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        log.info("GET /api/users/{} - Fetching user", id);
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Get user by email
     * 
     * GET /api/users/email/{email}
     * 
     * Response:
     * - HTTP 200 OK + User object (if found)
     * - HTTP 404 Not Found (if not found)
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        log.info("GET /api/users/email/{} - Fetching user", email);
        User user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    /**
     * Update existing user
     * 
     * PUT /api/users/{id}
     * 
     * Request Body: UserDto (same as create)
     * 
     * Response:
     * - HTTP 200 OK + Updated user
     * - HTTP 404 Not Found (if user doesn't exist)
     * - HTTP 400 Bad Request (if email already exists)
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDto userDto) {
        log.info("PUT /api/users/{} - Updating user", id);
        User updatedUser = userService.updateUser(id, userDto);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Delete user
     * 
     * DELETE /api/users/{id}
     * 
     * Response:
     * - HTTP 200 OK + Success message
     * - HTTP 404 Not Found (if user doesn't exist)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        log.info("DELETE /api/users/{} - Deleting user", id);
        userService.deleteUser(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "User deleted successfully");
        response.put("userId", id.toString());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Suspend user's membership
     * 
     * PUT /api/users/{id}/suspend
     * 
     * Response: HTTP 200 OK + Updated user
     */
    @PutMapping("/{id}/suspend")
    public ResponseEntity<User> suspendUser(@PathVariable Long id) {
        log.info("PUT /api/users/{}/suspend - Suspending user", id);
        User suspendedUser = userService.suspendUser(id);
        return ResponseEntity.ok(suspendedUser);
    }

    /**
     * Activate user's membership
     * 
     * PUT /api/users/{id}/activate
     * 
     * Response: HTTP 200 OK + Updated user
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<User> activateUser(@PathVariable Long id) {
        log.info("PUT /api/users/{}/activate - Activating user", id);
        User activatedUser = userService.activateUser(id);
        return ResponseEntity.ok(activatedUser);
    }

    /**
     * Renew user's membership
     * 
     * PUT /api/users/{id}/renew
     * 
     * Response: HTTP 200 OK + Updated user
     */
    @PutMapping("/{id}/renew")
    public ResponseEntity<User> renewMembership(@PathVariable Long id) {
        log.info("PUT /api/users/{}/renew - Renewing membership", id);
        User renewedUser = userService.renewMembership(id);
        return ResponseEntity.ok(renewedUser);
    }

    /**
     * Increment borrowed books count
     * Called by Book Service when user borrows a book
     * 
     * PUT /api/users/{id}/borrow
     * 
     * Response:
     * - HTTP 200 OK + Updated user
     * - HTTP 409 Conflict (if max limit reached)
     */
    @PutMapping("/{id}/borrow")
    public ResponseEntity<User> borrowBook(@PathVariable Long id) {
        log.info("PUT /api/users/{}/borrow - User borrowing book", id);
        User updatedUser = userService.incrementBorrowedBooks(id);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Decrement borrowed books count
     * Called by Book Service when user returns a book
     * 
     * PUT /api/users/{id}/return
     * 
     * Response: HTTP 200 OK + Updated user
     */
    @PutMapping("/{id}/return")
    public ResponseEntity<User> returnBook(@PathVariable Long id) {
        log.info("PUT /api/users/{}/return - User returning book", id);
        User updatedUser = userService.decrementBorrowedBooks(id);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Check if user can borrow more books
     * Called by Book Service before allowing borrow
     * 
     * GET /api/users/{id}/can-borrow
     * 
     * Response: HTTP 200 OK + { "canBorrow": true/false }
     */
    @GetMapping("/{id}/can-borrow")
    public ResponseEntity<Map<String, Boolean>> canBorrowBooks(@PathVariable Long id) {
        log.info("GET /api/users/{}/can-borrow - Checking eligibility", id);
        boolean canBorrow = userService.canBorrowMoreBooks(id);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("canBorrow", canBorrow);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint
     * 
     * GET /api/users/health
     * 
     * Response: HTTP 200 OK + Status message
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "User Service");
        response.put("port", "8081");
        return ResponseEntity.ok(response);
    }
}
