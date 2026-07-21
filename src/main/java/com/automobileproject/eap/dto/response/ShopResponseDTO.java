package com.automobileproject.eap.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopResponseDTO {

    private UUID id;
    private String name;
    private String slug;
    private String description;
    private String address;
    private String phoneNumber;
    private String email;
    private String logoUrl;
    private String bannerUrl;
    private String primaryColor;
    private String subscriptionPlan;
    private String status;
    private String ownerName;
    private String ownerEmail;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
}
