package com.automobileproject.eap.mapper;

import com.automobileproject.eap.dto.request.AppointmentSlotRequestDTO;
import com.automobileproject.eap.dto.response.AppointmentSlotResponseDTO;
import com.automobileproject.eap.entity.AppointmentSlot;
import com.automobileproject.eap.exception.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class AppointmentSlotMapper {

    public AppointmentSlot toEntity(AppointmentSlotRequestDTO dto) {
        if (dto == null) {
            throw new ValidationException("AppointmentSlotRequestDTO must not be null");
        }
        return AppointmentSlot.builder()
                .sessionPeriod(dto.getSessionPeriod())
                .slotNumber(dto.getSlotNumber())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .build();
    }

    public AppointmentSlotResponseDTO toResponseDTO(AppointmentSlot slot) {
        if (slot == null) {
            throw new ValidationException("AppointmentSlot entity must not be null");
        }
        return AppointmentSlotResponseDTO.builder()
                .id(slot.getId())
                .sessionPeriod(slot.getSessionPeriod())
                .slotNumber(slot.getSlotNumber())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .slotDescription(slot.getSlotDescription())
                .isAvailable(true) // default — caller overrides when checking date availability
                .build();
    }

    /** Convenience overload that lets the service inject the computed availability flag. */
    public AppointmentSlotResponseDTO toResponseDTO(AppointmentSlot slot, boolean isAvailable) {
        AppointmentSlotResponseDTO dto = toResponseDTO(slot);
        dto.setIsAvailable(isAvailable);
        return dto;
    }
}
