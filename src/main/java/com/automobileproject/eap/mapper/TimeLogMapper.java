package com.automobileproject.eap.mapper;

import com.automobileproject.eap.dto.request.TimeLogRequestDTO;
import com.automobileproject.eap.dto.response.TimeLogResponseDTO;
import com.automobileproject.eap.entity.Appointment;
import com.automobileproject.eap.entity.TimeLog;
import com.automobileproject.eap.entity.User;
import com.automobileproject.eap.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class TimeLogMapper {

    /**
     * Converts a TimeLogRequestDTO into a TimeLog entity.
     * Appointment and Employee are resolved in the service layer and passed in.
     */
    public TimeLog toEntity(TimeLogRequestDTO dto, Appointment appointment, User employee) {
        if (dto == null) {
            throw new ValidationException("TimeLogRequestDTO must not be null");
        }
        return TimeLog.builder()
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .notes(dto.getNotes())
                .appointment(appointment)
                .employee(employee)
                .build();
    }

    public TimeLogResponseDTO toResponseDTO(TimeLog timeLog) {
        if (timeLog == null) {
            throw new ValidationException("TimeLog entity must not be null");
        }

        TimeLogResponseDTO.TimeLogResponseDTOBuilder builder = TimeLogResponseDTO.builder()
                .id(timeLog.getId())
                .startTime(timeLog.getStartTime())
                .endTime(timeLog.getEndTime())
                .notes(timeLog.getNotes())
                .appointmentId(timeLog.getAppointment().getId())
                .employeeId(timeLog.getEmployee().getId())
                .employeeFirstName(timeLog.getEmployee().getFirstName())
                .employeeLastName(timeLog.getEmployee().getLastName())
                .employeeEmail(timeLog.getEmployee().getEmail());

        // Flatten appointment context
        if (timeLog.getAppointment() != null) {
            com.automobileproject.eap.entity.Service primaryService = timeLog.getAppointment().getPrimaryService();
            if (primaryService != null) {
                builder.serviceName(primaryService.getName());
            }
            if (timeLog.getAppointment().getVehicle() != null) {
                builder.vehicleModel(timeLog.getAppointment().getVehicle().getModel());
                builder.vehicleNumber(timeLog.getAppointment().getVehicle().getLicensePlate());
            }
        }

        // Calculate duration
        if (timeLog.getEndTime() != null && timeLog.getStartTime() != null) {
            Duration duration = Duration.between(timeLog.getStartTime(), timeLog.getEndTime());
            long totalSeconds = duration.getSeconds();
            long hours = totalSeconds / 3600;
            long minutes = (totalSeconds % 3600) / 60;
            long seconds = totalSeconds % 60;
            builder.durationMinutes(duration.toMinutes());
            builder.formattedDuration(String.format("%02d:%02d:%02d", hours, minutes, seconds));
        } else {
            builder.formattedDuration("In Progress");
        }

        return builder.build();
    }
}
