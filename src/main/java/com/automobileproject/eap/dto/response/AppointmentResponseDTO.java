package com.automobileproject.eap.dto.response;

import com.automobileproject.eap.entity.APPOINTMENT_STATUS_TYPES;
import com.automobileproject.eap.entity.APPOINTMENT_TYPE_TYPES;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AppointmentResponseDTO {

    private UUID id;
    private OffsetDateTime appointmentDateTime;
    private APPOINTMENT_STATUS_TYPES status;
    private APPOINTMENT_TYPE_TYPES appointmentType;
    private String customerNotes;
    private String technicianNotes;
    private OffsetDateTime createdAt;

    // Quote fields
    private Double quotePrice;
    private String quoteDetails;
    private Boolean quoteApproved;

    // Vehicle info (flattened)
    private UUID vehicleId;
    private String vehicleModel;
    private String vehicleLicensePlate;

    // Vehicle owner info (flattened)
    private UUID customerId;
    private String customerFirstName;
    private String customerLastName;
    private String customerEmail;

    // Primary service (flattened)
    private UUID serviceId;
    private String serviceName;

    // All services
    private Set<ServiceResponseDTO> services;

    // Slot info (flattened — null for non-slot-based bookings)
    private UUID slotId;
    private String slotDescription;

    // Assigned employees (summary list)
    private List<AssignedEmployeeDTO> assignedEmployees;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class AssignedEmployeeDTO {
        private UUID id;
        private String firstName;
        private String lastName;
        private String email;
    }
}
