package com.automobileproject.eap.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TimeLogRequestDTO {

    @NotNull(message = "Start time is required")
    private OffsetDateTime startTime;

    private OffsetDateTime endTime;

    private String notes;
}
