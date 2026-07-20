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
        log.info("Sending WebSocket notification for new appointment: {}", event.getAppointmentId());
        String msg = "New appointment booked by " + event.getCustomerEmail();
        sendEmployeeNotification(event.getNotificationType().name(), event.getAppointmentId().toString(), msg);
    }

    public void sendEmployeeNotification(String type, String appointmentId, String message) {
        log.info("Sending WebSocket notification to employees. Type: {}, Msg: {}", type, message);
        Map<String, Object> notification = Map.of(
                "id", java.util.UUID.randomUUID().toString(),
                "type", type,
                "appointmentId", appointmentId,
                "message", message,
                "timestamp", System.currentTimeMillis()
        );
        messagingTemplate.convertAndSend("/topic/notifications/employee", notification);
    }

    public void sendCustomerNotification(String recipientEmail, String type, String appointmentId, String message) {
        log.info("Sending WebSocket notification to customer {}. Type: {}, Msg: {}", recipientEmail, type, message);
        Map<String, Object> notification = Map.of(
                "id", java.util.UUID.randomUUID().toString(),
                "type", type,
                "appointmentId", appointmentId,
                "message", message,
                "timestamp", System.currentTimeMillis()
        );
        messagingTemplate.convertAndSend("/topic/notifications/customer/" + recipientEmail, notification);
    }
}
