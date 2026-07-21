package com.automobileproject.eap.mapper;

import com.automobileproject.eap.dto.request.ShopPageRequestDTO;
import com.automobileproject.eap.dto.response.ShopPageResponseDTO;
import com.automobileproject.eap.entity.ShopPage;
import com.automobileproject.eap.exception.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class ShopPageMapper {

    public ShopPageResponseDTO toResponseDTO(ShopPage page) {
        if (page == null) {
            throw new ValidationException("ShopPage entity must not be null");
        }
        return ShopPageResponseDTO.builder()
                .id(page.getId())
                .heroTitle(page.getHeroTitle())
                .heroSubtitle(page.getHeroSubtitle())
                .aboutText(page.getAboutText())
                .servicesTitle(page.getServicesTitle())
                .facebookUrl(page.getFacebookUrl())
                .instagramUrl(page.getInstagramUrl())
                .whatsappNumber(page.getWhatsappNumber())
                .published(page.isPublished())
                .build();
    }

    public void updateEntity(ShopPage page, ShopPageRequestDTO dto) {
        if (dto.getHeroTitle() != null) page.setHeroTitle(dto.getHeroTitle());
        if (dto.getHeroSubtitle() != null) page.setHeroSubtitle(dto.getHeroSubtitle());
        if (dto.getAboutText() != null) page.setAboutText(dto.getAboutText());
        if (dto.getServicesTitle() != null) page.setServicesTitle(dto.getServicesTitle());
        if (dto.getFacebookUrl() != null) page.setFacebookUrl(dto.getFacebookUrl());
        if (dto.getInstagramUrl() != null) page.setInstagramUrl(dto.getInstagramUrl());
        if (dto.getWhatsappNumber() != null) page.setWhatsappNumber(dto.getWhatsappNumber());
        if (dto.getPublished() != null) page.setPublished(dto.getPublished());
    }
}
