package com.automobileproject.eap.repo;

import com.automobileproject.eap.entity.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ServiceCategoryRepo extends JpaRepository<ServiceCategory, UUID> {

    Optional<ServiceCategory> findByName(String name);

    boolean existsByName(String name);
}
