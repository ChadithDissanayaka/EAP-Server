package com.automobileproject.eap.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleResponseDTO {

    private UUID id;
    private String model;
    private Integer year;
    private String licensePlate;

    // Owner fields flattened — never expose the full User entity
    private UUID ownerId;
    private String ownerFirstName;
    private String ownerLastName;
    private String ownerEmail;
}
