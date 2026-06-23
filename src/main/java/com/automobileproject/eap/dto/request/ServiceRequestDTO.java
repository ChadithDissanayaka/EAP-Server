package com.automobileproject.eap.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ServiceRequestDTO {

    @NotBlank(message = "Service name is required")
    private String name;

    private String description;

    @Positive(message = "Estimated cost must be positive")
    private Double estimatedCost;

    @NotNull(message = "Estimated duration is required")
    @Positive(message = "Estimated duration must be positive")
    private Integer estimatedDurationMinutes;

    @NotNull(message = "Category ID is required")
    private UUID categoryId;
}
