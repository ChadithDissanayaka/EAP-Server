package com.automobileproject.eap.service.impl;

import com.automobileproject.eap.dto.request.ModificationRequestDTO;
import com.automobileproject.eap.dto.request.SlotBasedAppointmentRequestDTO;
import com.automobileproject.eap.dto.request.StandardAppointmentRequestDTO;
import com.automobileproject.eap.dto.response.AppointmentResponseDTO;
import com.automobileproject.eap.dto.response.AppointmentSlotResponseDTO;
import com.automobileproject.eap.entity.*;
import com.automobileproject.eap.exception.EntryNotFoundException;
import com.automobileproject.eap.exception.ValidationException;
import com.automobileproject.eap.mapper.AppointmentMapper;
import com.automobileproject.eap.repo.*;
import com.automobileproject.eap.service.AppointmentService;
import com.automobileproject.eap.service.AppointmentSlotService;
import com.automobileproject.eap.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepo appointmentRepo;
    private final UserRepo userRepo;
    private final VehicleRepo vehicleRepo;
    private final ServiceRepo serviceRepo;
    private final TimeLogRepo timeLogRepo;
    private final AppointmentSlotService appointmentSlotService;
    private final AppointmentMapper appointmentMapper;
    private final NotificationService notificationService;

    private static final List<APPOINTMENT_STATUS_TYPES> ACTIVE_STATUSES = Arrays.asList(
            APPOINTMENT_STATUS_TYPES.IN_PROGRESS,
            APPOINTMENT_STATUS_TYPES.AWAITING_PARTS
    );

    @Override
    @Transactional
    public AppointmentResponseDTO createStandardAppointment(StandardAppointmentRequestDTO dto, String customerEmail) {
        User customer = findUserByEmail(customerEmail);
        Vehicle vehicle = findVehicleById(dto.getVehicleId());
        assertVehicleOwnership(vehicle, customer);
        assertVehicleNotBusy(vehicle);

        List<UUID> serviceIdsToBook = resolveServiceIds(dto.getServiceIds(), dto.getServiceId());
        Set<com.automobileproject.eap.entity.Service> selectedServices = fetchServices(serviceIdsToBook);

        Appointment appointment = Appointment.builder()
                .vehicle(vehicle)
                .services(selectedServices)
                .appointmentDateTime(dto.getAppointmentDateTime())
                .customerNotes(dto.getCustomerNotes())
                .appointmentType(APPOINTMENT_TYPE_TYPES.STANDARD_SERVICE)
                .status(APPOINTMENT_STATUS_TYPES.SCHEDULED)
                .build();

        return appointmentMapper.toResponseDTO(appointmentRepo.save(appointment));
    }

    @Override
    @Transactional
    public AppointmentResponseDTO createSlotBasedAppointment(SlotBasedAppointmentRequestDTO dto, String customerEmail) {
        log.info("Creating slot-based appointment for customer: {}", customerEmail);

        User customer = findUserByEmail(customerEmail);
        Vehicle vehicle = findVehicleById(dto.getVehicleId());
        assertVehicleOwnership(vehicle, customer);
        assertVehicleNotBusy(vehicle);

        Set<com.automobileproject.eap.entity.Service> services = fetchServices(dto.getServiceIds());

        AppointmentSlot slot = appointmentSlotService.findSlotTemplate(dto.getSessionPeriod(), dto.getSlotNumber());

        if (!appointmentSlotService.isSlotAvailable(dto.getAppointmentDate(), dto.getSessionPeriod(), dto.getSlotNumber())) {
            throw new ValidationException(String.format(
                    "The requested slot (%s Slot %d) is already booked on %s. Please choose another slot.",
                    dto.getSessionPeriod(), dto.getSlotNumber(), dto.getAppointmentDate()));
        }

        OffsetDateTime appointmentDateTime = OffsetDateTime.of(
                dto.getAppointmentDate(),
                slot.getStartTime(),
                ZoneId.systemDefault().getRules().getOffset(
                        dto.getAppointmentDate().atTime(slot.getStartTime()))
        );

        Appointment appointment = Appointment.builder()
                .vehicle(vehicle)
                .services(services)
                .appointmentDateTime(appointmentDateTime)
                .appointmentSlot(slot)
                .customerNotes(dto.getCustomerNotes())
                .appointmentType(APPOINTMENT_TYPE_TYPES.STANDARD_SERVICE)
                .status(APPOINTMENT_STATUS_TYPES.SCHEDULED)
                .build();

        return appointmentMapper.toResponseDTO(appointmentRepo.save(appointment));
    }

    @Override
    @Transactional
    public AppointmentResponseDTO createModificationRequest(ModificationRequestDTO dto, String customerEmail) {
        User customer = findUserByEmail(customerEmail);
        Vehicle vehicle = findVehicleById(dto.getVehicleId());
        assertVehicleOwnership(vehicle, customer);

        Appointment appointment = Appointment.builder()
                .vehicle(vehicle)
                .appointmentDateTime(dto.getAppointmentDateTime())
                .customerNotes(dto.getCustomerNotes())
                .appointmentType(APPOINTMENT_TYPE_TYPES.MODIFICATION_PROJECT)
                .build();

        return appointmentMapper.toResponseDTO(appointmentRepo.save(appointment));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getAllAppointments() {
        return mapList(appointmentRepo.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getAppointmentsByStatus(APPOINTMENT_STATUS_TYPES status) {
        return mapList(appointmentRepo.findByStatus(status));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getAppointmentsByCustomerEmail(String customerEmail) {
        User customer = findUserByEmail(customerEmail);
        return mapList(appointmentRepo.findByVehicle_Owner(customer));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getCustomerServiceHistory(String customerEmail) {
        User customer = findUserByEmail(customerEmail);
        return appointmentRepo.findByVehicle_Owner(customer).stream()
                .filter(a -> a.getStatus() == APPOINTMENT_STATUS_TYPES.COMPLETED)
                .map(appointmentMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getAllCompletedAppointments() {
        return mapList(appointmentRepo.findByStatus(APPOINTMENT_STATUS_TYPES.COMPLETED));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getScheduledAppointments() {
        return mapList(appointmentRepo.findByStatus(APPOINTMENT_STATUS_TYPES.SCHEDULED));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getEmployeeInProgressAppointments(String employeeEmail) {
        User employee = findUserByEmail(employeeEmail);
        List<Appointment> scheduled = appointmentRepo.findByAssignedEmployeesContainingAndStatus(
                employee, APPOINTMENT_STATUS_TYPES.SCHEDULED);
        List<Appointment> inProgress = appointmentRepo.findByAssignedEmployeesContainingAndStatus(
                employee, APPOINTMENT_STATUS_TYPES.IN_PROGRESS);
        List<Appointment> combined = new ArrayList<>(scheduled);
        combined.addAll(inProgress);
        return mapList(combined);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getEmployeeAwaitingPartsAppointments(String employeeEmail) {
        return mapList(appointmentRepo.findByAssignedEmployeesContainingAndStatus(
                findUserByEmail(employeeEmail), APPOINTMENT_STATUS_TYPES.AWAITING_PARTS));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getEmployeeCompletedAppointments(String employeeEmail) {
        return mapList(appointmentRepo.findByAssignedEmployeesContainingAndStatus(
                findUserByEmail(employeeEmail), APPOINTMENT_STATUS_TYPES.COMPLETED));
    }

    @Override
    @Transactional
    public AppointmentResponseDTO updateAppointmentStatus(UUID id, APPOINTMENT_STATUS_TYPES newStatus) {
        Appointment appointment = findAppointmentById(id);
        APPOINTMENT_STATUS_TYPES currentStatus = appointment.getStatus();

        if (currentStatus == APPOINTMENT_STATUS_TYPES.COMPLETED ||
                currentStatus == APPOINTMENT_STATUS_TYPES.CANCELLED) {
            throw new ValidationException("Cannot update status of a completed or cancelled appointment.");
        }

        if (currentStatus == APPOINTMENT_STATUS_TYPES.IN_PROGRESS &&
                (newStatus == APPOINTMENT_STATUS_TYPES.AWAITING_PARTS ||
                        newStatus == APPOINTMENT_STATUS_TYPES.COMPLETED)) {
            List<TimeLog> activeTimeLogs = timeLogRepo.findByAppointmentIdAndEndTimeIsNull(id);
            OffsetDateTime now = OffsetDateTime.now();
            for (TimeLog tl : activeTimeLogs) {
                tl.setEndTime(now);
                String suffix = newStatus == APPOINTMENT_STATUS_TYPES.AWAITING_PARTS
                        ? " - Paused: Awaiting parts" : " - Work completed";
                tl.setNotes(tl.getNotes() != null ? tl.getNotes() + suffix : suffix.trim());
                timeLogRepo.save(tl);
            }
        }

        if (currentStatus == APPOINTMENT_STATUS_TYPES.SCHEDULED &&
                newStatus == APPOINTMENT_STATUS_TYPES.IN_PROGRESS &&
                !appointment.getAssignedEmployees().isEmpty()) {
            User employee = appointment.getAssignedEmployees().iterator().next();
            TimeLog tl = TimeLog.builder()
                    .appointment(appointment)
                    .employee(employee)
                    .startTime(OffsetDateTime.now())
                    .notes("Work started on appointment")
                    .build();
            timeLogRepo.save(tl);
        }

        if (currentStatus == APPOINTMENT_STATUS_TYPES.AWAITING_PARTS &&
                newStatus == APPOINTMENT_STATUS_TYPES.IN_PROGRESS &&
                !appointment.getAssignedEmployees().isEmpty()) {
            User employee = appointment.getAssignedEmployees().iterator().next();
            TimeLog tl = TimeLog.builder()
                    .appointment(appointment)
                    .employee(employee)
                    .startTime(OffsetDateTime.now())
                    .notes("Work resumed after waiting for parts")
                    .build();
            timeLogRepo.save(tl);
        }

        appointment.setStatus(newStatus);
        Appointment saved = appointmentRepo.save(appointment);

        // Notify assigned employees about the status change
        String statusLabel = newStatus.name().replace("_", " ").toLowerCase();
        String vehicleModel = saved.getVehicle().getModel();
        notificationService.sendToEmployees(
                getAssignedEmployeeEmails(saved),
                "STATUS_UPDATED",
                saved.getId().toString(),
                "Appointment for " + vehicleModel + " is now " + statusLabel + "."
        );

        // Notify admins
        notificationService.sendToAdmins(
                "STATUS_UPDATED",
                saved.getId().toString(),
                "Appointment for " + vehicleModel + " is now " + statusLabel + "."
        );

        // Notify the appointment's customer
        notificationService.sendToCustomer(
                saved.getVehicle().getOwner().getEmail(),
                "STATUS_UPDATED",
                saved.getId().toString(),
                "Your appointment for " + vehicleModel + " has been updated to " + statusLabel + "."
        );

        return appointmentMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public AppointmentResponseDTO updateTechnicianNotes(UUID id, String notes) {
        Appointment appointment = findAppointmentById(id);
        appointment.setTechnicianNotes(notes);
        return appointmentMapper.toResponseDTO(appointmentRepo.save(appointment));
    }

    @Override
    @Transactional
    public AppointmentResponseDTO submitQuote(UUID id, Double quotePrice, String quoteDetails) {
        Appointment appointment = findAppointmentById(id);

        if (appointment.getAppointmentType() != APPOINTMENT_TYPE_TYPES.MODIFICATION_PROJECT) {
            throw new ValidationException("Quotes can only be submitted for modification projects.");
        }
        if (appointment.getStatus() != APPOINTMENT_STATUS_TYPES.QUOTE_REQUESTED) {
            throw new ValidationException("Quote can only be submitted when status is QUOTE_REQUESTED.");
        }

        appointment.setQuotePrice(quotePrice);
        appointment.setQuoteDetails(quoteDetails);
        appointment.setStatus(APPOINTMENT_STATUS_TYPES.AWAITING_CUSTOMER_APPROVAL);
        Appointment saved = appointmentRepo.save(appointment);
        notificationService.sendToCustomer(
                saved.getVehicle().getOwner().getEmail(),
                "QUOTE_SUBMITTED",
                saved.getId().toString(),
                "A new quote has been submitted for your modification request on " + saved.getVehicle().getModel() + "."
        );
        return appointmentMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public AppointmentResponseDTO assignEmployee(UUID appointmentId, UUID employeeId) {
        Appointment appointment = findAppointmentById(appointmentId);
        User employee = findUserById(employeeId);

        if (employee.getRole() != ROLE_TYPES.EMPLOYEE && employee.getRole() != ROLE_TYPES.ADMIN) {
            throw new ValidationException("Only employees or admins can be assigned to appointments.");
        }

        appointment.getAssignedEmployees().add(employee);
        Appointment saved = appointmentRepo.save(appointment);

        // Notify only the assigned employee
        notificationService.sendToEmployee(
                employee.getEmail(),
                "EMPLOYEE_ASSIGNED",
                saved.getId().toString(),
                "You have been assigned to appointment for " + saved.getVehicle().getModel() + "."
        );

        // Notify admins
        notificationService.sendToAdmins(
                "EMPLOYEE_ASSIGNED",
                saved.getId().toString(),
                employee.getFirstName() + " " + employee.getLastName() + " has been assigned to appointment for " + saved.getVehicle().getModel() + "."
        );

        return appointmentMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public AppointmentResponseDTO acceptAppointment(UUID appointmentId, String employeeEmail) {
        Appointment appointment = findAppointmentById(appointmentId);
        User employee = findUserByEmail(employeeEmail);

        if (appointment.getStatus() != APPOINTMENT_STATUS_TYPES.SCHEDULED) {
            throw new ValidationException("Only SCHEDULED appointments can be accepted.");
        }

        appointment.getAssignedEmployees().add(employee);
        appointment.setStatus(APPOINTMENT_STATUS_TYPES.IN_PROGRESS);

        TimeLog timeLog = TimeLog.builder()
                .appointment(appointment)
                .employee(employee)
                .startTime(OffsetDateTime.now())
                .notes("Work started on appointment")
                .build();
        timeLogRepo.save(timeLog);

        Appointment saved = appointmentRepo.save(appointment);

        // Notify admins
        notificationService.sendToAdmins(
                "APPOINTMENT_ACCEPTED",
                saved.getId().toString(),
                "Appointment for " + saved.getVehicle().getModel() + " has been accepted by " + employee.getFirstName() + " " + employee.getLastName() + "."
        );

        // Notify the customer
        notificationService.sendToCustomer(
                saved.getVehicle().getOwner().getEmail(),
                "APPOINTMENT_ACCEPTED",
                saved.getId().toString(),
                "Your appointment for " + saved.getVehicle().getModel() + " is now in progress."
        );

        return appointmentMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public AppointmentResponseDTO cancelAppointment(UUID appointmentId) {
        Appointment appointment = findAppointmentById(appointmentId);

        if (appointment.getStatus() == APPOINTMENT_STATUS_TYPES.COMPLETED ||
                appointment.getStatus() == APPOINTMENT_STATUS_TYPES.CANCELLED) {
            throw new ValidationException("Cannot cancel a completed or already cancelled appointment.");
        }

        appointment.setStatus(APPOINTMENT_STATUS_TYPES.CANCELLED);
        Appointment saved = appointmentRepo.save(appointment);
        // Notify the customer
        notificationService.sendToCustomer(
                saved.getVehicle().getOwner().getEmail(),
                "APPOINTMENT_CANCELLED",
                saved.getId().toString(),
                "Your appointment for " + saved.getVehicle().getModel() + " has been cancelled."
        );

        // Notify assigned employees
        notificationService.sendToEmployees(
                getAssignedEmployeeEmails(saved),
                "APPOINTMENT_CANCELLED",
                saved.getId().toString(),
                "Appointment for " + saved.getVehicle().getModel() + " has been cancelled."
        );

        // Notify admins
        notificationService.sendToAdmins(
                "APPOINTMENT_CANCELLED",
                saved.getId().toString(),
                "Appointment for " + saved.getVehicle().getModel() + " has been cancelled."
        );

        return appointmentMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentSlotResponseDTO> getAvailableSlots(LocalDate date, SESSION_PERIOD_TYPES period) {
        return appointmentSlotService.getAvailableSlotsByPeriod(date, period);
    }

    @Override
    public List<AppointmentSlotResponseDTO> getAllSlotTemplates() {
        return appointmentSlotService.getAllSlotTemplates();
    }

    @Override
    @Transactional
    public AppointmentResponseDTO rejectModificationRequest(UUID id, String rejectionReason) {
        Appointment appointment = findAppointmentById(id);
        if (appointment.getAppointmentType() != APPOINTMENT_TYPE_TYPES.MODIFICATION_PROJECT) {
            throw new ValidationException("Only modification projects can be rejected.");
        }
        if (appointment.getStatus() != APPOINTMENT_STATUS_TYPES.QUOTE_REQUESTED) {
            throw new ValidationException("Modification request can only be rejected when status is QUOTE_REQUESTED.");
        }

        appointment.setQuoteDetails(rejectionReason);
        appointment.setStatus(APPOINTMENT_STATUS_TYPES.REJECTED);
        Appointment saved = appointmentRepo.save(appointment);
        notificationService.sendToCustomer(
                saved.getVehicle().getOwner().getEmail(),
                "MODIFICATION_REJECTED",
                saved.getId().toString(),
                "Your modification request for " + saved.getVehicle().getModel() + " has been rejected."
        );
        return appointmentMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public AppointmentResponseDTO approveQuote(UUID id, String customerEmail) {
        Appointment appointment = findAppointmentById(id);
        User customer = findUserByEmail(customerEmail);

        if (!appointment.getVehicle().getOwner().getId().equals(customer.getId())) {
            throw new AccessDeniedException("You do not own this vehicle.");
        }
        if (appointment.getStatus() != APPOINTMENT_STATUS_TYPES.AWAITING_CUSTOMER_APPROVAL) {
            throw new ValidationException("Quote can only be approved when status is AWAITING_CUSTOMER_APPROVAL.");
        }

        appointment.setQuoteApproved(true);
        appointment.setStatus(APPOINTMENT_STATUS_TYPES.SCHEDULED);
        Appointment saved = appointmentRepo.save(appointment);
        // Notify assigned employees
        notificationService.sendToEmployees(
                getAssignedEmployeeEmails(saved),
                "QUOTE_APPROVED",
                saved.getId().toString(),
                "Quote approved for modification on " + saved.getVehicle().getModel() + " by " + customer.getEmail() + "."
        );

        // Notify admins
        notificationService.sendToAdmins(
                "QUOTE_APPROVED",
                saved.getId().toString(),
                "Quote approved for modification on " + saved.getVehicle().getModel() + " by " + customer.getEmail() + "."
        );

        return appointmentMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public AppointmentResponseDTO rejectQuote(UUID id, String rejectionReason, String customerEmail) {
        Appointment appointment = findAppointmentById(id);
        User customer = findUserByEmail(customerEmail);

        if (!appointment.getVehicle().getOwner().getId().equals(customer.getId())) {
            throw new AccessDeniedException("You do not own this vehicle.");
        }
        if (appointment.getStatus() != APPOINTMENT_STATUS_TYPES.AWAITING_CUSTOMER_APPROVAL) {
            throw new ValidationException("Quote can only be rejected when status is AWAITING_CUSTOMER_APPROVAL.");
        }

        appointment.setQuoteApproved(false);
        appointment.setQuoteDetails(rejectionReason);
        appointment.setStatus(APPOINTMENT_STATUS_TYPES.REJECTED);
        Appointment saved = appointmentRepo.save(appointment);
        // Notify assigned employees
        notificationService.sendToEmployees(
                getAssignedEmployeeEmails(saved),
                "QUOTE_REJECTED",
                saved.getId().toString(),
                "Quote rejected for modification on " + saved.getVehicle().getModel() + " by " + customer.getEmail() + "."
        );

        // Notify admins
        notificationService.sendToAdmins(
                "QUOTE_REJECTED",
                saved.getId().toString(),
                "Quote rejected for modification on " + saved.getVehicle().getModel() + " by " + customer.getEmail() + "."
        );

        return appointmentMapper.toResponseDTO(saved);
    }

    private User findUserByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new EntryNotFoundException("User not found with email: " + email));
    }

    private User findUserById(UUID id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new EntryNotFoundException("User not found with ID: " + id));
    }

    private Vehicle findVehicleById(UUID id) {
        return vehicleRepo.findById(id)
                .orElseThrow(() -> new EntryNotFoundException("Vehicle not found with ID: " + id));
    }

    private Appointment findAppointmentById(UUID id) {
        return appointmentRepo.findById(id)
                .orElseThrow(() -> new EntryNotFoundException("Appointment not found with ID: " + id));
    }

    private void assertVehicleOwnership(Vehicle vehicle, User customer) {
        if (!vehicle.getOwner().getId().equals(customer.getId())) {
            throw new AccessDeniedException("You do not own this vehicle.");
        }
    }

    private void assertVehicleNotBusy(Vehicle vehicle) {
        if (appointmentRepo.existsByVehicleIdAndStatusIn(vehicle.getId(), ACTIVE_STATUSES)) {
            throw new ValidationException("This vehicle is already in the garage for another service.");
        }
    }

    private List<UUID> resolveServiceIds(List<UUID> serviceIds, UUID singleServiceId) {
        if (serviceIds != null && !serviceIds.isEmpty()) return serviceIds;
        if (singleServiceId != null) return List.of(singleServiceId);
        throw new ValidationException("At least one service must be selected.");
    }

    private Set<com.automobileproject.eap.entity.Service> fetchServices(List<UUID> serviceIds) {
        Set<com.automobileproject.eap.entity.Service> services = new HashSet<>();
        for (UUID sid : serviceIds) {
            services.add(serviceRepo.findById(sid)
                    .orElseThrow(() -> new EntryNotFoundException("Service not found with ID: " + sid)));
        }
        return services;
    }

    private List<AppointmentResponseDTO> mapList(List<Appointment> appointments) {
        return appointments.stream()
                .map(appointmentMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    private List<String> getAssignedEmployeeEmails(Appointment appointment) {
        return appointment.getAssignedEmployees().stream()
                .map(User::getEmail)
                .collect(Collectors.toList());
    }
}