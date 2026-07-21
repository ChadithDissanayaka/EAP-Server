package com.automobileproject.eap.repo;

import com.automobileproject.eap.entity.SHOP_STATUS_TYPES;
import com.automobileproject.eap.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShopRepo extends JpaRepository<Shop, UUID> {

    Optional<Shop> findBySlug(String slug);

    Optional<Shop> findByOwnerId(UUID ownerId);

    List<Shop> findByStatus(SHOP_STATUS_TYPES status);

    boolean existsBySlug(String slug);

    boolean existsByName(String name);
}
