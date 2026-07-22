package com.automobileproject.eap.api;

import com.automobileproject.eap.dto.request.EmployeeCreateRequestDTO;
import com.automobileproject.eap.dto.response.UserResponseDTO;
import com.automobileproject.eap.entity.ROLE_TYPES;
import com.automobileproject.eap.entity.User;
import com.automobileproject.eap.exception.ValidationException;
import com.automobileproject.eap.repo.UserRepo;
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

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management and lookup endpoints")
public class UserController {

    private final UserService userService;
    private final UserRepo userRepo;

    @Operation(summary = "Get all active employees (Employee/Shop Owner/Super Admin)")
    @GetMapping("/employees")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'SHOP_OWNER', 'SUPER_ADMIN')")
    public ResponseEntity<StandardResponseDTO> getEmployees() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponseDTO.builder()
                        .code(200)
                        .message("Employees retrieved")
                        .data(userService.getActiveEmployees())
                        .build());
    }

    @Operation(summary = "Get shop users by role (Shop Owner only)")
    @GetMapping
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public ResponseEntity<StandardResponseDTO> getUsersByRole(
            @RequestParam ROLE_TYPES role, Authentication auth) {
        UUID shopId = extractShopId(auth);
        List<UserResponseDTO> users;
        if (role == ROLE_TYPES.EMPLOYEE) {
            users = userService.getShopEmployees(shopId);
        } else if (role == ROLE_TYPES.CUSTOMER) {
            users = userService.getShopCustomers(shopId);
        } else {
            users = List.of();
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponseDTO.builder()
                        .code(200)
                        .message("Users retrieved successfully")
                        .data(users)
                        .build());
    }

    @Operation(summary = "Create a new employee (Shop Owner only)")
    @PostMapping("/create-employee")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public ResponseEntity<StandardResponseDTO> createEmployee(
            @RequestBody @Valid EmployeeCreateRequestDTO dto, Authentication auth) {
        UUID shopId = extractShopId(auth);
        UserResponseDTO employee = userService.createEmployee(shopId, dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(StandardResponseDTO.builder()
                        .code(201)
                        .message("Employee created successfully")
                        .data(employee)
                        .build());
    }

    @Operation(summary = "Toggle user active/inactive status (Shop Owner only)")
    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('SHOP_OWNER')")
    public ResponseEntity<StandardResponseDTO> toggleUserStatus(
            @PathVariable UUID id, Authentication auth) {
        UUID shopId = extractShopId(auth);
        userService.toggleUserStatus(id, shopId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponseDTO.builder()
                        .code(200)
                        .message("User status toggled successfully")
                        .build());
    }

    private UUID extractShopId(Authentication auth) {
        String email = auth.getName();
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ValidationException("Authenticated user not found"));
        if (user.getShop() == null) {
            throw new ValidationException("User is not associated with any shop");
        }
        return user.getShop().getId();
    }
}