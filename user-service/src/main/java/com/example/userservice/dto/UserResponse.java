package com.example.userservice.dto;

import com.example.userservice.model.UserRole;

public record UserResponse(Long id, String username, UserRole role) {
}
