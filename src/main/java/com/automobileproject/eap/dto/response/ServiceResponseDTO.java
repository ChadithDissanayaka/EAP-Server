package com.automobileproject.eap.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceResponseDTO {

    private UUID id;
    private String name;
    private String description;
    private Double estimatedCost;
    private Integer estimatedDurationMinutes;
    private String imageUrl;

    // Category fields flattened
    private UUID categoryId;
    private String categoryName;
}
