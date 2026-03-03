package com.example.userservice.service;

import com.example.common.security.JwtUtil;
import com.example.userservice.dto.AuthResponse;
import com.example.userservice.dto.LoginRequest;
import com.example.userservice.dto.RegisterRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.model.UserEntity;
import com.example.userservice.model.UserRole;
import com.example.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final long ttlSeconds;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            @Value("${app.jwt.ttl-seconds}") long ttlSeconds
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.ttlSeconds = ttlSeconds;
    }

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(BAD_REQUEST, "Username already exists");
        }

        UserEntity user = new UserEntity();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.USER);

        UserEntity saved = userRepository.save(user);
        return toResponse(saved);
    }

    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtUtil.generateToken(
                user.getUsername(),
                Map.of(
                        "userId", user.getId(),
                        "role", user.getRole().name(),
                        "username", user.getUsername()
                ),
                ttlSeconds
        );

        return new AuthResponse(token, toResponse(user));
    }

    public UserResponse getById(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        return toResponse(user);
    }

    private UserResponse toResponse(UserEntity user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRole());
    }
}
