package com.learning.auth.dto;

public record AuthResponse(String token, long expiresInSeconds, UserResponse user) {
}
