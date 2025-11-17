package com.library.user.repository;

import com.library.user.model.MembershipStatus;
import com.library.user.model.MembershipType;
import com.library.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * UserRepository - Data Access Layer for User entity
 * 
 * SOLID Principles Applied:
 * 1. Single Responsibility: ONLY handles database operations
 * 2. Dependency Inversion: Controller/Service depend on this interface, not implementation
 * 3. Open/Closed: Can extend JpaRepository without modifying it
 * 
 * Spring Data JPA automatically generates implementation at runtime
 * No need to write SQL queries for basic CRUD operations
 * 
 * Method Naming Convention (Spring Data JPA):
 * - findBy{FieldName} → SELECT * FROM users WHERE field_name = ?
 * - existsBy{FieldName} → SELECT EXISTS(SELECT 1 FROM users WHERE field_name = ?)
 * - countBy{FieldName} → SELECT COUNT(*) FROM users WHERE field_name = ?
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email address
     * Generated SQL: SELECT * FROM users WHERE email = ?
     * 
     * @param email user's email
     * @return Optional<User> - empty if not found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if email already exists in database
     * Generated SQL: SELECT EXISTS(SELECT 1 FROM users WHERE email = ?)
     * 
     * Used for validation during user registration
     * 
     * @param email email to check
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find all users by membership type
     * Generated SQL: SELECT * FROM users WHERE membership_type = ?
     * 
     * @param membershipType type of membership
     * @return List of users with that membership type
     */
    List<User> findByMembershipType(MembershipType membershipType);

    /**
     * Find all users by membership status
     * Generated SQL: SELECT * FROM users WHERE membership_status = ?
     * 
     * @param membershipStatus status of membership
     * @return List of users with that status
     */
    List<User> findByMembershipStatus(MembershipStatus membershipStatus);

    /**
     * Find all ACTIVE users
     * Generated SQL: SELECT * FROM users WHERE membership_status = 'ACTIVE'
     * 
     * @return List of active users
     */
    default List<User> findActiveUsers() {
        return findByMembershipStatus(MembershipStatus.ACTIVE);
    }

    /**
     * Find users who can borrow books (active with available slots)
     * This is a custom query - Spring Data generates it from method name
     * 
     * Generated SQL: 
     * SELECT * FROM users 
     * WHERE membership_status = 'ACTIVE' 
     * AND borrowed_books_count < max_books_allowed
     * 
     * @return List of users who can borrow books
     */
    List<User> findByMembershipStatusAndBorrowedBooksCountLessThan(
            MembershipStatus status, Integer maxBooks
    );
}
