package com.automobileproject.eap.service.impl;

import com.automobileproject.eap.dto.request.UserRequestDTO;
import com.automobileproject.eap.dto.response.EmployeeResponseDTO;
import com.automobileproject.eap.dto.response.UserResponseDTO;
import com.automobileproject.eap.entity.ROLE_TYPES;
import com.automobileproject.eap.entity.User;
import com.automobileproject.eap.exception.DuplicateEntryException;
import com.automobileproject.eap.exception.EntryNotFoundException;
import com.automobileproject.eap.exception.ValidationException;
import com.automobileproject.eap.mapper.UserMapper;
import com.automobileproject.eap.repo.UserRepo;
import com.automobileproject.eap.service.EmailService;
import com.automobileproject.eap.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final EmailService emailService;

    @Override
    @Transactional
    public UserResponseDTO registerUser(UserRequestDTO dto) {
        log.info("Attempting to register user with email: {}", dto.getEmail());

        if (userRepo.findByUsername(dto.getUsername()).isPresent()) {
            throw new DuplicateEntryException("Username already exists: " + dto.getUsername());
        }
        if (userRepo.findByEmail(dto.getEmail()).isPresent()) {
            throw new DuplicateEntryException("Email already registered: " + dto.getEmail());
        }

        User user = userMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        user.setEmailVerified(false);

        User savedUser = userRepo.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        try {
            emailService.sendVerificationEmail(savedUser.getEmail(), verificationToken);
            log.info("Verification email sent to: {}", savedUser.getEmail());
        } catch (Exception e) {
            log.error("Failed to send verification email, but user was saved", e);
        }

        UserResponseDTO response = userMapper.toResponseDTO(savedUser);
        response.setMessage("User registered successfully. Please check your email to verify your account.");
        return response;
    }

    @Override
    public UserResponseDTO findByEmail(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new EntryNotFoundException("User not found with email: " + email));
        return userMapper.toResponseDTO(user);
    }

    @Override
    public UserResponseDTO findByUsername(String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new EntryNotFoundException("User not found with username: " + username));
        return userMapper.toResponseDTO(user);
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        User user = userRepo.findByVerificationToken(token)
                .orElseThrow(() -> new ValidationException("Invalid verification token"));

        if (user.getEmailVerified()) {
            throw new ValidationException("Email is already verified");
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        userRepo.save(user);
        log.info("Email verified successfully for user: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new EntryNotFoundException("User not found with email: " + email));

        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepo.save(user);
        log.info("Password reset token generated for user: {}", email);

        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
        log.info("Password reset email sent to: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepo.findByPasswordResetToken(token)
                .orElseThrow(() -> new ValidationException("Invalid password reset token"));

        if (user.getPasswordResetTokenExpiry() == null ||
                LocalDateTime.now().isAfter(user.getPasswordResetTokenExpiry())) {
            throw new ValidationException("Password reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepo.save(user);
        log.info("Password reset successfully for user: {}", user.getEmail());
    }

    @Override
    public void validatePasswordResetToken(String token) {
        User user = userRepo.findByPasswordResetToken(token)
                .orElseThrow(() -> new ValidationException("Invalid password reset token"));

        if (user.getPasswordResetTokenExpiry() == null ||
                LocalDateTime.now().isAfter(user.getPasswordResetTokenExpiry())) {
            throw new ValidationException("Password reset token has expired");
        }

        log.info("Password reset token validated for user: {}", user.getEmail());
    }

    @Override
    public List<EmployeeResponseDTO> getActiveEmployees() {
        return userRepo.findByRole(ROLE_TYPES.EMPLOYEE)
                .stream()
                .filter(User::isActive)
                .map(userMapper::toEmployeeResponseDTO)
                .toList();
    }
}
