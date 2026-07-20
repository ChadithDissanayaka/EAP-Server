package com.automobileproject.eap.event;

import lombok.Getter;

import java.util.List;

@Getter
public class WebSocketNotificationEvent {
    private final List<String> recipientEmails;
    private final String channel; // "admin", "employee", or "customer"
    private final String type;
    private final String appointmentId;
    private final String message;

    public WebSocketNotificationEvent(List<String> recipientEmails, String channel, String type, String appointmentId, String message) {
        this.recipientEmails = recipientEmails;
        this.channel = channel;
        this.type = type;
        this.appointmentId = appointmentId;
        this.message = message;
    }
}
