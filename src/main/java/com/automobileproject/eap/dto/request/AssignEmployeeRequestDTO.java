package com.automobileproject.eap.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AssignEmployeeRequestDTO {

    @NotNull(message = "Employee ID is required")
    private UUID employeeId;
}
