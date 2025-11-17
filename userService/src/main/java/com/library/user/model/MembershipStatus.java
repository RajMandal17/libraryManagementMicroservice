package com.library.user.model;

/**
 * MembershipStatus Enum
 * 
 * Tracks the current status of a user's membership
 * 
 * SOLID Principle: Single Responsibility - Only defines membership statuses
 */
public enum MembershipStatus {
    /**
     * ACTIVE - User can borrow books
     */
    ACTIVE,
    
    /**
     * SUSPENDED - User cannot borrow books
     * Reasons: Late returns, unpaid fines, policy violations
     */
    SUSPENDED,
    
    /**
     * EXPIRED - Membership has expired
     * User needs to renew to borrow books
     */
    EXPIRED
}
