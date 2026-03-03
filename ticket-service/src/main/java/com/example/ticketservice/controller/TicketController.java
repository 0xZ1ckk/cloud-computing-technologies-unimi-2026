package com.example.ticketservice.controller;

import com.example.ticketservice.dto.CreateTicketRequest;
import com.example.ticketservice.dto.TicketResponse;
import com.example.ticketservice.dto.UpdateStatusRequest;
import com.example.ticketservice.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/tickets")
public class TicketController {
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public TicketResponse create(
            @RequestHeader(name = "X-User-Id", required = false) Long userId,
            @Valid @RequestBody CreateTicketRequest request
    ) {
        if (userId == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "Missing user context");
        }
        return ticketService.create(userId, request);
    }

    @GetMapping
    public List<TicketResponse> list(
            @RequestHeader(name = "X-User-Id", required = false) Long userId
    ) {
        if (userId == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "Missing user context");
        }
        return ticketService.listByOwner(userId);
    }

    @PutMapping("/{id}")
    public TicketResponse update(
            @PathVariable("id") Long ticketId,
            @Valid @RequestBody UpdateStatusRequest request
    ) {
        return ticketService.updateStatus(ticketId, request);
    }
}
