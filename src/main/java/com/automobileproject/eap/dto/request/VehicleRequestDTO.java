package com.automobileproject.eap.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class VehicleRequestDTO {

    @NotBlank(message = "Model is required")
    private String model;

    @NotNull(message = "Year is required")
    @Positive(message = "Year must be a positive number")
    private Integer year;

    @NotBlank(message = "License plate is required")
    private String licensePlate;
}
