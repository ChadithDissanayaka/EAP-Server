package com.automobileproject.eap.dto.request;

import com.automobileproject.eap.entity.SESSION_PERIOD_TYPES;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SlotBasedAppointmentRequestDTO {

    @NotNull(message = "Vehicle ID is required")
    private UUID vehicleId;

    @NotNull(message = "At least one service ID is required")
    private List<UUID> serviceIds;

    @NotNull(message = "Appointment date is required")
    private LocalDate appointmentDate;

    @NotNull(message = "Session period is required")
    private SESSION_PERIOD_TYPES sessionPeriod;

    @NotNull(message = "Slot number is required")
    @Min(value = 1, message = "Slot number must be between 1 and 5")
    @Max(value = 5, message = "Slot number must be between 1 and 5")
    private Integer slotNumber;

    private String customerNotes;
}
