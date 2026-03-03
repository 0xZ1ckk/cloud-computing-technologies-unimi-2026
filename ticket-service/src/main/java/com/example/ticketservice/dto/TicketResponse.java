package com.example.ticketservice.dto;

import com.example.ticketservice.model.TicketStatus;

import java.time.Instant;

public record TicketResponse(
        Long id,
        String title,
        String description,
        TicketStatus status,
        Long ownerId,
        Instant createdAt,
        Instant updatedAt,
        String instanceId
) {
}
