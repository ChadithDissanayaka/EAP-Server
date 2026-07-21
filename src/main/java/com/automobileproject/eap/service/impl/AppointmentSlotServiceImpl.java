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
import com.automobileproject.eap.repo.ShopRepo;
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
    private final ShopRepo shopRepo;

    @Override
    public List<AppointmentSlotResponseDTO> getAllSlotTemplates() {
        return appointmentSlotRepo.findAll()
                .stream()
                .map(appointmentSlotMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentSlotResponseDTO> getSlotTemplatesByShop(UUID shopId) {
        return appointmentSlotRepo.findByShopId(shopId)
                .stream()
                .map(appointmentSlotMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AppointmentSlotResponseDTO createSlotTemplate(UUID shopId, AppointmentSlotRequestDTO dto) {
        if (appointmentSlotRepo.existsBySessionPeriodAndSlotNumberAndShopId(dto.getSessionPeriod(), dto.getSlotNumber(), shopId)) {
            throw new ValidationException("Slot template already exists for this period and slot number in this shop");
        }

        com.automobileproject.eap.entity.Shop shop = shopRepo.findById(shopId)
                .orElseThrow(() -> new EntryNotFoundException("Shop not found: " + shopId));

        AppointmentSlot slot = AppointmentSlot.builder()
                .sessionPeriod(dto.getSessionPeriod())
                .slotNumber(dto.getSlotNumber())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .shop(shop)
                .build();

        AppointmentSlot saved = appointmentSlotRepo.save(slot);
        log.info("Created slot template: {} for shop: {}", saved.getSlotDescription(), shopId);
        return appointmentSlotMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public void deleteSlotTemplate(UUID slotId, UUID shopId) {
        AppointmentSlot slot = appointmentSlotRepo.findById(slotId)
                .orElseThrow(() -> new EntryNotFoundException("Slot template not found: " + slotId));

        if (!slot.getShop().getId().equals(shopId)) {
            throw new ValidationException("Slot template does not belong to this shop");
        }

        // Check if there are any appointments booked for this slot
        if (appointmentRepo.existsByAppointmentSlotId(slotId)) {
            throw new ValidationException("Cannot delete slot template as it has associated appointments");
        }

        appointmentSlotRepo.delete(slot);
        log.info("Deleted slot template: {} from shop: {}", slotId, shopId);
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
    public AppointmentSlot findSlotTemplate(SESSION_PERIOD_TYPES period, Integer slotNumber, UUID shopId) {
        if (slotNumber < 1 || slotNumber > 5) {
            throw new ValidationException("Slot number must be between 1 and 5");
        }
        return appointmentSlotRepo.findBySessionPeriodAndSlotNumberAndShopId(period, slotNumber, shopId)
                .orElseThrow(() -> new EntryNotFoundException(
                        String.format("Slot template not found: %s Slot %d for shop: %s", period, slotNumber, shopId)));
    }

    @Override
    public boolean isSlotAvailable(LocalDate date, SESSION_PERIOD_TYPES period, Integer slotNumber) {
        AppointmentSlot slot = findSlotTemplate(period, slotNumber);
        return !appointmentRepo.isSlotBookedOnDate(slot.getId(), date);
    }

    @Override
    public boolean isSlotAvailable(LocalDate date, SESSION_PERIOD_TYPES period, Integer slotNumber, UUID shopId) {
        AppointmentSlot slot = findSlotTemplate(period, slotNumber, shopId);
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
