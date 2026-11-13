package com.learning.auth.dto;

import com.learning.auth.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Size(max = 100) String fullName,
        @Email String email,
        @NotBlank @Size(min = 6, max = 100) String password,
        Role role) {
}
