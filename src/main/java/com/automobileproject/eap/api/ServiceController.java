package com.automobileproject.eap.api;

import com.automobileproject.eap.config.OpenApiConfig;
import com.automobileproject.eap.dto.request.ServiceRequestDTO;
import com.automobileproject.eap.dto.response.ServiceResponseDTO;
import com.automobileproject.eap.service.AutoService;
import com.automobileproject.eap.util.StandardResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Services", description = "Garage service management with Cloudinary image upload")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class ServiceController {

        private final AutoService autoService;
        private final ObjectMapper objectMapper;

        @Operation(summary = "Create a new service with optional image (Admin only)")
        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasRole('SHOP_OWNER')")
        public ResponseEntity<StandardResponseDTO> createService(
                        @RequestParam("service") String serviceDtoJson,
                        @RequestParam(value = "image", required = false) MultipartFile imageFile) throws IOException {

                ServiceRequestDTO dto = objectMapper.readValue(serviceDtoJson, ServiceRequestDTO.class);
                ServiceResponseDTO response = autoService.createService(dto, imageFile);
                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(StandardResponseDTO.builder()
                                                .code(201)
                                                .message("Service created successfully")
                                                .data(response)
                                                .build());
        }

        @Operation(summary = "Update a service with optional new image (Admin only)")
        @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasRole('SHOP_OWNER')")
        public ResponseEntity<StandardResponseDTO> updateService(
                        @PathVariable UUID id,
                        @RequestParam("service") String serviceDtoJson,
                        @RequestParam(value = "image", required = false) MultipartFile imageFile) throws IOException {

                ServiceRequestDTO dto = objectMapper.readValue(serviceDtoJson, ServiceRequestDTO.class);
                ServiceResponseDTO response = autoService.updateService(id, dto, imageFile);
                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(StandardResponseDTO.builder()
                                                .code(200)
                                                .message("Service updated successfully")
                                                .data(response)
                                                .build());
        }

        @Operation(summary = "Delete a service and its image (Admin only)")
        @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('SHOP_OWNER')")
        public ResponseEntity<StandardResponseDTO> deleteService(@PathVariable UUID id) throws IOException {
                autoService.deleteService(id);
                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(StandardResponseDTO.builder()
                                                .code(200)
                                                .message("Service deleted successfully")
                                                .data(null)
                                                .build());
        }

        @Operation(summary = "Get all services")
        @GetMapping
        @PreAuthorize("hasAnyRole('CUSTOMER', 'SHOP_OWNER')")
        public ResponseEntity<StandardResponseDTO> getAllServices() {
                List<ServiceResponseDTO> services = autoService.getAllServices();
                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(StandardResponseDTO.builder()
                                                .code(200)
                                                .message("Services retrieved successfully")
                                                .data(services)
                                                .build());
        }

        @Operation(summary = "Get services by category")
        @GetMapping("/category/{categoryId}")
        @PreAuthorize("hasAnyRole('CUSTOMER', 'SHOP_OWNER')")
        public ResponseEntity<StandardResponseDTO> getServicesByCategory(@PathVariable UUID categoryId) {
                List<ServiceResponseDTO> services = autoService.getServicesByCategory(categoryId);
                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(StandardResponseDTO.builder()
                                                .code(200)
                                                .message("Services retrieved successfully")
                                                .data(services)
                                                .build());
        }

        @Operation(summary = "Get a service by ID")
        @GetMapping("/{id}")
        @PreAuthorize("hasAnyRole('CUSTOMER', 'SHOP_OWNER')")
        public ResponseEntity<StandardResponseDTO> getServiceById(@PathVariable UUID id) {
                ServiceResponseDTO response = autoService.getServiceById(id);
                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(StandardResponseDTO.builder()
                                                .code(200)
                                                .message("Service retrieved successfully")
                                                .data(response)
                                                .build());
        }
}