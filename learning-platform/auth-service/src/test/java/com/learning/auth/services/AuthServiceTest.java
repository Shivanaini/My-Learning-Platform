package com.learning.auth.service;

import com.learning.auth.dto.AuthResponse;
import com.learning.auth.dto.LoginRequest;
import com.learning.auth.dto.RegisterRequest;
import com.learning.auth.dto.UserResponse;
import com.learning.auth.model.Role;
import com.learning.auth.model.User;
import com.learning.auth.repository.UserRepository;
import com.learning.auth.security.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository users;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtService jwt;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginWithValidCredentials() {
        User user = new User(
                "shiva", "Shiva Naidu", "shiva@example.com",
                "encoded-password", Role.STUDENT
        );

        when(users.findByUsername("shiva")).thenReturn(Optional.of(user));
        when(encoder.matches("password123", "encoded-password")).thenReturn(true);
        when(jwt.generateToken(user)).thenReturn("test-jwt");
        when(jwt.getExpirationSeconds()).thenReturn(86400L);

        AuthResponse response = authService.login(
                new LoginRequest(" shiva ", "password123")
        );

        assertEquals("test-jwt", response.token());
        assertEquals(86400L, response.expiresInSeconds());
        assertEquals("shiva", response.user().username());

        verify(users).findByUsername("shiva");
        verify(encoder).matches("password123", "encoded-password");
    }

    @Test
    void loginWithInvalidPassword() {
        User user = new User(
                "shiva", "Shiva Naidu", "shiva@example.com",
                "encoded-password", Role.STUDENT
        );

        when(users.findByUsername("shiva")).thenReturn(Optional.of(user));
        when(encoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(new LoginRequest("shiva", "wrong-password"))
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void loginWithUnknownUsername() {
        when(users.findByUsername("unknown")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(new LoginRequest("unknown", "password123"))
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verify(encoder, never()).matches(any(), any());
    }

    @Test
    void registerNewUserWithDefaultStudentRole() {
        when(users.existsByUsername("shiva")).thenReturn(false);
        when(encoder.encode("password123")).thenReturn("encoded-password");

        when(users.save(any(User.class))).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        when(jwt.generateToken(any(User.class))).thenReturn("test-jwt");
        when(jwt.getExpirationSeconds()).thenReturn(86400L);

        RegisterRequest request = new RegisterRequest(
                " shiva ",
                " Shiva Naidu ",
                "shiva@example.com",
                "password123",
                null
        );

        AuthResponse response = authService.register(request);

        assertEquals("test-jwt", response.token());
        assertEquals("shiva", response.user().username());
        assertEquals("Shiva Naidu", response.user().fullName());
        assertEquals("STUDENT", response.user().role());

        verify(users).existsByUsername("shiva");
        verify(encoder).encode("password123");
        verify(users).save(any(User.class));
    }

    @Test
    void registerWithExistingUsername() {
        when(users.existsByUsername("shiva")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.register(new RegisterRequest(
                        "shiva",
                        "Shiva Naidu",
                        "shiva@example.com",
                        "password123",
                        Role.STUDENT
                ))
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(users, never()).save(any(User.class));
        verify(encoder, never()).encode(any());
    }

    @Test
    void registerWithInstructorRole() {
        when(users.existsByUsername("instructor")).thenReturn(false);
        when(encoder.encode("password123")).thenReturn("encoded-password");

        when(users.save(any(User.class))).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        when(jwt.generateToken(any(User.class))).thenReturn("instructor-jwt");
        when(jwt.getExpirationSeconds()).thenReturn(86400L);

        RegisterRequest request = new RegisterRequest(
                "instructor",
                "Test Instructor",
                "instructor@example.com",
                "password123",
                Role.INSTRUCTOR
        );

        AuthResponse response = authService.register(request);

        assertEquals("INSTRUCTOR", response.user().role());
    }

    @Test
    void getCurrentUserWithValidToken() {
        Claims claims = mock(Claims.class);

        User user = new User(
                "shiva", "Shiva Naidu", "shiva@example.com",
                "encoded-password", Role.STUDENT
        );

        when(jwt.parse("valid-token")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("shiva");
        when(users.findByUsername("shiva")).thenReturn(Optional.of(user));

        UserResponse response = authService.me("valid-token");

        assertEquals("shiva", response.username());
        assertEquals("Shiva Naidu", response.fullName());
        assertEquals("STUDENT", response.role());
    }

    @Test
    void getCurrentUserWithInvalidToken() {
        when(jwt.parse("invalid-token"))
                .thenThrow(new RuntimeException("Invalid JWT"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.me("invalid-token")
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void getCurrentUserWhenUserDoesNotExist() {
        Claims claims = mock(Claims.class);

        when(jwt.parse("valid-token")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("unknown");
        when(users.findByUsername("unknown")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.me("valid-token")
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }
}