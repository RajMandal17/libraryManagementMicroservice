package com.library.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDateTime;

/**
 * User Entity - Represents library members
 * 
 * SOLID Principles Applied:
 * 1. Single Responsibility: Only manages user data and basic validation
 * 2. Open/Closed: Can be extended without modification (inheritance)
 * 
 * Database Table: users
 * 
 * Key Fields:
 * - id: Unique identifier
 * - name: User's full name
 * - email: Unique email (used for login)
 * - phone: Contact number
 * - membershipType: Type of membership (affects max books)
 * - membershipStatus: Current status (active, suspended, expired)
 * - borrowedBooksCount: How many books currently borrowed
 * - maxBooksAllowed: Maximum books based on membership type
 * - joinedDate: When user registered
 * - expiryDate: When membership expires
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(unique = true, nullable = false)
    private String email;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    @Column(length = 10)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipType membershipType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipStatus membershipStatus;

    @Column(nullable = false)
    private LocalDateTime joinedDate;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private Integer borrowedBooksCount = 0;

    @Column(nullable = false)
    private Integer maxBooksAllowed;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Lifecycle callback: Executed before persisting new entity
     * Sets default values and calculates derived fields
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.borrowedBooksCount = 0;
        this.joinedDate = LocalDateTime.now();
        
        // Set membership expiry (1 year from now)
        this.expiryDate = LocalDateTime.now().plusYears(1);
        
        // Set max books based on membership type
        setMaxBooksBasedOnMembership();
    }

    /**
     * Lifecycle callback: Executed before updating entity
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Business Logic: Calculate max books allowed based on membership type
     * 
     * Note: This is simple logic that belongs in the entity.
     * Complex business logic should go in Service layer.
     */
    private void setMaxBooksBasedOnMembership() {
        switch (this.membershipType) {
            case STUDENT -> this.maxBooksAllowed = 3;
            case REGULAR -> this.maxBooksAllowed = 5;
            case PREMIUM -> this.maxBooksAllowed = 10;
            default -> this.maxBooksAllowed = 5;
        }
    }

    /**
     * Helper method: Check if user can borrow more books
     * This is a simple query method - business logic stays in Service layer
     */
    public boolean canBorrowBooks() {
        return this.membershipStatus == MembershipStatus.ACTIVE
                && this.borrowedBooksCount < this.maxBooksAllowed
                && this.expiryDate.isAfter(LocalDateTime.now());
    }
}
