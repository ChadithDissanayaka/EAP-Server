package com.automobileproject.eap.api;

import com.automobileproject.eap.dto.request.UserRequestDTO;
import com.automobileproject.eap.dto.response.UserResponseDTO;
import com.automobileproject.eap.entity.ROLE_TYPES;
import com.automobileproject.eap.service.UserService;
import com.automobileproject.eap.util.StandardResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management and lookup endpoints")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get all active employees (Employee/Admin)")
    @GetMapping("/employees")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'SHOP_OWNER')")
    public ResponseEntity<StandardResponseDTO> getEmployees() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponseDTO.builder()
                        .code(200)
                        .message("Employees retrieved")
                        .data(userService.getActiveEmployees())
                        .build());
    }

    @Operation(summary = "Get all users by role (Admin only)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StandardResponseDTO> getUsersByRole(@RequestParam ROLE_TYPES role) {
        List<UserResponseDTO> users = userService.getUsersByRole(role);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponseDTO.builder()
                        .code(200)
                        .message("Users retrieved successfully")
                        .data(users)
                        .build());
    }

    @Operation(summary = "Create a new employee (Admin only)")
    @PostMapping("/create-employee")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StandardResponseDTO> createEmployee(@RequestBody @Valid UserRequestDTO dto) {
        UserResponseDTO employee = userService.createEmployee(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(StandardResponseDTO.builder()
                        .code(201)
                        .message("Employee created successfully")
                        .data(employee)
                        .build());
    }

    @Operation(summary = "Toggle user active/inactive status (Admin only)")
    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StandardResponseDTO> toggleUserStatus(@PathVariable UUID id) {
        userService.toggleUserStatus(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponseDTO.builder()
                        .code(200)
                        .message("User status toggled successfully")
                        .build());
    }
}