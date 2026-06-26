package com.automobileproject.eap.event;

import com.automobileproject.eap.entity.NOTIFICATION_TYPES;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class AppointmentCreatedEvent extends ApplicationEvent {

    private final UUID appointmentId;
    private final String customerEmail;
    private final NOTIFICATION_TYPES notificationType;

    public AppointmentCreatedEvent(Object source, UUID appointmentId, String customerEmail) {
        super(source);
        this.appointmentId = appointmentId;
        this.customerEmail = customerEmail;
        this.notificationType = NOTIFICATION_TYPES.NEW_APPOINTMENT;
    }
}
