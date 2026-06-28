package com.automobileproject.eap.service;

import com.automobileproject.eap.dto.request.UserRequestDTO;
import com.automobileproject.eap.dto.response.UserResponseDTO;
import com.automobileproject.eap.dto.response.EmployeeResponseDTO;

import java.util.List;

public interface UserService {

    UserResponseDTO registerUser(UserRequestDTO dto);

    UserResponseDTO findByEmail(String email);

    UserResponseDTO findByUsername(String username);

    void verifyEmail(String token);

    void requestPasswordReset(String email);

    void resetPassword(String token, String newPassword);

    void validatePasswordResetToken(String token);

    List<EmployeeResponseDTO> getActiveEmployees();
}
