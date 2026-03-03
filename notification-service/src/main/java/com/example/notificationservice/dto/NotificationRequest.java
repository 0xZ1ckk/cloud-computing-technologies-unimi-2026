package com.example.notificationservice.dto;

public record NotificationRequest(
        String type,
        Long ticketId,
        String message
) {
}
