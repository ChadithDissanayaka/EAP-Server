package com.automobileproject.eap.service;

import com.automobileproject.eap.dto.request.ModificationRequestDTO;
import com.automobileproject.eap.dto.request.SlotBasedAppointmentRequestDTO;
import com.automobileproject.eap.dto.request.StandardAppointmentRequestDTO;
import com.automobileproject.eap.dto.response.AppointmentResponseDTO;
import com.automobileproject.eap.dto.response.AppointmentSlotResponseDTO;
import com.automobileproject.eap.entity.APPOINTMENT_STATUS_TYPES;
import com.automobileproject.eap.entity.SESSION_PERIOD_TYPES;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AppointmentService {

    AppointmentResponseDTO createStandardAppointment(StandardAppointmentRequestDTO dto, String customerEmail);

    AppointmentResponseDTO createSlotBasedAppointment(SlotBasedAppointmentRequestDTO dto, String customerEmail);

    AppointmentResponseDTO createModificationRequest(ModificationRequestDTO dto, String customerEmail);

    List<AppointmentResponseDTO> getAllAppointments();

    List<AppointmentResponseDTO> getAppointmentsByStatus(APPOINTMENT_STATUS_TYPES status);

    List<AppointmentResponseDTO> getAppointmentsByCustomerEmail(String customerEmail);

    List<AppointmentResponseDTO> getCustomerServiceHistory(String customerEmail);

    List<AppointmentResponseDTO> getAllCompletedAppointments();

    List<AppointmentResponseDTO> getScheduledAppointments();

    List<AppointmentResponseDTO> getEmployeeInProgressAppointments(String employeeEmail);

    List<AppointmentResponseDTO> getEmployeeAwaitingPartsAppointments(String employeeEmail);

    List<AppointmentResponseDTO> getEmployeeCompletedAppointments(String employeeEmail);

    AppointmentResponseDTO updateAppointmentStatus(UUID id, APPOINTMENT_STATUS_TYPES newStatus);

    AppointmentResponseDTO updateTechnicianNotes(UUID id, String notes);

    AppointmentResponseDTO submitQuote(UUID id, Double quotePrice, String quoteDetails);

    AppointmentResponseDTO assignEmployee(UUID appointmentId, UUID employeeId);

    AppointmentResponseDTO acceptAppointment(UUID appointmentId, String employeeEmail);

    AppointmentResponseDTO cancelAppointment(UUID appointmentId);

    AppointmentResponseDTO rejectModificationRequest(UUID id, String rejectionReason);

    AppointmentResponseDTO approveQuote(UUID id, String customerEmail);

    AppointmentResponseDTO rejectQuote(UUID id, String rejectionReason, String customerEmail);

    List<AppointmentSlotResponseDTO> getAvailableSlots(LocalDate date, SESSION_PERIOD_TYPES period);

    List<AppointmentSlotResponseDTO> getAllSlotTemplates();
}
