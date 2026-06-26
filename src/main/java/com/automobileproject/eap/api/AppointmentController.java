package com.automobileproject.eap.api;

import com.automobileproject.eap.config.OpenApiConfig;
import com.automobileproject.eap.dto.request.*;
import com.automobileproject.eap.dto.response.AppointmentResponseDTO;
import com.automobileproject.eap.dto.response.AppointmentSlotResponseDTO;
import com.automobileproject.eap.entity.APPOINTMENT_STATUS_TYPES;
import com.automobileproject.eap.entity.SESSION_PERIOD_TYPES;
import com.automobileproject.eap.event.AppointmentCreatedEvent;
import com.automobileproject.eap.service.AppointmentService;
import com.automobileproject.eap.util.StandardResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Appointments", description = "Appointment booking and management")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final ApplicationEventPublisher publisher;

    @Operation(summary = "Book a standard service appointment (Customer only)")
    @PostMapping("/standard-service")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<StandardResponseDTO> createStandardAppointment(
            @Valid @RequestBody StandardAppointmentRequestDTO dto, Authentication authentication) {
        String email = authentication.getName();
        AppointmentResponseDTO response = appointmentService.createStandardAppointment(dto, email);
        publisher.publishEvent(new AppointmentCreatedEvent(this, response.getId(), email));
        return ResponseEntity.status(HttpStatus.CREATED).body(StandardResponseDTO.builder()
                .code(201).message("Appointment booked successfully").data(response).build());
    }

    @Operation(summary = "Book a slot-based service appointment (Customer only)")
    @PostMapping("/slot-based-service")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<StandardResponseDTO> createSlotBasedAppointment(
            @Valid @RequestBody SlotBasedAppointmentRequestDTO dto, Authentication authentication) {
        String email = authentication.getName();
        AppointmentResponseDTO response = appointmentService.createSlotBasedAppointment(dto, email);
        publisher.publishEvent(new AppointmentCreatedEvent(this, response.getId(), email));
        return ResponseEntity.status(HttpStatus.CREATED).body(StandardResponseDTO.builder()
                .code(201).message("Slot-based appointment booked successfully").data(response).build());
    }

    @Operation(summary = "Submit a modification project request (Customer only)")
    @PostMapping("/modification-request")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<StandardResponseDTO> createModificationRequest(
            @Valid @RequestBody ModificationRequestDTO dto, Authentication authentication) {
        String email = authentication.getName();
        AppointmentResponseDTO response = appointmentService.createModificationRequest(dto, email);
        publisher.publishEvent(new AppointmentCreatedEvent(this, response.getId(), email));
        return ResponseEntity.status(HttpStatus.CREATED).body(StandardResponseDTO.builder()
                .code(201).message("Modification request submitted successfully").data(response).build());
    }

    @Operation(summary = "Get available slots for a date and period (public)")
    @GetMapping("/available-slots")
    public ResponseEntity<StandardResponseDTO> getAvailableSlots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam SESSION_PERIOD_TYPES period) {
        List<AppointmentSlotResponseDTO> slots = appointmentService.getAvailableSlots(date, period);
        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200).message("Available slots retrieved").data(slots).build());
    }

    @Operation(summary = "Get all slot templates (public)")
    @GetMapping("/slot-templates")
    public ResponseEntity<StandardResponseDTO> getSlotTemplates() {
        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200).message("Slot templates retrieved").data(appointmentService.getAllSlotTemplates()).build());
    }

    @Operation(summary = "Get all appointments, optionally filtered by status (Employee/Admin)")
    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<StandardResponseDTO> getAppointments(
            @RequestParam(required = false) APPOINTMENT_STATUS_TYPES status) {
        List<AppointmentResponseDTO> list = status != null
                ? appointmentService.getAppointmentsByStatus(status)
                : appointmentService.getAllAppointments();
        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200).message("Appointments retrieved").data(list).build());
    }

    @Operation(summary = "Get all scheduled appointments (Employee/Admin)")
    @GetMapping("/scheduled")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<StandardResponseDTO> getScheduledAppointments() {
        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200).message("Scheduled appointments retrieved")
                .data(appointmentService.getScheduledAppointments()).build());
    }

    @Operation(summary = "Get all completed appointments / service history (Employee/Admin)")
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<StandardResponseDTO> getAllServiceHistory() {
        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200).message("Service history retrieved")
                .data(appointmentService.getAllCompletedAppointments()).build());
    }

    @Operation(summary = "Get employee's in-progress appointments (Employee/Admin)")
    @GetMapping("/my-inprogress")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<StandardResponseDTO> getMyInProgressAppointments(Authentication auth) {
        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200).message("In-progress appointments retrieved")
                .data(appointmentService.getEmployeeInProgressAppointments(auth.getName())).build());
    }

    @Operation(summary = "Get employee's awaiting-parts appointments (Employee/Admin)")
    @GetMapping("/my-awaiting-parts")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<StandardResponseDTO> getMyAwaitingPartsAppointments(Authentication auth) {
        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200).message("Awaiting-parts appointments retrieved")
                .data(appointmentService.getEmployeeAwaitingPartsAppointments(auth.getName())).build());
    }

    @Operation(summary = "Get employee's completed appointments (Employee/Admin)")
    @GetMapping("/my-completed")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<StandardResponseDTO> getMyCompletedAppointments(Authentication auth) {
        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200).message("Completed appointments retrieved")
                .data(appointmentService.getEmployeeCompletedAppointments(auth.getName())).build());
    }

    @Operation(summary = "Get the logged-in customer's appointments (Customer only)")
    @GetMapping("/my-appointments")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<StandardResponseDTO> getMyAppointments(Authentication auth) {
        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200).message("Your appointments retrieved")
                .data(appointmentService.getAppointmentsByCustomerEmail(auth.getName())).build());
    }

    @Operation(summary = "Get the logged-in customer's service history (Customer only)")
    @GetMapping("/my-history")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<StandardResponseDTO> getMyServiceHistory(Authentication auth) {
        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200).message("Your service history retrieved")
                .data(appointmentService.getCustomerServiceHistory(auth.getName())).build());
    }

    @Operation(summary = "Update appointment status (Employee/Admin)")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<StandardResponseDTO> updateStatus(
            @PathVariable UUID id, @Valid @RequestBody UpdateAppointmentStatusRequestDTO dto) {
        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200).message("Status updated")
                .data(appointmentService.updateAppointmentStatus(id, dto.getStatus())).build());
    }

    @Operation(summary = "Update technician notes (Employee/Admin)")
    @PutMapping("/{id}/notes")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<StandardResponseDTO> updateNotes(
            @PathVariable UUID id, @Valid @RequestBody UpdateTechnicianNotesRequestDTO dto) {
        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200).message("Technician notes updated")
                .data(appointmentService.updateTechnicianNotes(id, dto.getTechnicianNotes())).build());
    }

    @Operation(summary = "Submit a quote for a modification project (Employee/Admin)")
    @PostMapping("/{id}/quote")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<StandardResponseDTO> submitQuote(
            @PathVariable UUID id, @Valid @RequestBody QuoteRequestDTO dto) {
        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200).message("Quote submitted")
                .data(appointmentService.submitQuote(id, dto.getQuotePrice(), dto.getQuoteDetails())).build());
    }

    @Operation(summary = "Manually assign an employee to an appointment (Employee/Admin)")
    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<StandardResponseDTO> assignEmployee(
            @PathVariable UUID id, @Valid @RequestBody AssignEmployeeRequestDTO dto) {
        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200).message("Employee assigned")
                .data(appointmentService.assignEmployee(id, dto.getEmployeeId())).build());
    }

    @Operation(summary = "Accept a scheduled appointment (Employee/Admin)")
    @PostMapping("/{id}/accept")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<StandardResponseDTO> acceptAppointment(
            @PathVariable UUID id, Authentication auth) {
        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200).message("Appointment accepted and started")
                .data(appointmentService.acceptAppointment(id, auth.getName())).build());
    }

    @Operation(summary = "Cancel an appointment (Employee/Admin)")
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<StandardResponseDTO> cancelAppointment(@PathVariable UUID id) {
        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200).message("Appointment cancelled")
                .data(appointmentService.cancelAppointment(id)).build());
    }
}
