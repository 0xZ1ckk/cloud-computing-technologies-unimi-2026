package com.example.userservice.dto;

public record AuthResponse(String token, UserResponse user) {
}
