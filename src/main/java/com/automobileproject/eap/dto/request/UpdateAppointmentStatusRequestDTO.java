package com.automobileproject.eap.dto.request;

import com.automobileproject.eap.entity.APPOINTMENT_STATUS_TYPES;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UpdateAppointmentStatusRequestDTO {

    @NotNull(message = "Status is required")
    private APPOINTMENT_STATUS_TYPES status;
}
