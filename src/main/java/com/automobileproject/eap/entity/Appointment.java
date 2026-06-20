package com.automobileproject.eap.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@ToString(exclude = {"services", "assignedEmployees", "appointmentSlot"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private OffsetDateTime appointmentDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private APPOINTMENT_STATUS_TYPES status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private APPOINTMENT_TYPE_TYPES appointmentType;

    @Column(columnDefinition = "TEXT")
    private String customerNotes;

    @Column(columnDefinition = "TEXT")
    private String technicianNotes;

    private OffsetDateTime createdAt;

    // Quote fields (modification projects only)
    private Double quotePrice;
    private String quoteDetails;
    private Boolean quoteApproved;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    // Primary service (kept for backward compatibility)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "service_id", nullable = true)
    private Service service;

    // Multiple services support
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "appointment_services",
            joinColumns = @JoinColumn(name = "appointment_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    @Builder.Default
    private Set<Service> services = new HashSet<>();

    // Booked slot reference (slot-based bookings only)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = true)
    private AppointmentSlot appointmentSlot;

    // Assigned employees (many-to-many)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "appointment_assignments",
            joinColumns = @JoinColumn(name = "appointment_id"),
            inverseJoinColumns = @JoinColumn(name = "employee_id")
    )
    @Builder.Default
    private Set<User> assignedEmployees = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        if (status == null) {
            status = (appointmentType == APPOINTMENT_TYPE_TYPES.STANDARD_SERVICE)
                    ? APPOINTMENT_STATUS_TYPES.SCHEDULED
                    : APPOINTMENT_STATUS_TYPES.QUOTE_REQUESTED;
        }
    }
}
