package com.automobileproject.eap.service;

import com.automobileproject.eap.dto.request.VehicleRequestDTO;
import com.automobileproject.eap.dto.response.VehicleResponseDTO;

import java.util.List;

public interface VehicleService {

    VehicleResponseDTO createVehicle(VehicleRequestDTO dto, String ownerEmail);

    List<VehicleResponseDTO> getVehiclesByOwner(String ownerEmail);
}
