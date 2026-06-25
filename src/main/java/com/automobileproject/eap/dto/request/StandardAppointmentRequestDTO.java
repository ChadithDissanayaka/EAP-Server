package com.automobileproject.eap.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class StandardAppointmentRequestDTO {

    @NotNull(message = "Vehicle ID is required")
    private UUID vehicleId;

    // Supports multiple services
    private List<UUID> serviceIds;

    // Backward compatibility: single service
    private UUID serviceId;

    private OffsetDateTime appointmentDateTime;

    private String customerNotes;
}
