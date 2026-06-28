package com.automobileproject.eap.service;

import com.automobileproject.eap.dto.request.TimeLogRequestDTO;
import com.automobileproject.eap.dto.response.TimeLogResponseDTO;

import java.util.List;
import java.util.UUID;

public interface TimeLogService {

    List<TimeLogResponseDTO> getTimeLogsByAppointmentId(UUID appointmentId);

    List<TimeLogResponseDTO> getMyTimeLogs(String employeeEmail);

    List<TimeLogResponseDTO> getAllTimeLogs();

    TimeLogResponseDTO createTimeLog(UUID appointmentId, TimeLogRequestDTO dto, String employeeEmail);
}
