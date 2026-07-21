package com.automobileproject.eap.mapper;

import com.automobileproject.eap.dto.response.ShopResponseDTO;
import com.automobileproject.eap.entity.Shop;
import com.automobileproject.eap.exception.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class ShopMapper {

    public ShopResponseDTO toResponseDTO(Shop shop) {
        if (shop == null) {
            throw new ValidationException("Shop entity must not be null");
        }
        return ShopResponseDTO.builder()
                .id(shop.getId())
                .name(shop.getName())
                .slug(shop.getSlug())
                .description(shop.getDescription())
                .address(shop.getAddress())
                .phoneNumber(shop.getPhoneNumber())
                .email(shop.getEmail())
                .logoUrl(shop.getLogoUrl())
                .bannerUrl(shop.getBannerUrl())
                .primaryColor(shop.getPrimaryColor())
                .subscriptionPlan(shop.getSubscriptionPlan().name())
                .status(shop.getStatus().name())
                .ownerName(shop.getOwner().getFirstName() + " " + shop.getOwner().getLastName())
                .ownerEmail(shop.getOwner().getEmail())
                .createdAt(shop.getCreatedAt())
                .approvedAt(shop.getApprovedAt())
                .build();
    }
}
