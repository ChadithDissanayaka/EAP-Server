package com.automobileproject.eap.service.impl;

import com.automobileproject.eap.dto.request.ShopPageRequestDTO;
import com.automobileproject.eap.dto.response.ShopPageResponseDTO;
import com.automobileproject.eap.entity.ShopPage;
import com.automobileproject.eap.exception.EntryNotFoundException;
import com.automobileproject.eap.mapper.ShopPageMapper;
import com.automobileproject.eap.repo.ShopPageRepo;
import com.automobileproject.eap.service.ShopPageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShopPageServiceImpl implements ShopPageService {

    private final ShopPageRepo shopPageRepo;
    private final ShopPageMapper shopPageMapper;

    @Override
    public ShopPageResponseDTO getPageByShopId(UUID shopId) {
        ShopPage page = shopPageRepo.findByShopId(shopId)
                .orElseThrow(() -> new EntryNotFoundException("Shop page not found for shop ID: " + shopId));
        return shopPageMapper.toResponseDTO(page);
    }

    @Override
    public ShopPageResponseDTO getPageByShopSlug(String slug) {
        ShopPage page = shopPageRepo.findByShopSlug(slug)
                .orElseThrow(() -> new EntryNotFoundException("Shop page not found for slug: " + slug));
        return shopPageMapper.toResponseDTO(page);
    }

    @Override
    @Transactional
    public ShopPageResponseDTO updatePage(UUID shopId, ShopPageRequestDTO dto) {
        ShopPage page = shopPageRepo.findByShopId(shopId)
                .orElseThrow(() -> new EntryNotFoundException("Shop page not found for shop ID: " + shopId));

        shopPageMapper.updateEntity(page, dto);
        ShopPage saved = shopPageRepo.save(page);
        log.info("Shop page updated for shop ID: {}", shopId);
        return shopPageMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public ShopPageResponseDTO togglePublish(UUID shopId) {
        ShopPage page = shopPageRepo.findByShopId(shopId)
                .orElseThrow(() -> new EntryNotFoundException("Shop page not found for shop ID: " + shopId));

        page.setPublished(!page.isPublished());
        ShopPage saved = shopPageRepo.save(page);
        log.info("Shop page publish toggled to {} for shop ID: {}", saved.isPublished(), shopId);
        return shopPageMapper.toResponseDTO(saved);
    }
}
