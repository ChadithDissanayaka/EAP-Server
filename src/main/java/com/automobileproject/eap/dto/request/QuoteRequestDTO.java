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
public class QuoteRequestDTO {

    @NotNull(message = "Quote price is required")
    @Positive(message = "Quote price must be positive")
    private Double quotePrice;

    @NotBlank(message = "Quote details are required")
    private String quoteDetails;
}
