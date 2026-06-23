package com.automobileproject.eap.repo;

import com.automobileproject.eap.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleRepo extends JpaRepository<Vehicle, UUID> {

    List<Vehicle> findByOwnerId(UUID ownerId);

    Optional<Vehicle> findByLicensePlate(String licensePlate);
}