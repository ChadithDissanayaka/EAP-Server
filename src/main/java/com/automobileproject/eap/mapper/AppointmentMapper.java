package com.automobileproject.eap.mapper;

import com.automobileproject.eap.dto.response.AppointmentResponseDTO;
import com.automobileproject.eap.dto.response.ServiceResponseDTO;
import com.automobileproject.eap.entity.Appointment;
import com.automobileproject.eap.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class AppointmentMapper {

    private final ServiceMapper serviceMapper;

    public AppointmentMapper(ServiceMapper serviceMapper) {
        this.serviceMapper = serviceMapper;
    }

    public AppointmentResponseDTO toResponseDTO(Appointment appointment) {
        if (appointment == null) {
            throw new ValidationException("Appointment entity must not be null");
        }

        UUID vehicleId = null;
        String vehicleModel = null;
        String vehicleLicensePlate = null;
        UUID customerId = null;
        String customerFirstName = null;
        String customerLastName = null;
        String customerEmail = null;

        if (appointment.getVehicle() != null) {
            vehicleId = appointment.getVehicle().getId();
            vehicleModel = appointment.getVehicle().getModel();
            vehicleLicensePlate = appointment.getVehicle().getLicensePlate();
            if (appointment.getVehicle().getOwner() != null) {
                customerId = appointment.getVehicle().getOwner().getId();
                customerFirstName = appointment.getVehicle().getOwner().getFirstName();
                customerLastName = appointment.getVehicle().getOwner().getLastName();
                customerEmail = appointment.getVehicle().getOwner().getEmail();
            }
        }

        UUID serviceId = null;
        String serviceName = null;
        com.automobileproject.eap.entity.Service primaryService = appointment.getPrimaryService();
        if (primaryService != null) {
            serviceId = primaryService.getId();
            serviceName = primaryService.getName();
        }

        Set<ServiceResponseDTO> services = appointment.getServices() == null ? Set.of() :
                appointment.getServices().stream()
                        .map(serviceMapper::toResponseDTO)
                        .collect(Collectors.toSet());

        UUID slotId = null;
        String slotDescription = null;
        if (appointment.getAppointmentSlot() != null) {
            slotId = appointment.getAppointmentSlot().getId();
            slotDescription = appointment.getAppointmentSlot().getSlotDescription();
        }

        List<AppointmentResponseDTO.AssignedEmployeeDTO> assignedEmployees =
                appointment.getAssignedEmployees() == null ? List.of() :
                        appointment.getAssignedEmployees().stream()
                                .map(emp -> AppointmentResponseDTO.AssignedEmployeeDTO.builder()
                                        .id(emp.getId())
                                        .firstName(emp.getFirstName())
                                        .lastName(emp.getLastName())
                                        .email(emp.getEmail())
                                        .build())
                                .collect(Collectors.toList());

        return AppointmentResponseDTO.builder()
                .id(appointment.getId())
                .appointmentDateTime(appointment.getAppointmentDateTime())
                .status(appointment.getStatus())
                .appointmentType(appointment.getAppointmentType())
                .customerNotes(appointment.getCustomerNotes())
                .technicianNotes(appointment.getTechnicianNotes())
                .createdAt(appointment.getCreatedAt())
                .quotePrice(appointment.getQuotePrice())
                .quoteDetails(appointment.getQuoteDetails())
                .quoteApproved(appointment.getQuoteApproved())
                .vehicleId(vehicleId)
                .vehicleModel(vehicleModel)
                .vehicleLicensePlate(vehicleLicensePlate)
                .customerId(customerId)
                .customerFirstName(customerFirstName)
                .customerLastName(customerLastName)
                .customerEmail(customerEmail)
                .serviceId(serviceId)
                .serviceName(serviceName)
                .services(services)
                .slotId(slotId)
                .slotDescription(slotDescription)
                .assignedEmployees(assignedEmployees)
                .build();
    }
}
