package com.library.user.serviceImpl;

import com.library.user.exception.ResourceNotFoundException;
import com.library.user.model.MembershipStatus;
import com.library.user.model.User;
import com.library.user.model.UserDto;
import com.library.user.repository.UserRepository;
import com.library.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * UserServiceImpl - Implementation of UserService interface
 * 
 * SOLID Principles Applied:
 * 
 * 1. Single Responsibility (S):
 *    - ONLY handles USER BUSINESS LOGIC
 *    - Database access → delegated to UserRepository
 *    - HTTP handling → delegated to UserController
 * 
 * 2. Open/Closed (O):
 *    - Open for extension (can create new implementations)
 *    - Closed for modification (implements interface contract)
 * 
 * 3. Liskov Substitution (L):
 *    - Can be replaced by any other UserService implementation
 *    - Maintains same behavior contract
 * 
 * 4. Dependency Inversion (D):
 *    - Depends on UserRepository INTERFACE
 *    - Injected via constructor (not new keyword)
 * 
 * @Service - Marks as Spring service component (auto-detected by Spring)
 * @RequiredArgsConstructor - Lombok generates constructor for final fields
 * @Slf4j - Lombok generates logger instance
 * @Transactional - Database transactions (rollback on error)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    // Dependency Injection via constructor (Dependency Inversion Principle)
    private final UserRepository userRepository;

    @Override
    public User createUser(UserDto userDto) {
        log.info("Creating new user with email: {}", userDto.getEmail());

        // Business Rule 1: Email must be unique
        if (userRepository.existsByEmail(userDto.getEmail())) {
            log.error("User with email {} already exists", userDto.getEmail());
            throw new IllegalArgumentException(
                "User with email " + userDto.getEmail() + " already exists"
            );
        }

        // Build user entity from DTO
        User user = User.builder()
            .name(userDto.getName())
            .email(userDto.getEmail())
            .phone(userDto.getPhone())
            .membershipType(userDto.getMembershipType())
            .membershipStatus(MembershipStatus.ACTIVE) // New users are active
            .build();

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {} and membership type: {}", 
                savedUser.getId(), savedUser.getMembershipType());

        return savedUser;
    }

    @Override
    @Transactional(readOnly = true) // Read-only transaction (better performance)
    public List<User> getAllUsers() {
        log.info("Fetching all users");
        List<User> users = userRepository.findAll();
        log.info("Found {} users", users.size());
        return users;
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        log.info("Fetching user with ID: {}", id);
        return userRepository.findById(id)
            .orElseThrow(() -> {
                log.error("User not found with ID: {}", id);
                return new ResourceNotFoundException("User not found with id: " + id);
            });
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        log.info("Fetching user with email: {}", email);
        return userRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.error("User not found with email: {}", email);
                return new ResourceNotFoundException("User not found with email: " + email);
            });
    }

    @Override
    public User updateUser(Long id, UserDto userDto) {
        log.info("Updating user with ID: {}", id);

        // Business Rule 1: User must exist
        User existingUser = getUserById(id);

        // Business Rule 2: If email is being changed, new email must not exist
        if (!existingUser.getEmail().equals(userDto.getEmail())) {
            if (userRepository.existsByEmail(userDto.getEmail())) {
                log.error("Email {} already exists", userDto.getEmail());
                throw new IllegalArgumentException(
                    "Email " + userDto.getEmail() + " already exists"
                );
            }
        }

        // Update fields
        existingUser.setName(userDto.getName());
        existingUser.setEmail(userDto.getEmail());
        existingUser.setPhone(userDto.getPhone());
        existingUser.setMembershipType(userDto.getMembershipType());

        User updatedUser = userRepository.save(existingUser);
        log.info("User updated successfully: {}", updatedUser.getName());

        return updatedUser;
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);
        
        // Business Rule: User must exist
        User user = getUserById(id);
        
        // In production: Consider soft delete instead
        // user.setDeleted(true); user.setDeletedAt(LocalDateTime.now());
        userRepository.delete(user);
        
        log.info("User deleted successfully: {}", user.getName());
    }

    @Override
    public User suspendUser(Long id) {
        log.info("Suspending user with ID: {}", id);
        
        User user = getUserById(id);
        user.setMembershipStatus(MembershipStatus.SUSPENDED);
        
        User suspended = userRepository.save(user);
        log.info("User suspended: {}", suspended.getName());
        
        return suspended;
    }

    @Override
    public User activateUser(Long id) {
        log.info("Activating user with ID: {}", id);
        
        User user = getUserById(id);
        user.setMembershipStatus(MembershipStatus.ACTIVE);
        
        User activated = userRepository.save(user);
        log.info("User activated: {}", activated.getName());
        
        return activated;
    }

    @Override
    public User renewMembership(Long id) {
        log.info("Renewing membership for user ID: {}", id);
        
        User user = getUserById(id);
        
        // Extend expiry by 1 year
        user.setExpiryDate(LocalDateTime.now().plusYears(1));
        
        // Activate if expired
        if (user.getMembershipStatus() == MembershipStatus.EXPIRED) {
            user.setMembershipStatus(MembershipStatus.ACTIVE);
        }
        
        User renewed = userRepository.save(user);
        log.info("Membership renewed for: {}", renewed.getName());
        
        return renewed;
    }

    @Override
    public User incrementBorrowedBooks(Long userId) {
        log.info("Incrementing borrowed books for user ID: {}", userId);
        
        User user = getUserById(userId);
        
        // Business Rule 1: Check if user can borrow more books
        if (!canBorrowMoreBooks(userId)) {
            log.error("User {} has reached maximum book limit or is not eligible", 
                    user.getName());
            throw new IllegalStateException(
                "User has reached maximum book limit or membership is not active"
            );
        }
        
        // Increment counter
        user.setBorrowedBooksCount(user.getBorrowedBooksCount() + 1);
        
        User updated = userRepository.save(user);
        log.info("User {} now has {} books borrowed", 
                updated.getName(), updated.getBorrowedBooksCount());
        
        return updated;
    }

    @Override
    public User decrementBorrowedBooks(Long userId) {
        log.info("Decrementing borrowed books for user ID: {}", userId);
        
        User user = getUserById(userId);
        
        // Business Rule: Cannot go below 0
        if (user.getBorrowedBooksCount() > 0) {
            user.setBorrowedBooksCount(user.getBorrowedBooksCount() - 1);
        }
        
        User updated = userRepository.save(user);
        log.info("User {} now has {} books borrowed", 
                updated.getName(), updated.getBorrowedBooksCount());
        
        return updated;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canBorrowMoreBooks(Long userId) {
        User user = getUserById(userId);
        
        // Business Rule 1: Membership must be ACTIVE
        if (user.getMembershipStatus() != MembershipStatus.ACTIVE) {
            log.warn("User {} cannot borrow - membership status: {}", 
                    user.getName(), user.getMembershipStatus());
            return false;
        }
        
        // Business Rule 2: Membership must not be expired
        if (user.getExpiryDate().isBefore(LocalDateTime.now())) {
            log.warn("User {} cannot borrow - membership expired on {}", 
                    user.getName(), user.getExpiryDate());
            return false;
        }
        
        // Business Rule 3: Must not exceed max books allowed
        if (user.getBorrowedBooksCount() >= user.getMaxBooksAllowed()) {
            log.warn("User {} cannot borrow - has {} books (max: {})", 
                    user.getName(), user.getBorrowedBooksCount(), user.getMaxBooksAllowed());
            return false;
        }
        
        log.info("User {} can borrow more books ({}/{})", 
                user.getName(), user.getBorrowedBooksCount(), user.getMaxBooksAllowed());
        return true;
    }
}
