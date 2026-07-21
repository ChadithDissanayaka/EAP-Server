package com.automobileproject.eap.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UpdateShopSlugRequestDTO {

    @NotBlank(message = "Portal link is required")
    @Size(min = 3, max = 60, message = "Portal link must be between 3 and 60 characters")
    @Pattern(
            regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
            message = "Portal link can only contain lowercase letters, numbers, and single hyphens between words"
    )
    private String slug;
}