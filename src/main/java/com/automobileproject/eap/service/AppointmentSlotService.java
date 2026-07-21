package com.automobileproject.eap.service;

import com.automobileproject.eap.dto.request.AppointmentSlotRequestDTO;
import com.automobileproject.eap.dto.response.AppointmentSlotResponseDTO;
import com.automobileproject.eap.entity.AppointmentSlot;
import com.automobileproject.eap.entity.SESSION_PERIOD_TYPES;

import java.time.LocalDate;
import java.util.UUID;
import java.util.List;
import java.util.UUID;

public interface AppointmentSlotService {

    List<AppointmentSlotResponseDTO> getAllSlotTemplates();

    List<AppointmentSlotResponseDTO> getSlotTemplatesByShop(UUID shopId);

    AppointmentSlotResponseDTO createSlotTemplate(UUID shopId, AppointmentSlotRequestDTO dto);

    void deleteSlotTemplate(UUID slotId, UUID shopId);

    List<AppointmentSlotResponseDTO> getSlotTemplatesByPeriod(SESSION_PERIOD_TYPES period);

    List<AppointmentSlotResponseDTO> getAvailableSlotsForDate(LocalDate date);

    List<AppointmentSlotResponseDTO> getAvailableSlotsByPeriod(LocalDate date, SESSION_PERIOD_TYPES period);

    AppointmentSlot findSlotTemplate(SESSION_PERIOD_TYPES period, Integer slotNumber);

    AppointmentSlot findSlotTemplate(SESSION_PERIOD_TYPES period, Integer slotNumber, UUID shopId);

    boolean isSlotAvailable(LocalDate date, SESSION_PERIOD_TYPES period, Integer slotNumber);

    boolean isSlotAvailable(LocalDate date, SESSION_PERIOD_TYPES period, Integer slotNumber, UUID shopId);

    Long countAvailableSlots(LocalDate date);

    Long countAvailableSlotsByPeriod(LocalDate date, SESSION_PERIOD_TYPES period);

    AppointmentSlotResponseDTO createSlotTemplate(AppointmentSlotRequestDTO dto);

    void deleteSlotTemplate(UUID id);
}
