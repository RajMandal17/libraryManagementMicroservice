package com.library.user.model;

/**
 * MembershipType Enum
 * 
 * Defines different types of library memberships
 * Each type has different privileges (max books allowed)
 * 
 * SOLID Principle: Single Responsibility - Only defines membership types
 */
public enum MembershipType {
    /**
     * STUDENT Membership
     * - Can borrow up to 3 books
     * - Usually discounted rates
     */
    STUDENT,
    
    /**
     * REGULAR Membership
     * - Can borrow up to 5 books
     * - Standard membership
     */
    REGULAR,
    
    /**
     * PREMIUM Membership
     * - Can borrow up to 10 books
     * - Priority access to new books
     */
    PREMIUM
}
