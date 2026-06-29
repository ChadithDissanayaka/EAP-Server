package com.automobileproject.eap.api;

import com.automobileproject.eap.config.OpenApiConfig;
import com.automobileproject.eap.dto.request.TimeLogRequestDTO;
import com.automobileproject.eap.dto.response.TimeLogResponseDTO;
import com.automobileproject.eap.service.TimeLogService;
import com.automobileproject.eap.util.StandardResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/time-logs")
@RequiredArgsConstructor
@Tag(name = "Time Logs", description = "Employee work time tracking per appointment")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class TimeLogController {

    private final TimeLogService timeLogService;

    @Operation(summary = "Get the logged-in employee's own time logs (Employee only)")
    @GetMapping("/my-logs")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<StandardResponseDTO> getMyTimeLogs(Authentication auth) {
        List<TimeLogResponseDTO> logs = timeLogService.getMyTimeLogs(auth.getName());
        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200).message("Your time logs retrieved").data(logs).build());
    }

    @Operation(summary = "Get all time logs across all employees (Admin only)")
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StandardResponseDTO> getAllTimeLogs() {
        List<TimeLogResponseDTO> logs = timeLogService.getAllTimeLogs();
        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200).message("All time logs retrieved").data(logs).build());
    }

    @Operation(summary = "Get all time logs for a specific appointment (Employee/Admin)")
    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<StandardResponseDTO> getTimeLogsByAppointment(@PathVariable UUID appointmentId) {
        List<TimeLogResponseDTO> logs = timeLogService.getTimeLogsByAppointmentId(appointmentId);
        return ResponseEntity.ok(StandardResponseDTO.builder()
                .code(200).message("Time logs for appointment retrieved").data(logs).build());
    }

    @Operation(summary = "Create a new time log for an appointment (Employee only)")
    @PostMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<StandardResponseDTO> createTimeLog(
            @PathVariable UUID appointmentId,
            @Valid @RequestBody TimeLogRequestDTO dto,
            Authentication auth) {
        TimeLogResponseDTO log = timeLogService.createTimeLog(appointmentId, dto, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(StandardResponseDTO.builder()
                .code(201).message("Time log created successfully").data(log).build());
    }
}
