package com.automobileproject.eap.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ModificationRequestDTO {

    @NotNull(message = "Vehicle ID is required")
    private UUID vehicleId;

    private OffsetDateTime appointmentDateTime;

    private String customerNotes;
}
