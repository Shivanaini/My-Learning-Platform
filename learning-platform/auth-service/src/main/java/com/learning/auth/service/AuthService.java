package com.learning.auth.service;

import com.learning.auth.dto.AuthResponse;
import com.learning.auth.dto.LoginRequest;
import com.learning.auth.dto.RegisterRequest;
import com.learning.auth.dto.UserResponse;
import com.learning.auth.model.Role;
import com.learning.auth.model.User;
import com.learning.auth.repository.UserRepository;
import com.learning.auth.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public AuthResponse login(LoginRequest req) {
        User user = users.findByUsername(req.username().trim())
                .filter(u -> encoder.matches(req.password(), u.getPassword()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));
        return toResponse(user);
    }

    public AuthResponse register(RegisterRequest req) {
        String username = req.username().trim();
        if (users.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
        }
        Role role = req.role() == null ? Role.STUDENT : req.role();
        User user = users.save(new User(username, req.fullName().trim(), req.email(),
                encoder.encode(req.password()), role));
        return toResponse(user);
    }

    public UserResponse me(String token) {
        try {
            String username = jwt.parse(token).getSubject();
            return users.findByUsername(username).map(UserResponse::from)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
    }

    private AuthResponse toResponse(User user) {
        return new AuthResponse(jwt.generateToken(user), jwt.getExpirationSeconds(), UserResponse.from(user));
    }
}
