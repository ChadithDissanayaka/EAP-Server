package com.automobileproject.eap.service.impl;

import com.automobileproject.eap.dto.request.VehicleRequestDTO;
import com.automobileproject.eap.dto.response.VehicleResponseDTO;
import com.automobileproject.eap.entity.User;
import com.automobileproject.eap.entity.Vehicle;
import com.automobileproject.eap.exception.DuplicateEntryException;
import com.automobileproject.eap.exception.EntryNotFoundException;
import com.automobileproject.eap.mapper.VehicleMapper;
import com.automobileproject.eap.repo.UserRepo;
import com.automobileproject.eap.repo.VehicleRepo;
import com.automobileproject.eap.service.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepo vehicleRepo;
    private final UserRepo userRepo;
    private final VehicleMapper vehicleMapper;

    @Override
    public VehicleResponseDTO createVehicle(VehicleRequestDTO dto, String ownerEmail) {
        User owner = userRepo.findByEmail(ownerEmail)
                .orElseThrow(() -> new EntryNotFoundException("User not found with email: " + ownerEmail));

        // Check for duplicate license plate
        vehicleRepo.findAll().stream()
                .filter(v -> v.getLicensePlate().equalsIgnoreCase(dto.getLicensePlate()))
                .findFirst()
                .ifPresent(v -> {
                    throw new DuplicateEntryException(
                            "A vehicle with license plate '" + dto.getLicensePlate() + "' already exists");
                });

        Vehicle vehicle = vehicleMapper.toEntity(dto, owner);
        Vehicle saved = vehicleRepo.save(vehicle);
        log.info("Vehicle created with ID: {} for owner: {}", saved.getId(), ownerEmail);
        return vehicleMapper.toResponseDTO(saved);
    }

    @Override
    public List<VehicleResponseDTO> getVehiclesByOwner(String ownerEmail) {
        User owner = userRepo.findByEmail(ownerEmail)
                .orElseThrow(() -> new EntryNotFoundException("User not found with email: " + ownerEmail));

        return vehicleRepo.findByOwnerId(owner.getId())
                .stream()
                .map(vehicleMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}
