package com.example.ticketservice.dto;

public record NotificationRequest(
        String type,
        Long ticketId,
        String message
) {
}
