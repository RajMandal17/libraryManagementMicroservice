package com.library.user.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UserDto - Data Transfer Object for User
 * 
 * SOLID Principles Applied:
 * 1. Single Responsibility: Only transfers data between client and server
 * 2. Interface Segregation: Client only sends what's needed (not internal fields)
 * 
 * Used For:
 * - Creating new users (POST requests)
 * - Updating existing users (PUT requests)
 * 
 * Why DTO?
 * - Hides internal entity structure from clients
 * - Prevents exposing sensitive fields (id, timestamps, etc.)
 * - Allows different validation rules for API vs Database
 * - Decouples API contract from database schema
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;

    @NotNull(message = "Membership type is required")
    private MembershipType membershipType;
}
