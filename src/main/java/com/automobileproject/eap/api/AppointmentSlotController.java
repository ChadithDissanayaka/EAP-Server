package com.automobileproject.eap.api;

import com.automobileproject.eap.config.OpenApiConfig;
import com.automobileproject.eap.dto.request.AppointmentSlotRequestDTO;
import com.automobileproject.eap.dto.response.AppointmentSlotResponseDTO;
import com.automobileproject.eap.entity.SESSION_PERIOD_TYPES;
import com.automobileproject.eap.entity.User;
import com.automobileproject.eap.exception.ValidationException;
import com.automobileproject.eap.repo.UserRepo;
import com.automobileproject.eap.service.AppointmentSlotService;
import com.automobileproject.eap.util.StandardResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/slots")
@RequiredArgsConstructor
@Tag(name = "Appointment Slots", description = "Slot template management and availability checking")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class AppointmentSlotController {

        private final AppointmentSlotService appointmentSlotService;
        private final UserRepo userRepo;

        @Operation(summary = "Get all available slots for a specific date (public)")
        @GetMapping("/available")
        public ResponseEntity<StandardResponseDTO> getAvailableSlots(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

                List<AppointmentSlotResponseDTO> slots = appointmentSlotService.getAvailableSlotsForDate(date);
                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(StandardResponseDTO.builder()
                                                .code(200)
                                                .message("Available slots retrieved successfully")
                                                .data(slots)
                                                .build());
        }

        @Operation(summary = "Get available slots for a specific date and session period (public)")
        @GetMapping("/available/by-period")
        public ResponseEntity<StandardResponseDTO> getAvailableSlotsByPeriod(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                        @RequestParam SESSION_PERIOD_TYPES period) {

                List<AppointmentSlotResponseDTO> slots = appointmentSlotService.getAvailableSlotsByPeriod(date, period);
                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(StandardResponseDTO.builder()
                                                .code(200)
                                                .message("Available slots for period retrieved successfully")
                                                .data(slots)
                                                .build());
        }

        @Operation(summary = "Check if a specific slot is available on a date (public)")
        @GetMapping("/check-availability")
        public ResponseEntity<StandardResponseDTO> checkSlotAvailability(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                        @RequestParam SESSION_PERIOD_TYPES period,
                        @RequestParam Integer slotNumber) {

                boolean available = appointmentSlotService.isSlotAvailable(date, period, slotNumber);
                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(StandardResponseDTO.builder()
                                                .code(200)
                                                .message("Slot availability checked successfully")
                                                .data(Map.of(
                                                                "date", date.toString(),
                                                                "period", period.name(),
                                                                "slotNumber", slotNumber,
                                                                "available", available))
                                                .build());
        }

        @Operation(summary = "Get count of available slots for a date (public)")
        @GetMapping("/count")
        public ResponseEntity<StandardResponseDTO> getAvailableSlotCount(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                        @RequestParam(required = false) SESSION_PERIOD_TYPES period) {

                Long count = (period != null)
                                ? appointmentSlotService.countAvailableSlotsByPeriod(date, period)
                                : appointmentSlotService.countAvailableSlots(date);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(StandardResponseDTO.builder()
                                                .code(200)
                                                .message("Slot count retrieved successfully")
                                                .data(Map.of(
                                                                "date", date.toString(),
                                                                "period", period != null ? period.name() : "ALL",
                                                                "availableSlots", count))
                                                .build());
        }

        @Operation(summary = "Get all slot templates (Employee/Admin only)")
        @GetMapping("/templates")
        @PreAuthorize("hasAnyRole('EMPLOYEE', 'SHOP_OWNER')")
        public ResponseEntity<StandardResponseDTO> getAllSlotTemplates(Authentication auth) {
                UUID shopId = extractShopId(auth);
                List<AppointmentSlotResponseDTO> templates = appointmentSlotService.getSlotTemplatesByShop(shopId);
                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(StandardResponseDTO.builder()
                                                .code(200)
                                                .message("Slot templates retrieved successfully")
                                                .data(templates)
                                                .build());
        }

        @Operation(summary = "Create a slot template (Admin only)")
        @PostMapping("/templates")
        @PreAuthorize("hasRole('SHOP_OWNER')")
        public ResponseEntity<StandardResponseDTO> createSlotTemplate(
                        @Valid @RequestBody AppointmentSlotRequestDTO dto, Authentication auth) {
                UUID shopId = extractShopId(auth);
                AppointmentSlotResponseDTO template = appointmentSlotService.createSlotTemplate(shopId, dto);
                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(StandardResponseDTO.builder()
                                                .code(201)
                                                .message("Slot template created successfully")
                                                .data(template)
                                                .build());
        }

        @Operation(summary = "Delete a slot template (Admin only)")
        @DeleteMapping("/templates/{id}")
        @PreAuthorize("hasRole('SHOP_OWNER')")
        public ResponseEntity<StandardResponseDTO> deleteSlotTemplate(
                        @PathVariable UUID id, Authentication auth) {
                UUID shopId = extractShopId(auth);
                appointmentSlotService.deleteSlotTemplate(id, shopId);
                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(StandardResponseDTO.builder()
                                                .code(200)
                                                .message("Slot template deleted successfully")
                                                .data(null)
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
