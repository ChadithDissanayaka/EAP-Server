package com.automobileproject.eap.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopPageResponseDTO {

    private UUID id;
    private String heroTitle;
    private String heroSubtitle;
    private String aboutText;
    private String servicesTitle;
    private String facebookUrl;
    private String instagramUrl;
    private String whatsappNumber;
    private boolean published;
}
