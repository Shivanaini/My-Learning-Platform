package com.learning.auth.dto;

import com.learning.auth.model.User;

public record UserResponse(Long id, String username, String fullName, String email, String role) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getFullName(), u.getEmail(), u.getRole().name());
    }
}
