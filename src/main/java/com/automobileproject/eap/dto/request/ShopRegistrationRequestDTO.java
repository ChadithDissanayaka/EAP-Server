package com.automobileproject.eap.dto.request;

import com.automobileproject.eap.entity.SUBSCRIPTION_PLAN_TYPES;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ShopRegistrationRequestDTO {

    // ── Shop details ─────────────────────────────────────────────────────────
    @NotBlank(message = "Shop name is required")
    @Size(max = 100, message = "Shop name must not exceed 100 characters")
    private String shopName;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String shopDescription;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String shopAddress;

    @Pattern(regexp = "\\d{10,15}", message = "Shop phone number must be between 10 and 15 digits")
    private String shopPhoneNumber;

    @Email(message = "Shop email should be valid")
    private String shopEmail;

    @NotNull(message = "Subscription plan is required")
    private SUBSCRIPTION_PLAN_TYPES subscriptionPlan;

    // ── Owner account details ────────────────────────────────────────────────
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "\\d{10,15}", message = "Phone number must be between 10 and 15 digits")
    private String phoneNumber;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;
}
