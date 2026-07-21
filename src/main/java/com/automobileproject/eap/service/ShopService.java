package com.automobileproject.eap.service;

import com.automobileproject.eap.dto.request.ShopRegistrationRequestDTO;
import com.automobileproject.eap.dto.response.ShopResponseDTO;

import java.util.List;
import java.util.UUID;

public interface ShopService {

    ShopResponseDTO registerShop(ShopRegistrationRequestDTO dto);

    ShopResponseDTO getShopById(UUID shopId);

    ShopResponseDTO getShopBySlug(String slug);

    List<ShopResponseDTO> getAllShops();

    List<ShopResponseDTO> getShopsByStatus(String status);

    ShopResponseDTO approveShop(UUID shopId);

    ShopResponseDTO activateShop(UUID shopId);

    ShopResponseDTO deactivateShop(UUID shopId);

    ShopResponseDTO suspendShop(UUID shopId);

    ShopResponseDTO upgradePlan(UUID shopId, String plan);

    long countByStatus(String status);

    long countAll();

    ShopResponseDTO updateSlug(UUID shopId, String slug);
}
