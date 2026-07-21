package com.automobileproject.eap.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRequestReviewDTO {

    @NotBlank(message = "Status must be APPROVED or REJECTED")
    private String status; // APPROVED or REJECTED

    private String adminNote;
}
