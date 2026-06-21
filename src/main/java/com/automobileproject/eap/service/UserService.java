package com.automobileproject.eap.service;

import com.automobileproject.eap.dto.request.UserRequestDTO;
import com.automobileproject.eap.dto.response.UserResponseDTO;

public interface UserService {

    UserResponseDTO registerUser(UserRequestDTO dto);

    UserResponseDTO findByEmail(String email);

    UserResponseDTO findByUsername(String username);

    void verifyEmail(String token);

    void requestPasswordReset(String email);

    void resetPassword(String token, String newPassword);

    void validatePasswordResetToken(String token);
}
