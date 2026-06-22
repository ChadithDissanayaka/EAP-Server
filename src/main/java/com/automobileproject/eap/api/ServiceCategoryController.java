package com.automobileproject.eap.api;

import com.automobileproject.eap.config.OpenApiConfig;
import com.automobileproject.eap.dto.request.ServiceCategoryRequestDTO;
import com.automobileproject.eap.dto.response.ServiceCategoryResponseDTO;
import com.automobileproject.eap.service.ServiceCategoryService;
import com.automobileproject.eap.util.StandardResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/service-categories")
@RequiredArgsConstructor
@Tag(name = "Service Categories", description = "Manage service categories (Admin only for writes)")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class ServiceCategoryController {

    private final ServiceCategoryService serviceCategoryService;

    @Operation(summary = "Create a new service category (Admin only)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StandardResponseDTO> createCategory(@Valid @RequestBody ServiceCategoryRequestDTO dto) {
        ServiceCategoryResponseDTO response = serviceCategoryService.createCategory(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                StandardResponseDTO.builder()
                        .code(201)
                        .message("Service category created successfully")
                        .data(response)
                        .build()
        );
    }

    @Operation(summary = "Update a service category (Admin only)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StandardResponseDTO> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody ServiceCategoryRequestDTO dto) {
        ServiceCategoryResponseDTO response = serviceCategoryService.updateCategory(id, dto);
        return ResponseEntity.ok(
                StandardResponseDTO.builder()
                        .code(200)
                        .message("Service category updated successfully")
                        .data(response)
                        .build()
        );
    }

    @Operation(summary = "Delete a service category (Admin only)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StandardResponseDTO> deleteCategory(@PathVariable UUID id) {
        serviceCategoryService.deleteCategory(id);
        return ResponseEntity.ok(
                StandardResponseDTO.builder()
                        .code(200)
                        .message("Service category deleted successfully")
                        .data(null)
                        .build()
        );
    }

    @Operation(summary = "Get all service categories")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<StandardResponseDTO> getAllCategories() {
        List<ServiceCategoryResponseDTO> categories = serviceCategoryService.getAllCategories();
        return ResponseEntity.ok(
                StandardResponseDTO.builder()
                        .code(200)
                        .message("Service categories retrieved successfully")
                        .data(categories)
                        .build()
        );
    }

    @Operation(summary = "Get a service category by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<StandardResponseDTO> getCategoryById(@PathVariable UUID id) {
        ServiceCategoryResponseDTO response = serviceCategoryService.getCategoryById(id);
        return ResponseEntity.ok(
                StandardResponseDTO.builder()
                        .code(200)
                        .message("Service category retrieved successfully")
                        .data(response)
                        .build()
        );
    }
}
