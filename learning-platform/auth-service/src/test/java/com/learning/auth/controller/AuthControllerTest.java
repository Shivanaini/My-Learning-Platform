package com.learning.auth.controller;

import com.learning.auth.dto.AuthResponse;
import com.learning.auth.dto.LoginRequest;
import com.learning.auth.dto.RegisterRequest;
import com.learning.auth.dto.UserResponse;
import com.learning.auth.model.Role;
import com.learning.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void loginEndpoint() {
        LoginRequest request = new LoginRequest("shiva", "password123");

        AuthResponse expected = new AuthResponse(
                "test-jwt",
                86400L,
                new UserResponse(1L, "shiva", "Shiva Naidu",
                        "shiva@example.com", "STUDENT")
        );

        when(authService.login(request)).thenReturn(expected);

        AuthResponse response = authController.login(request);

        assertEquals("test-jwt", response.token());
        assertEquals("shiva", response.user().username());

        verify(authService).login(request);
    }

    @Test
    void registerEndpoint() {
        RegisterRequest request = new RegisterRequest(
                "shiva",
                "Shiva Naidu",
                "shiva@example.com",
                "password123",
                Role.STUDENT
        );

        AuthResponse expected = new AuthResponse(
                "test-jwt",
                86400L,
                new UserResponse(1L, "shiva", "Shiva Naidu",
                        "shiva@example.com", "STUDENT")
        );

        when(authService.register(request)).thenReturn(expected);

        AuthResponse response = authController.register(request);

        assertEquals("test-jwt", response.token());
        assertEquals("STUDENT", response.user().role());

        verify(authService).register(request);
    }

    @Test
    void getCurrentUserWithBearerToken() {
        UserResponse expected = new UserResponse(
                1L,
                "shiva",
                "Shiva Naidu",
                "shiva@example.com",
                "STUDENT"
        );

        when(authService.me("valid-token")).thenReturn(expected);

        UserResponse response = authController.me("Bearer valid-token");

        assertEquals("shiva", response.username());

        verify(authService).me("valid-token");
    }

    @Test
    void getCurrentUserWithoutAuthorizationHeader() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authController.me(null)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());

        verifyNoInteractions(authService);
    }

    @Test
    void getCurrentUserWithInvalidAuthorizationHeader() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authController.me("Invalid token")
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());

        verifyNoInteractions(authService);
    }
}
