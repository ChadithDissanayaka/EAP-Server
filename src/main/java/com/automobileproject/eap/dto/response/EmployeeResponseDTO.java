package com.automobileproject.eap.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeResponseDTO {

    private UUID id;
    private String firstName;
    private String lastName;
    private String role;
}