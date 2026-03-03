package com.example.ticketservice.dto;

import com.example.ticketservice.model.TicketStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @NotNull TicketStatus status
) {
}
