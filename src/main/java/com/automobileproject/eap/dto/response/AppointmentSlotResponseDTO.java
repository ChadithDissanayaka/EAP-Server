package com.automobileproject.eap.dto.response;

import com.automobileproject.eap.entity.SESSION_PERIOD_TYPES;
import lombok.*;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AppointmentSlotResponseDTO {

    private UUID id;
    private SESSION_PERIOD_TYPES sessionPeriod;
    private Integer slotNumber;
    private LocalTime startTime;
    private LocalTime endTime;
    private String slotDescription;
    private Boolean isAvailable;
}
