package com.automobileproject.eap.service;

import com.automobileproject.eap.event.AppointmentCreatedEvent;
import com.automobileproject.eap.event.WebSocketNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    @Async("taskExecutor")
    public void handleAppointmentCreated(AppointmentCreatedEvent event) {
        log.info("Sending WebSocket notification for new appointment: {}", event.getAppointmentId());
        String msg = "New appointment booked by " + event.getCustomerEmail();
        sendEmployeeNotification(event.getNotificationType().name(), event.getAppointmentId().toString(), msg);
    }

    public void sendEmployeeNotification(String type, String appointmentId, String message) {
        log.info("Queuing employee WebSocket notification event. Type: {}", type);
        eventPublisher.publishEvent(new WebSocketNotificationEvent(null, type, appointmentId, message));
    }

    public void sendCustomerNotification(String recipientEmail, String type, String appointmentId, String message) {
        log.info("Queuing customer WebSocket notification event for {}. Type: {}", recipientEmail, type);
        eventPublisher.publishEvent(new WebSocketNotificationEvent(recipientEmail, type, appointmentId, message));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Async("taskExecutor")
    public void handleWebSocketNotificationEvent(WebSocketNotificationEvent event) {
        if (event.getRecipientEmail() == null) {
            log.info("Sending committed WebSocket notification to employees. Type: {}, Msg: {}", event.getType(), event.getMessage());
            Map<String, Object> notification = Map.of(
                    "id", java.util.UUID.randomUUID().toString(),
                    "type", event.getType(),
                    "appointmentId", event.getAppointmentId(),
                    "message", event.getMessage(),
                    "timestamp", System.currentTimeMillis()
            );
            messagingTemplate.convertAndSend("/topic/notifications/employee", notification);
        } else {
            log.info("Sending committed WebSocket notification to customer {}. Type: {}, Msg: {}", event.getRecipientEmail(), event.getType(), event.getMessage());
            Map<String, Object> notification = Map.of(
                    "id", java.util.UUID.randomUUID().toString(),
                    "type", event.getType(),
                    "appointmentId", event.getAppointmentId(),
                    "message", event.getMessage(),
                    "timestamp", System.currentTimeMillis()
            );
            messagingTemplate.convertAndSend("/topic/notifications/customer/" + event.getRecipientEmail(), notification);
        }
    }
}
