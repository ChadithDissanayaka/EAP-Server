package com.automobileproject.eap.service;

import com.automobileproject.eap.dto.request.UserRequestDTO;
import com.automobileproject.eap.dto.request.EmployeeCreateRequestDTO;
import com.automobileproject.eap.dto.response.UserResponseDTO;
import com.automobileproject.eap.dto.response.EmployeeResponseDTO;
import com.automobileproject.eap.entity.ROLE_TYPES;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponseDTO registerUser(UserRequestDTO dto);

    UserResponseDTO findByEmail(String email);

    UserResponseDTO findByUsername(String username);

    void verifyEmail(String token);

    void requestPasswordReset(String email);

    void resetPassword(String token, String newPassword);

    void validatePasswordResetToken(String token);

    List<EmployeeResponseDTO> getActiveEmployees();

    UserResponseDTO createEmployee(UUID shopId, EmployeeCreateRequestDTO dto);

    List<UserResponseDTO> getShopEmployees(UUID shopId);

    List<UserResponseDTO> getShopCustomers(UUID shopId);

    void toggleUserStatus(UUID userId, UUID shopId);

    UserResponseDTO registerCustomerForShop(UUID shopId, UserRequestDTO dto);
}
