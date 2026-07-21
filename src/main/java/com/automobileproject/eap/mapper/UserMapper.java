package com.automobileproject.eap.mapper;

import com.automobileproject.eap.dto.request.UserRequestDTO;
import com.automobileproject.eap.dto.response.EmployeeResponseDTO;
import com.automobileproject.eap.dto.response.LoginResponseDTO;
import com.automobileproject.eap.dto.response.UserResponseDTO;
import com.automobileproject.eap.entity.User;
import com.automobileproject.eap.exception.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(UserRequestDTO dto) {
        if (dto == null) {
            throw new ValidationException("UserRequestDTO must not be null");
        }
        return User.builder()
                .username(dto.getUsername())
                .password(dto.getPassword())
                .email(dto.getEmail())
                .role(dto.getRole())
                .phoneNumber(dto.getPhoneNumber())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .build();
    }

    public UserResponseDTO toResponseDTO(User user) {
        if (user == null) {
            throw new ValidationException("User entity must not be null");
        }
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .emailVerified(user.getEmailVerified())
                .isActive(user.isActive())
                .build();
    }

    public LoginResponseDTO toLoginResponseDTO(User user, String token) {
        if (user == null) {
            throw new ValidationException("User entity must not be null");
        }
        return LoginResponseDTO.builder()
                .message("Login successful")
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .token(token)
                .build();
    }

    public EmployeeResponseDTO toEmployeeResponseDTO(User user) {
        if (user == null) {
            throw new ValidationException("User entity must not be null");
        }
        return EmployeeResponseDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .build();
    }
}
