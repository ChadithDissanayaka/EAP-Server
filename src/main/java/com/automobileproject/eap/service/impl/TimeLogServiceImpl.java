package com.automobileproject.eap.service.impl;

import com.automobileproject.eap.dto.request.TimeLogRequestDTO;
import com.automobileproject.eap.dto.response.TimeLogResponseDTO;
import com.automobileproject.eap.entity.Appointment;
import com.automobileproject.eap.entity.TimeLog;
import com.automobileproject.eap.entity.User;
import com.automobileproject.eap.exception.EntryNotFoundException;
import com.automobileproject.eap.exception.ValidationException;
import com.automobileproject.eap.mapper.TimeLogMapper;
import com.automobileproject.eap.repo.AppointmentRepo;
import com.automobileproject.eap.repo.TimeLogRepo;
import com.automobileproject.eap.repo.UserRepo;
import com.automobileproject.eap.service.TimeLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimeLogServiceImpl implements TimeLogService {

    private final TimeLogRepo timeLogRepo;
    private final AppointmentRepo appointmentRepo;
    private final UserRepo userRepo;
    private final TimeLogMapper timeLogMapper;

    @Override
    @Transactional(readOnly = true)
    public List<TimeLogResponseDTO> getTimeLogsByAppointmentId(UUID appointmentId) {
        return timeLogRepo.findByAppointmentIdWithRelations(appointmentId)
                .stream()
                .map(timeLogMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeLogResponseDTO> getMyTimeLogs(String employeeEmail) {
        User employee = findUserByEmail(employeeEmail);
        return timeLogRepo.findByEmployeeIdWithRelations(employee.getId())
                .stream()
                .map(timeLogMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeLogResponseDTO> getAllTimeLogs() {
        return timeLogRepo.findAllWithRelations()
                .stream()
                .map(timeLogMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TimeLogResponseDTO createTimeLog(UUID appointmentId, TimeLogRequestDTO dto, String employeeEmail) {
        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new EntryNotFoundException("Appointment not found with ID: " + appointmentId));

        User employee = findUserByEmail(employeeEmail);

        boolean isAssigned = appointment.getAssignedEmployees().stream()
                .anyMatch(u -> u.getId().equals(employee.getId()));
        if (!isAssigned) {
            throw new AccessDeniedException("You are not assigned to this appointment.");
        }

        if (dto.getEndTime() != null && dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new ValidationException("End time must be after start time.");
        }

        TimeLog timeLog = timeLogMapper.toEntity(dto, appointment, employee);
        TimeLog saved = timeLogRepo.save(timeLog);
        log.info("TimeLog created: {} for appointment: {} by employee: {}", saved.getId(), appointmentId, employeeEmail);
        return timeLogMapper.toResponseDTO(saved);
    }

    private User findUserByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new EntryNotFoundException("User not found with email: " + email));
    }
}
