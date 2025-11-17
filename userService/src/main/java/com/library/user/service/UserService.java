package com.library.user.service;

import com.library.user.model.User;
import com.library.user.model.UserDto;

import java.util.List;

/**
 * UserService Interface - Contract for User business operations
 * 
 * SOLID Principles Applied:
 * 1. Dependency Inversion Principle (D): 
 *    - Controller depends on this INTERFACE, not concrete implementation
 *    - Easy to swap implementations (testing, different logic, etc.)
 * 
 * 2. Interface Segregation Principle (I):
 *    - Contains only methods related to User operations
 *    - Not bloated with unrelated methods
 * 
 * 3. Single Responsibility Principle (S):
 *    - Only defines business operations contract
 *    - Implementation handles actual logic
 * 
 * Why use Interface?
 * - Loose coupling between Controller and Service
 * - Easy to mock for unit testing
 * - Can have multiple implementations (e.g., UserServiceCacheImpl, UserServiceRestImpl)
 * - Follows programming to interfaces, not implementations
 */
public interface UserService {

    // ========== CRUD Operations ==========
    
    /**
     * Register a new user in the system
     * 
     * Business Rules:
     * - Email must be unique
     * - All required fields must be present
     * - Sets membership as ACTIVE by default
     * - Sets expiry date to 1 year from now
     * 
     * @param userDto user data from client
     * @return created User with generated ID
     * @throws IllegalArgumentException if email already exists
     */
    User createUser(UserDto userDto);

    /**
     * Get all users in the system
     * 
     * @return List of all users
     */
    List<User> getAllUsers();

    /**
     * Find user by unique ID
     * 
     * @param id user ID
     * @return User entity
     * @throws ResourceNotFoundException if user not found
     */
    User getUserById(Long id);

    /**
     * Find user by email address
     * 
     * @param email user's email
     * @return User entity
     * @throws ResourceNotFoundException if user not found
     */
    User getUserByEmail(String email);

    /**
     * Update existing user details
     * 
     * Business Rules:
     * - User must exist
     * - If changing email, new email must not already exist
     * 
     * @param id user ID to update
     * @param userDto updated user data
     * @return updated User
     * @throws ResourceNotFoundException if user not found
     * @throws IllegalArgumentException if new email already exists
     */
    User updateUser(Long id, UserDto userDto);

    /**
     * Delete a user from system
     * 
     * Note: In production, use soft delete (mark as deleted) instead
     * 
     * @param id user ID to delete
     * @throws ResourceNotFoundException if user not found
     */
    void deleteUser(Long id);

    // ========== Membership Management ==========
    
    /**
     * Suspend user's membership
     * User cannot borrow books when suspended
     * 
     * Reasons: Late returns, fines, policy violations
     * 
     * @param id user ID
     * @return updated User with SUSPENDED status
     * @throws ResourceNotFoundException if user not found
     */
    User suspendUser(Long id);

    /**
     * Activate suspended/expired user
     * 
     * @param id user ID
     * @return updated User with ACTIVE status
     * @throws ResourceNotFoundException if user not found
     */
    User activateUser(Long id);

    /**
     * Renew user's membership for another year
     * Sets expiry date to 1 year from now
     * Activates membership if expired
     * 
     * @param id user ID
     * @return updated User
     * @throws ResourceNotFoundException if user not found
     */
    User renewMembership(Long id);

    // ========== Book Borrowing Operations ==========
    // These will be called by Book Service via REST API
    
    /**
     * Increment borrowed books count
     * Called when user borrows a book
     * 
     * Business Rules:
     * - User must be ACTIVE
     * - Membership must not be expired
     * - Must not exceed max books allowed
     * 
     * @param userId user ID
     * @return updated User
     * @throws ResourceNotFoundException if user not found
     * @throws IllegalStateException if user reached max limit
     */
    User incrementBorrowedBooks(Long userId);

    /**
     * Decrement borrowed books count
     * Called when user returns a book
     * 
     * @param userId user ID
     * @return updated User
     * @throws ResourceNotFoundException if user not found
     */
    User decrementBorrowedBooks(Long userId);

    /**
     * Check if user can borrow more books
     * 
     * Checks:
     * - Membership is ACTIVE
     * - Membership not expired
     * - Has not reached max books limit
     * 
     * @param userId user ID
     * @return true if can borrow, false otherwise
     */
    boolean canBorrowMoreBooks(Long userId);
}
