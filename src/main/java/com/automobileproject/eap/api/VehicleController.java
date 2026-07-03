package com.automobileproject.eap.api;

import com.automobileproject.eap.config.OpenApiConfig;
import com.automobileproject.eap.dto.request.VehicleRequestDTO;
import com.automobileproject.eap.dto.response.VehicleResponseDTO;
import com.automobileproject.eap.service.VehicleService;
import com.automobileproject.eap.util.StandardResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicles", description = "Customer vehicle management")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class VehicleController {

    private final VehicleService vehicleService;

    @Operation(summary = "Add a new vehicle for the logged-in customer")
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<StandardResponseDTO> createVehicle(
            @Valid @RequestBody VehicleRequestDTO dto,
            Authentication authentication) {

        VehicleResponseDTO response = vehicleService.createVehicle(dto, authentication.getName());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(StandardResponseDTO.builder()
                        .code(201)
                        .message("Vehicle added successfully")
                        .data(response)
                        .build());
    }

    @Operation(summary = "Get all vehicles owned by the logged-in customer")
    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<StandardResponseDTO> getMyVehicles(Authentication authentication) {
        List<VehicleResponseDTO> vehicles = vehicleService.getVehiclesByOwner(authentication.getName());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponseDTO.builder()
                        .code(200)
                        .message("Vehicles retrieved successfully")
                        .data(vehicles)
                        .build());
    }
}
