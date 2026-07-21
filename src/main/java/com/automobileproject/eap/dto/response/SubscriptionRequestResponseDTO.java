package com.automobileproject.eap.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRequestResponseDTO {

    private UUID id;
    private UUID shopId;
    private String shopName;
    private UUID requestedById;
    private String requestedByName;
    private String requestedByEmail;
    private String currentPlan;
    private String requestedPlan;
    private String status;
    private String message;
    private String adminNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
