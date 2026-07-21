package com.automobileproject.eap.api;

import com.automobileproject.eap.dto.request.LoginRequestDTO;
import com.automobileproject.eap.dto.request.UserRequestDTO;
import com.automobileproject.eap.dto.response.ShopPageResponseDTO;
import com.automobileproject.eap.dto.response.ShopResponseDTO;
import com.automobileproject.eap.dto.response.UserResponseDTO;
import com.automobileproject.eap.entity.SHOP_STATUS_TYPES;
import com.automobileproject.eap.entity.Shop;
import com.automobileproject.eap.entity.User;
import com.automobileproject.eap.exception.EntryNotFoundException;
import com.automobileproject.eap.exception.ValidationException;
import com.automobileproject.eap.mapper.UserMapper;
import com.automobileproject.eap.repo.ShopRepo;
import com.automobileproject.eap.repo.UserRepo;
import com.automobileproject.eap.service.ShopPageService;
import com.automobileproject.eap.service.ShopService;
import com.automobileproject.eap.service.UserService;
import com.automobileproject.eap.util.JwtUtil;
import com.automobileproject.eap.util.StandardResponseDTO;
import com.automobileproject.eap.dto.response.LoginResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/portal")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer Portal", description = "Public customer-facing endpoints per shop (slug-based)")
public class CustomerPortalController {

    private final ShopService shopService;
    private final ShopPageService shopPageService;
    private final UserService userService;
    private final ShopRepo shopRepo;
    private final UserRepo userRepo;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final com.automobileproject.eap.repo.ServiceRepo serviceRepo;
    private final com.automobileproject.eap.mapper.ServiceMapper serviceMapper;
    private final com.automobileproject.eap.repo.ServiceCategoryRepo serviceCategoryRepo;
    private final com.automobileproject.eap.mapper.ServiceCategoryMapper serviceCategoryMapper;

    @Operation(summary = "Get shop public page data")
    @GetMapping("/{slug}")
    public ResponseEntity<StandardResponseDTO> getShopPortal(@PathVariable String slug) {
        ShopResponseDTO shop = shopService.getShopBySlug(slug);

        // Only show if shop is active
        if (!"ACTIVE".equals(shop.getStatus())) {
            throw new EntryNotFoundException("Shop not found");
        }

        ShopPageResponseDTO page = shopPageService.getPageByShopSlug(slug);

        // Fetch services and categories for this shop
        java.util.List<com.automobileproject.eap.dto.response.ServiceResponseDTO> services = serviceRepo.findByShopId(shop.getId()).stream()
                .map(serviceMapper::toResponseDTO)
                .toList();

        java.util.List<com.automobileproject.eap.dto.response.ServiceCategoryResponseDTO> categories = serviceCategoryRepo.findByShopId(shop.getId()).stream()
                .map(serviceCategoryMapper::toResponseDTO)
                .toList();

        Map<String, Object> portal = new HashMap<>();
        portal.put("shop", shop);
        portal.put("page", page);
        portal.put("services", services);
        portal.put("categories", categories);

        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200)
                .message("Shop portal retrieved successfully")
                .data(portal)
                .build());
    }

    @Operation(summary = "Register as a customer for a specific shop")
    @PostMapping("/{slug}/register")
    public ResponseEntity<StandardResponseDTO> registerCustomer(
            @PathVariable String slug, @Valid @RequestBody UserRequestDTO dto) {
        Shop shop = shopRepo.findBySlug(slug)
                .orElseThrow(() -> new EntryNotFoundException("Shop not found: " + slug));

        if (shop.getStatus() != SHOP_STATUS_TYPES.ACTIVE) {
            throw new ValidationException("This shop is not currently accepting registrations");
        }

        // Force customer role
        dto.setRole(com.automobileproject.eap.entity.ROLE_TYPES.CUSTOMER);

        UserResponseDTO response = userService.registerCustomerForShop(shop.getId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(StandardResponseDTO.builder()
                        .code(201)
                        .message("Registration successful. Please verify your email.")
                        .data(response)
                        .build());
    }

    @Operation(summary = "Login as a customer for a specific shop")
    @PostMapping("/{slug}/login")
    public ResponseEntity<StandardResponseDTO> loginCustomer(
            @PathVariable String slug, @Valid @RequestBody LoginRequestDTO dto) {
        Shop shop = shopRepo.findBySlug(slug)
                .orElseThrow(() -> new EntryNotFoundException("Shop not found: " + slug));

        if (shop.getStatus() != SHOP_STATUS_TYPES.ACTIVE) {
            throw new ValidationException("This shop is not currently active");
        }

        User user = userRepo.findByEmail(dto.getEmail())
                .orElseThrow(() -> new EntryNotFoundException("User not found"));

        // Verify user belongs to this shop
        if (user.getShop() == null || !user.getShop().getId().equals(shop.getId())) {
            throw new ValidationException("User is not registered with this shop");
        }

        if (!user.getEmailVerified()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(StandardResponseDTO.builder()
                            .code(403)
                            .message("Please verify your email address before logging in.")
                            .data(null)
                            .build());
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));

        String token = jwtUtil.generateAccessToken(user);
        LoginResponseDTO loginResponse = userMapper.toLoginResponseDTO(user, token);

        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200)
                .message("Login successful")
                .data(loginResponse)
                .build());
    }
}
