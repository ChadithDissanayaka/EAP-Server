package com.automobileproject.eap.service.impl;

import com.automobileproject.eap.dto.response.AppointmentSlotResponseDTO;
import com.automobileproject.eap.entity.AppointmentSlot;
import com.automobileproject.eap.entity.SESSION_PERIOD_TYPES;
import com.automobileproject.eap.exception.EntryNotFoundException;
import com.automobileproject.eap.exception.ValidationException;
import com.automobileproject.eap.mapper.AppointmentSlotMapper;
import com.automobileproject.eap.repo.AppointmentRepo;
import com.automobileproject.eap.repo.AppointmentSlotRepo;
import com.automobileproject.eap.service.AppointmentSlotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentSlotServiceImpl implements AppointmentSlotService {

    private final AppointmentSlotRepo appointmentSlotRepo;
    private final AppointmentRepo appointmentRepo;
    private final AppointmentSlotMapper appointmentSlotMapper;

    @Override
    public List<AppointmentSlotResponseDTO> getAllSlotTemplates() {
        return appointmentSlotRepo.findAll()
                .stream()
                .map(appointmentSlotMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentSlotResponseDTO> getSlotTemplatesByPeriod(SESSION_PERIOD_TYPES period) {
        return appointmentSlotRepo.findBySessionPeriod(period)
                .stream()
                .map(appointmentSlotMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentSlotResponseDTO> getAvailableSlotsForDate(LocalDate date) {
        return appointmentSlotRepo.findAll()
                .stream()
                .map(slot -> appointmentSlotMapper.toResponseDTO(
                        slot,
                        !appointmentRepo.isSlotBookedOnDate(slot.getId(), date)
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentSlotResponseDTO> getAvailableSlotsByPeriod(LocalDate date, SESSION_PERIOD_TYPES period) {
        return appointmentSlotRepo.findBySessionPeriod(period)
                .stream()
                .map(slot -> appointmentSlotMapper.toResponseDTO(
                        slot,
                        !appointmentRepo.isSlotBookedOnDate(slot.getId(), date)
                ))
                .collect(Collectors.toList());
    }

    @Override
    public AppointmentSlot findSlotTemplate(SESSION_PERIOD_TYPES period, Integer slotNumber) {
        if (slotNumber < 1 || slotNumber > 5) {
            throw new ValidationException("Slot number must be between 1 and 5");
        }
        return appointmentSlotRepo.findBySessionPeriodAndSlotNumber(period, slotNumber)
                .orElseThrow(() -> new EntryNotFoundException(
                        String.format("Slot template not found: %s Slot %d", period, slotNumber)));
    }

    @Override
    public boolean isSlotAvailable(LocalDate date, SESSION_PERIOD_TYPES period, Integer slotNumber) {
        AppointmentSlot slot = findSlotTemplate(period, slotNumber);
        return !appointmentRepo.isSlotBookedOnDate(slot.getId(), date);
    }

    @Override
    public Long countAvailableSlots(LocalDate date) {
        return appointmentSlotRepo.findAll()
                .stream()
                .filter(slot -> !appointmentRepo.isSlotBookedOnDate(slot.getId(), date))
                .count();
    }

    @Override
    public Long countAvailableSlotsByPeriod(LocalDate date, SESSION_PERIOD_TYPES period) {
        return appointmentSlotRepo.findBySessionPeriod(period)
                .stream()
                .filter(slot -> !appointmentRepo.isSlotBookedOnDate(slot.getId(), date))
                .count();
    }
}
