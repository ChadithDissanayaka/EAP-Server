package com.automobileproject.eap.api;

import com.automobileproject.eap.dto.request.ForgotPasswordRequestDTO;
import com.automobileproject.eap.dto.request.LoginRequestDTO;
import com.automobileproject.eap.dto.request.ResetPasswordRequestDTO;
import com.automobileproject.eap.dto.request.ShopRegistrationRequestDTO;
import com.automobileproject.eap.dto.request.UserRequestDTO;
import com.automobileproject.eap.dto.response.LoginResponseDTO;
import com.automobileproject.eap.dto.response.ShopResponseDTO;
import com.automobileproject.eap.dto.response.UserResponseDTO;
import com.automobileproject.eap.entity.User;
import com.automobileproject.eap.mapper.UserMapper;
import com.automobileproject.eap.repo.UserRepo;
import com.automobileproject.eap.service.ShopService;
import com.automobileproject.eap.service.UserService;
import com.automobileproject.eap.util.JwtUtil;
import com.automobileproject.eap.util.StandardResponseDTO;
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

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Registration, login, email verification, and password reset")
public class AuthController {

    private final UserService userService;
    private final ShopService shopService;
    private final AuthenticationManager authenticationManager;
    private final UserRepo userRepo;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    @Operation(summary = "Register a new shop with owner account")
    @PostMapping("/register-shop")
    public ResponseEntity<StandardResponseDTO> registerShop(@Valid @RequestBody ShopRegistrationRequestDTO dto) {
        ShopResponseDTO response = shopService.registerShop(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(StandardResponseDTO.builder()
                        .code(201)
                        .message("Shop registered successfully. Please verify your email. Your shop is pending approval.")
                        .data(response)
                        .build());
    }

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<StandardResponseDTO> register(@Valid @RequestBody UserRequestDTO dto) {
        UserResponseDTO response = userService.registerUser(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(StandardResponseDTO.builder()
                        .code(201)
                        .message("User registered successfully. Please verify your email.")
                        .data(response)
                        .build());
    }

    @Operation(summary = "Login and receive JWT token")
    @PostMapping("/login")
    public ResponseEntity<StandardResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        // Check email verification before authenticating
        User user = userRepo.findByEmail(dto.getEmail())
                .orElseThrow(() -> new com.automobileproject.eap.exception.EntryNotFoundException(
                        "User not found with email: " + dto.getEmail()));

        if (!user.getEmailVerified()) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
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

        log.info("Login successful for: {}", user.getEmail());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponseDTO.builder()
                        .code(200)
                        .message("Login successful")
                        .data(loginResponse)
                        .build());
    }

    @Operation(summary = "Verify email address using token from email link")
    @GetMapping("/verify-email")
    public ResponseEntity<StandardResponseDTO> verifyEmail(@RequestParam("token") String token) {
        userService.verifyEmail(token);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponseDTO.builder()
                        .code(200)
                        .message("Email verified successfully. You can now log in.")
                        .data(null)
                        .build());
    }

    @Operation(summary = "Request a password reset email")
    @PostMapping("/forgot-password")
    public ResponseEntity<StandardResponseDTO> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO dto) {
        // Always return success to avoid revealing whether email exists
        try {
            userService.requestPasswordReset(dto.getEmail());
        } catch (Exception e) {
            log.warn("Forgot password attempt for unknown email (suppressed): {}", dto.getEmail());
        }
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponseDTO.builder()
                        .code(200)
                        .message("If the email exists, a password reset link will be sent.")
                        .data(null)
                        .build());
    }

    @Operation(summary = "Validate a password reset token")
    @GetMapping("/reset-password")
    public ResponseEntity<StandardResponseDTO> validateResetToken(@RequestParam("token") String token) {
        userService.validatePasswordResetToken(token);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponseDTO.builder()
                        .code(200)
                        .message("Token is valid. Submit your new password via POST /auth/reset-password.")
                        .data(null)
                        .build());
    }

    @Operation(summary = "Reset password using token")
    @PostMapping("/reset-password")
    public ResponseEntity<StandardResponseDTO> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO dto) {
        userService.resetPassword(dto.getToken(), dto.getNewPassword());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponseDTO.builder()
                        .code(200)
                        .message("Password reset successfully. You can now log in with your new password.")
                        .data(null)
                        .build());
    }
}
