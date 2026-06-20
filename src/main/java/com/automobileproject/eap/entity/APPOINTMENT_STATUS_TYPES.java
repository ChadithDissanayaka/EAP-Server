package com.automobileproject.eap.entity;

public enum APPOINTMENT_STATUS_TYPES {
    // Standard statuses
    SCHEDULED,
    IN_PROGRESS,
    AWAITING_PARTS,
    COMPLETED,
    CANCELLED,

    // Modification project statuses
    QUOTE_REQUESTED,
    AWAITING_CUSTOMER_APPROVAL,
    REJECTED
}
