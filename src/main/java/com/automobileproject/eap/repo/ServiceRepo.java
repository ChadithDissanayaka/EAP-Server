package com.automobileproject.eap.repo;

import com.automobileproject.eap.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

import java.util.Optional;

public interface ServiceRepo extends JpaRepository<Service, UUID> {

    List<Service> findByCategoryId(UUID categoryId);

    boolean existsByName(String name);

    Optional<Service> findByName(String name);
}
