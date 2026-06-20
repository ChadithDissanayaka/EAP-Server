package com.automobileproject.eap.entity;

public enum NOTIFICATION_TYPES {
    // Employee notifications
    NEW_APPOINTMENT,
    QUOTE_APPROVED,
    QUOTE_REJECTED,

    // Customer notifications
    APPOINTMENT_ACCEPTED,
    APPOINTMENT_STATUS_CHANGED,
    QUOTE_SUBMITTED,
    APPOINTMENT_COMPLETED,

    // Admin notifications
    EMPLOYEE_ASSIGNED,
    SYSTEM_ALERT
}
