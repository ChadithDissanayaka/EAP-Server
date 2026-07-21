package com.automobileproject.eap.service.impl;

import com.automobileproject.eap.dto.request.AppointmentSlotRequestDTO;
import com.automobileproject.eap.dto.response.AppointmentSlotResponseDTO;
import com.automobileproject.eap.entity.AppointmentSlot;
import com.automobileproject.eap.entity.SESSION_PERIOD_TYPES;
import com.automobileproject.eap.exception.DuplicateEntryException;
import com.automobileproject.eap.exception.EntryNotFoundException;
import com.automobileproject.eap.exception.ValidationException;
import com.automobileproject.eap.mapper.AppointmentSlotMapper;
import com.automobileproject.eap.repo.AppointmentRepo;
import com.automobileproject.eap.repo.AppointmentSlotRepo;
import com.automobileproject.eap.service.AppointmentSlotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
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

    @Override
    @Transactional
    public AppointmentSlotResponseDTO createSlotTemplate(AppointmentSlotRequestDTO dto) {
        log.info("Creating new slot template: {} Slot {}", dto.getSessionPeriod(), dto.getSlotNumber());

        if (appointmentSlotRepo.findBySessionPeriodAndSlotNumber(dto.getSessionPeriod(), dto.getSlotNumber()).isPresent()) {
            throw new DuplicateEntryException(
                    String.format("Slot template already exists for %s Slot %d", dto.getSessionPeriod(), dto.getSlotNumber()));
        }

        AppointmentSlot slot = AppointmentSlot.builder()
                .sessionPeriod(dto.getSessionPeriod())
                .slotNumber(dto.getSlotNumber())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .build();

        AppointmentSlot savedSlot = appointmentSlotRepo.save(slot);
        log.info("Slot template created successfully with ID: {}", savedSlot.getId());

        return appointmentSlotMapper.toResponseDTO(savedSlot);
    }

    @Override
    @Transactional
    public void deleteSlotTemplate(UUID id) {
        log.info("Deleting slot template: {}", id);
        AppointmentSlot slot = appointmentSlotRepo.findById(id)
                .orElseThrow(() -> new EntryNotFoundException("Slot template not found with ID: " + id));

        appointmentSlotRepo.delete(slot);
        log.info("Slot template deleted successfully");
    }
}
