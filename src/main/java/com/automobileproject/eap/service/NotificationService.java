package com.automobileproject.eap.service;

import com.automobileproject.eap.event.AppointmentCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    @Async("taskExecutor")
    public void handleAppointmentCreated(AppointmentCreatedEvent event) {
        log.info("Sending WebSocket notification for appointment: {}", event.getAppointmentId());

        Map<String, Object> notification = Map.of(
                "type", event.getNotificationType().name(),
                "appointmentId", event.getAppointmentId().toString(),
                "customerEmail", event.getCustomerEmail(),
                "message", "New appointment booked by " + event.getCustomerEmail()
        );

        messagingTemplate.convertAndSend("/topic/notifications/employee", notification);
        log.info("WebSocket notification sent for appointment: {}", event.getAppointmentId());
    }
}
