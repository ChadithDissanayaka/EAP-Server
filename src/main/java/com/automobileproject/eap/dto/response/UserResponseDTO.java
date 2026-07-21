package com.automobileproject.eap.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponseDTO {

    private UUID id;
    private String username;
    private String email;
    private String role;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Boolean emailVerified;
    private Boolean isActive;
    private String message;
}
