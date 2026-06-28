package com.automobileproject.eap.dto.response;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TimeLogResponseDTO {

    private UUID id;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private String notes;
    private Long durationMinutes;
    private String formattedDuration;

    // Appointment context (flattened)
    private UUID appointmentId;
    private String serviceName;
    private String vehicleModel;
    private String vehicleNumber;

    // Employee info (flattened)
    private UUID employeeId;
    private String employeeFirstName;
    private String employeeLastName;
    private String employeeEmail;
}
