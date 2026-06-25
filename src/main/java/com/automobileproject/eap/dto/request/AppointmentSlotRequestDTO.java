package com.automobileproject.eap.dto.request;

import com.automobileproject.eap.entity.SESSION_PERIOD_TYPES;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AppointmentSlotRequestDTO {

    @NotNull(message = "Session period is required")
    private SESSION_PERIOD_TYPES sessionPeriod;

    @NotNull(message = "Slot number is required")
    @Min(value = 1, message = "Slot number must be between 1 and 5")
    @Max(value = 5, message = "Slot number must be between 1 and 5")
    private Integer slotNumber;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;
}
