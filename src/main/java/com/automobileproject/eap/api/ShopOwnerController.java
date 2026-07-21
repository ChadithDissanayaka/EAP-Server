package com.automobileproject.eap.api;

import com.automobileproject.eap.dto.request.EmployeeCreateRequestDTO;
import com.automobileproject.eap.dto.request.ShopPageRequestDTO;
import com.automobileproject.eap.dto.request.UpdateShopSlugRequestDTO;
import com.automobileproject.eap.dto.response.ShopPageResponseDTO;
import com.automobileproject.eap.dto.response.ShopResponseDTO;
import com.automobileproject.eap.dto.response.UserResponseDTO;
import com.automobileproject.eap.entity.User;
import com.automobileproject.eap.exception.ValidationException;
import com.automobileproject.eap.repo.UserRepo;
import com.automobileproject.eap.service.ShopPageService;
import com.automobileproject.eap.service.ShopService;
import com.automobileproject.eap.service.UserService;
import com.automobileproject.eap.util.StandardResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import com.automobileproject.eap.dto.request.SubscriptionRequestCreateDTO;
import com.automobileproject.eap.dto.response.SubscriptionRequestResponseDTO;
import com.automobileproject.eap.service.SubscriptionRequestService;

@RestController
@RequestMapping("/shop-owner")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SHOP_OWNER')")
@Tag(name = "Shop Owner", description = "Shop owner management — employees, customers, shop page, subscriptions")
public class ShopOwnerController {

    private final UserService userService;
    private final ShopService shopService;
    private final ShopPageService shopPageService;
    private final SubscriptionRequestService subscriptionRequestService;
    private final UserRepo userRepo;

    // ── Employee Management ─────────────────────────────────────────────────

    @Operation(summary = "Create an employee for this shop")
    @PostMapping("/employees")
    public ResponseEntity<StandardResponseDTO> createEmployee(
            @Valid @RequestBody EmployeeCreateRequestDTO dto, Authentication auth) {
        UUID shopId = extractShopId(auth);
        UserResponseDTO employee = userService.createEmployee(shopId, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(StandardResponseDTO.builder()
                        .code(201)
                        .message("Employee created successfully")
                        .data(employee)
                        .build());
    }

    @Operation(summary = "List employees for this shop")
    @GetMapping("/employees")
    public ResponseEntity<StandardResponseDTO> getEmployees(Authentication auth) {
        UUID shopId = extractShopId(auth);
        List<UserResponseDTO> employees = userService.getShopEmployees(shopId);
        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200)
                .message("Employees retrieved successfully")
                .data(employees)
                .build());
    }

    @Operation(summary = "Toggle employee active status")
    @PutMapping("/employees/{id}/toggle-status")
    public ResponseEntity<StandardResponseDTO> toggleEmployeeStatus(
            @PathVariable UUID id, Authentication auth) {
        UUID shopId = extractShopId(auth);
        userService.toggleUserStatus(id, shopId);
        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200)
                .message("Employee status toggled successfully")
                .data(null)
                .build());
    }

    // ── Customer Management ─────────────────────────────────────────────────

    @Operation(summary = "List customers for this shop")
    @GetMapping("/customers")
    public ResponseEntity<StandardResponseDTO> getCustomers(Authentication auth) {
        UUID shopId = extractShopId(auth);
        List<UserResponseDTO> customers = userService.getShopCustomers(shopId);
        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200)
                .message("Customers retrieved successfully")
                .data(customers)
                .build());
    }

    @Operation(summary = "Toggle customer active status")
    @PutMapping("/customers/{id}/toggle-status")
    public ResponseEntity<StandardResponseDTO> toggleCustomerStatus(
            @PathVariable UUID id, Authentication auth) {
        UUID shopId = extractShopId(auth);
        userService.toggleUserStatus(id, shopId);
        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200)
                .message("Customer status toggled successfully")
                .data(null)
                .build());
    }

    // ── Shop Details ────────────────────────────────────────────────────────

    @Operation(summary = "Get own shop details")
    @GetMapping("/shop")
    public ResponseEntity<StandardResponseDTO> getShop(Authentication auth) {
        UUID shopId = extractShopId(auth);
        ShopResponseDTO shop = shopService.getShopById(shopId);
        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200)
                .message("Shop retrieved successfully")
                .data(shop)
                .build());
    }

    // ── Shop Page Builder ───────────────────────────────────────────────────

    @Operation(summary = "Get shop page configuration")
    @GetMapping("/page")
    public ResponseEntity<StandardResponseDTO> getPage(Authentication auth) {
        UUID shopId = extractShopId(auth);
        ShopPageResponseDTO page = shopPageService.getPageByShopId(shopId);
        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200)
                .message("Shop page retrieved successfully")
                .data(page)
                .build());
    }

    @Operation(summary = "Update shop page configuration")
    @PutMapping("/page")
    public ResponseEntity<StandardResponseDTO> updatePage(
            @Valid @RequestBody ShopPageRequestDTO dto, Authentication auth) {
        UUID shopId = extractShopId(auth);
        ShopPageResponseDTO page = shopPageService.updatePage(shopId, dto);
        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200)
                .message("Shop page updated successfully")
                .data(page)
                .build());
    }

    @Operation(summary = "Toggle shop page publish status")
    @PutMapping("/page/publish")
    public ResponseEntity<StandardResponseDTO> togglePublish(Authentication auth) {
        UUID shopId = extractShopId(auth);
        ShopPageResponseDTO page = shopPageService.togglePublish(shopId);
        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200)
                .message("Shop page publish status toggled")
                .data(page)
                .build());
    }

    // ── Helper ──────────────────────────────────────────────────────────────

    private UUID extractShopId(Authentication auth) {
        String email = auth.getName();
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ValidationException("Authenticated user not found"));
        if (user.getShop() == null) {
            throw new ValidationException("User is not associated with any shop");
        }
        return user.getShop().getId();
    }

    @Operation(summary = "Update own shop's portal URL link (slug)")
    @PutMapping("/shop/slug")
    public ResponseEntity<StandardResponseDTO> updateShopSlug(
            @Valid @RequestBody UpdateShopSlugRequestDTO dto, Authentication auth) {
        UUID shopId = extractShopId(auth);
        ShopResponseDTO shop = shopService.updateSlug(shopId, dto.getSlug());
        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200)
                .message("Portal link updated successfully")
                .data(shop)
                .build());
    }

    // ── Subscription Request & Communication ────────────────────────────────

    @Operation(summary = "Request subscription plan change / communication with SaaS owner")
    @PostMapping("/subscription-requests")
    public ResponseEntity<StandardResponseDTO> createSubscriptionRequest(
            @Valid @RequestBody SubscriptionRequestCreateDTO dto, Authentication auth) {
        String email = auth.getName();
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ValidationException("Authenticated user not found"));
        if (user.getShop() == null) {
            throw new ValidationException("User is not associated with any shop");
        }
        SubscriptionRequestResponseDTO response = subscriptionRequestService.createRequest(
                user.getShop().getId(), user.getId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(StandardResponseDTO.builder()
                        .code(201)
                        .message("Subscription request submitted successfully")
                        .data(response)
                        .build());
    }

    @Operation(summary = "Get subscription change request history for own shop")
    @GetMapping("/subscription-requests")
    public ResponseEntity<StandardResponseDTO> getSubscriptionRequests(Authentication auth) {
        UUID shopId = extractShopId(auth);
        List<SubscriptionRequestResponseDTO> requests = subscriptionRequestService.getRequestsByShop(shopId);
        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200)
                .message("Subscription requests retrieved successfully")
                .data(requests)
                .build());
    }
}
