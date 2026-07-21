package com.automobileproject.eap.repo;

import com.automobileproject.eap.entity.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceCategoryRepo extends JpaRepository<ServiceCategory, UUID> {

    Optional<ServiceCategory> findByName(String name);

    boolean existsByName(String name);

    List<ServiceCategory> findByShopId(UUID shopId);

    Optional<ServiceCategory> findByNameAndShopId(String name, UUID shopId);

    boolean existsByNameAndShopId(String name, UUID shopId);
}
