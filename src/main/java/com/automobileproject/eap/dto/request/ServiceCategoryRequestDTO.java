package com.automobileproject.eap.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ServiceCategoryRequestDTO {

    @NotBlank(message = "Category name is required")
    private String name;

    private String description;
}
