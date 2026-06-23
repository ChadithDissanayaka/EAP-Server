package com.automobileproject.eap.mapper;

import com.automobileproject.eap.dto.request.VehicleRequestDTO;
import com.automobileproject.eap.dto.response.VehicleResponseDTO;
import com.automobileproject.eap.entity.User;
import com.automobileproject.eap.entity.Vehicle;
import com.automobileproject.eap.exception.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {

    /**
     * Converts a VehicleRequestDTO + resolved owner into a Vehicle entity.
     * The owner is resolved in the service layer before calling this method.
     */
    public Vehicle toEntity(VehicleRequestDTO dto, User owner) {
        if (dto == null) {
            throw new ValidationException("VehicleRequestDTO must not be null");
        }
        if (owner == null) {
            throw new ValidationException("Owner must not be null");
        }
        return Vehicle.builder()
                .model(dto.getModel())
                .year(dto.getYear())
                .licensePlate(dto.getLicensePlate())
                .owner(owner)
                .build();
    }

    public VehicleResponseDTO toResponseDTO(Vehicle vehicle) {
        if (vehicle == null) {
            throw new ValidationException("Vehicle entity must not be null");
        }
        return VehicleResponseDTO.builder()
                .id(vehicle.getId())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .licensePlate(vehicle.getLicensePlate())
                .ownerId(vehicle.getOwner().getId())
                .ownerFirstName(vehicle.getOwner().getFirstName())
                .ownerLastName(vehicle.getOwner().getLastName())
                .ownerEmail(vehicle.getOwner().getEmail())
                .build();
    }
}
