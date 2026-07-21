package com.automobileproject.eap.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ShopPageRequestDTO {

    @Size(max = 200, message = "Hero title must not exceed 200 characters")
    private String heroTitle;

    @Size(max = 500, message = "Hero subtitle must not exceed 500 characters")
    private String heroSubtitle;

    private String aboutText;

    @Size(max = 200, message = "Services title must not exceed 200 characters")
    private String servicesTitle;

    @Size(max = 500, message = "Facebook URL must not exceed 500 characters")
    private String facebookUrl;

    @Size(max = 500, message = "Instagram URL must not exceed 500 characters")
    private String instagramUrl;

    @Size(max = 20, message = "WhatsApp number must not exceed 20 characters")
    private String whatsappNumber;

    private Boolean published;
}
