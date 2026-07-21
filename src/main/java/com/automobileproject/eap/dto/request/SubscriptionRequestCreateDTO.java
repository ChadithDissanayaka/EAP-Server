package com.automobileproject.eap.dto.request;

import com.automobileproject.eap.entity.SUBSCRIPTION_PLAN_TYPES;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRequestCreateDTO {

    @NotNull(message = "Requested plan is required")
    private SUBSCRIPTION_PLAN_TYPES requestedPlan;

    private String message;
}
