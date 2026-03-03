package com.example.ticketservice.repository;

import com.example.ticketservice.model.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<TicketEntity, Long> {
    List<TicketEntity> findByOwnerId(Long ownerId);
}
