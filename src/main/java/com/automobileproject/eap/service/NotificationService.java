package com.automobileproject.eap.service;

import com.automobileproject.eap.entity.ROLE_TYPES;
import com.automobileproject.eap.entity.User;
import com.automobileproject.eap.event.AppointmentCreatedEvent;
import com.automobileproject.eap.event.WebSocketNotificationEvent;
import com.automobileproject.eap.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepo userRepo;

    // ── Event listener for new appointment creation ──────────────────────────

    @EventListener
    @Async("taskExecutor")
    public void handleAppointmentCreated(AppointmentCreatedEvent event) {
        log.info("New appointment created: {}. Notifying admins.", event.getAppointmentId());
        String msg = "New appointment booked by " + event.getCustomerEmail();
        sendToAdmins(event.getNotificationType().name(), event.getAppointmentId().toString(), msg);
    }

    // ── Targeted notification methods ────────────────────────────────────────

    /**
     * Notify a specific employee by email.
     */
    public void sendToEmployee(String employeeEmail, String type, String appointmentId, String message) {
        log.info("Queuing notification for employee {}. Type: {}", employeeEmail, type);
        eventPublisher.publishEvent(new WebSocketNotificationEvent(
                List.of(employeeEmail), "employee", type, appointmentId, message));
    }

    /**
     * Notify multiple specific employees by email.
     */
    public void sendToEmployees(List<String> employeeEmails, String type, String appointmentId, String message) {
        if (employeeEmails == null || employeeEmails.isEmpty()) return;
        log.info("Queuing notification for employees {}. Type: {}", employeeEmails, type);
        eventPublisher.publishEvent(new WebSocketNotificationEvent(
                employeeEmails, "employee", type, appointmentId, message));
    }

    /**
     * Notify all admins.
     */
    public void sendToAdmins(String type, String appointmentId, String message) {
        List<String> adminEmails = userRepo.findByRole(ROLE_TYPES.ADMIN)
                .stream()
                .map(User::getEmail)
                .collect(Collectors.toList());
        if (adminEmails.isEmpty()) {
            log.warn("No admin users found to notify.");
            return;
        }
        log.info("Queuing notification for admins {}. Type: {}", adminEmails, type);
        eventPublisher.publishEvent(new WebSocketNotificationEvent(
                adminEmails, "admin", type, appointmentId, message));
    }

    /**
     * Notify a specific customer by email.
     */
    public void sendToCustomer(String customerEmail, String type, String appointmentId, String message) {
        log.info("Queuing notification for customer {}. Type: {}", customerEmail, type);
        eventPublisher.publishEvent(new WebSocketNotificationEvent(
                List.of(customerEmail), "customer", type, appointmentId, message));
    }

    // ── Transactional event handler — delivers STOMP messages after commit ───

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Async("taskExecutor")
    public void handleWebSocketNotificationEvent(WebSocketNotificationEvent event) {
        Map<String, Object> payload = Map.of(
                "id", java.util.UUID.randomUUID().toString(),
                "type", event.getType(),
                "appointmentId", event.getAppointmentId(),
                "message", event.getMessage(),
                "timestamp", System.currentTimeMillis()
        );

        String channel = event.getChannel();
        for (String email : event.getRecipientEmails()) {
            String destination = "/topic/notifications/" + channel + "/" + email;
            log.info("Sending STOMP notification to {}. Type: {}", destination, event.getType());
            messagingTemplate.convertAndSend(destination, payload);
        }
    }
}
