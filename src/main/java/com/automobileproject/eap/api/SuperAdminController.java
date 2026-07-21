package com.automobileproject.eap.api;

import com.automobileproject.eap.dto.response.ShopResponseDTO;
import com.automobileproject.eap.service.ShopService;
import com.automobileproject.eap.util.StandardResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/super-admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
@Tag(name = "Super Admin", description = "Platform-level shop management (Super Admin only)")
public class SuperAdminController {

    private final ShopService shopService;

    @Operation(summary = "Get all shops")
    @GetMapping("/shops")
    public ResponseEntity<StandardResponseDTO> getAllShops(
            @RequestParam(required = false) String status) {
        List<ShopResponseDTO> shops = (status != null && !status.isBlank())
                ? shopService.getShopsByStatus(status)
                : shopService.getAllShops();
        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200)
                .message("Shops retrieved successfully")
                .data(shops)
                .build());
    }

    @Operation(summary = "Get shop by ID")
    @GetMapping("/shops/{id}")
    public ResponseEntity<StandardResponseDTO> getShopById(@PathVariable UUID id) {
        ShopResponseDTO shop = shopService.getShopById(id);
        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200)
                .message("Shop retrieved successfully")
                .data(shop)
                .build());
    }

    @Operation(summary = "Approve a pending shop")
    @PutMapping("/shops/{id}/approve")
    public ResponseEntity<StandardResponseDTO> approveShop(@PathVariable UUID id) {
        ShopResponseDTO shop = shopService.approveShop(id);
        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200)
                .message("Shop approved and activated successfully")
                .data(shop)
                .build());
    }

    @Operation(summary = "Activate a shop")
    @PutMapping("/shops/{id}/activate")
    public ResponseEntity<StandardResponseDTO> activateShop(@PathVariable UUID id) {
        ShopResponseDTO shop = shopService.activateShop(id);
        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200)
                .message("Shop activated successfully")
                .data(shop)
                .build());
    }

    @Operation(summary = "Deactivate a shop")
    @PutMapping("/shops/{id}/deactivate")
    public ResponseEntity<StandardResponseDTO> deactivateShop(@PathVariable UUID id) {
        ShopResponseDTO shop = shopService.deactivateShop(id);
        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200)
                .message("Shop deactivated successfully")
                .data(shop)
                .build());
    }

    @Operation(summary = "Suspend a shop")
    @PutMapping("/shops/{id}/suspend")
    public ResponseEntity<StandardResponseDTO> suspendShop(@PathVariable UUID id) {
        ShopResponseDTO shop = shopService.suspendShop(id);
        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200)
                .message("Shop suspended successfully")
                .data(shop)
                .build());
    }

    @Operation(summary = "Upgrade shop subscription plan")
    @PutMapping("/shops/{id}/upgrade-plan")
    public ResponseEntity<StandardResponseDTO> upgradePlan(
            @PathVariable UUID id, @RequestParam String plan) {
        ShopResponseDTO shop = shopService.upgradePlan(id, plan);
        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200)
                .message("Shop plan upgraded successfully")
                .data(shop)
                .build());
    }

    @Operation(summary = "Get dashboard statistics")
    @GetMapping("/stats")
    public ResponseEntity<StandardResponseDTO> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalShops", shopService.countAll());
        stats.put("pendingShops", shopService.countByStatus("PENDING_APPROVAL"));
        stats.put("activeShops", shopService.countByStatus("ACTIVE"));
        stats.put("suspendedShops", shopService.countByStatus("SUSPENDED"));
        stats.put("deactivatedShops", shopService.countByStatus("DEACTIVATED"));
        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200)
                .message("Stats retrieved successfully")
                .data(stats)
                .build());
    }
}
