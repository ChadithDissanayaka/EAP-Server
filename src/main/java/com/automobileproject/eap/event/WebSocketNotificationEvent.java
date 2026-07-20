package com.automobileproject.eap.event;

import lombok.Getter;

@Getter
public class WebSocketNotificationEvent {
    private final String recipientEmail;
    private final String type;
    private final String appointmentId;
    private final String message;

    public WebSocketNotificationEvent(String recipientEmail, String type, String appointmentId, String message) {
        this.recipientEmail = recipientEmail;
        this.type = type;
        this.appointmentId = appointmentId;
        this.message = message;
    }
}
