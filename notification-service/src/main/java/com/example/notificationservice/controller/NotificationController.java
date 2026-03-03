package com.example.notificationservice.controller;

import com.example.notificationservice.dto.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    @PostMapping
    public void notify(@RequestBody NotificationRequest request) {
        logger.info("Notification type={} ticketId={} message={}", request.type(), request.ticketId(), request.message());
    }
}
