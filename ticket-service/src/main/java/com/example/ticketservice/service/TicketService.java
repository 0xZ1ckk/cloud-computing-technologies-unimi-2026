package com.example.ticketservice.service;

import com.example.ticketservice.dto.CreateTicketRequest;
import com.example.ticketservice.dto.NotificationRequest;
import com.example.ticketservice.dto.TicketResponse;
import com.example.ticketservice.dto.UpdateStatusRequest;
import com.example.ticketservice.model.TicketEntity;
import com.example.ticketservice.model.TicketStatus;
import com.example.ticketservice.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final RestTemplate restTemplate;
    private final String notificationUrl;
    private final String instanceId;

    public TicketService(
            TicketRepository ticketRepository,
            RestTemplate restTemplate,
            @Value("${app.notification-url}") String notificationUrl,
            @Value("${app.instance-id}") String instanceId
    ) {
        this.ticketRepository = ticketRepository;
        this.restTemplate = restTemplate;
        this.notificationUrl = notificationUrl;
        this.instanceId = instanceId;
    }

    public TicketResponse create(Long ownerId, CreateTicketRequest request) {
        TicketEntity ticket = new TicketEntity();
        ticket.setTitle(request.title());
        ticket.setDescription(request.description());
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setOwnerId(ownerId);
        ticket.setCreatedAt(Instant.now());
        ticket.setUpdatedAt(Instant.now());

        TicketEntity saved = ticketRepository.save(ticket);
        sendNotification(new NotificationRequest(
                "TICKET_CREATED",
                saved.getId(),
                "Ticket created by user " + ownerId
        ));
        return toResponse(saved);
    }

    public List<TicketResponse> listByOwner(Long ownerId) {
        return ticketRepository.findByOwnerId(ownerId).stream()
                .map(this::toResponse)
                .toList();
    }

    public TicketResponse updateStatus(Long ticketId, UpdateStatusRequest request) {
        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Ticket not found"));

        ticket.setStatus(request.status());
        ticket.setUpdatedAt(Instant.now());
        TicketEntity saved = ticketRepository.save(ticket);

        sendNotification(new NotificationRequest(
                "TICKET_UPDATED",
                saved.getId(),
                "Ticket updated to " + saved.getStatus().name()
        ));

        return toResponse(saved);
    }

    private void sendNotification(NotificationRequest request) {
        try {
            restTemplate.postForEntity(notificationUrl, request, Void.class);
        } catch (Exception ignored) {
        }
    }

    private TicketResponse toResponse(TicketEntity ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getOwnerId(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                instanceId
        );
    }
}
