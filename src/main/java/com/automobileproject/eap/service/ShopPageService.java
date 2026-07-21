package com.automobileproject.eap.service;

import com.automobileproject.eap.dto.request.ShopPageRequestDTO;
import com.automobileproject.eap.dto.response.ShopPageResponseDTO;

import java.util.UUID;

public interface ShopPageService {

    ShopPageResponseDTO getPageByShopId(UUID shopId);

    ShopPageResponseDTO getPageByShopSlug(String slug);

    ShopPageResponseDTO updatePage(UUID shopId, ShopPageRequestDTO dto);

    ShopPageResponseDTO togglePublish(UUID shopId);
}
