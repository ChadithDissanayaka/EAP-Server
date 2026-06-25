package com.automobileproject.eap.repo;

import com.automobileproject.eap.entity.APPOINTMENT_STATUS_TYPES;
import com.automobileproject.eap.entity.Appointment;
import com.automobileproject.eap.entity.SESSION_PERIOD_TYPES;
import com.automobileproject.eap.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepo extends JpaRepository<Appointment, UUID> {

    boolean existsByVehicleIdAndStatusIn(UUID vehicleId, List<APPOINTMENT_STATUS_TYPES> statuses);

    List<Appointment> findByStatus(APPOINTMENT_STATUS_TYPES status);

    List<Appointment> findByVehicle_Owner(User owner);

    List<Appointment> findByAssignedEmployeesContainingAndStatus(User employee, APPOINTMENT_STATUS_TYPES status);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
            "FROM Appointment a " +
            "WHERE a.appointmentSlot.id = :slotId " +
            "AND CAST(a.appointmentDateTime AS date) = :date " +
            "AND a.status != 'CANCELLED'")
    boolean isSlotBookedOnDate(@Param("slotId") UUID slotId, @Param("date") LocalDate date);

    @Query("SELECT a FROM Appointment a " +
            "WHERE a.appointmentSlot.id = :slotId " +
            "AND CAST(a.appointmentDateTime AS date) = :date " +
            "AND a.status != 'CANCELLED'")
    List<Appointment> findBySlotIdAndDate(@Param("slotId") UUID slotId, @Param("date") LocalDate date);

    @Query("SELECT a FROM Appointment a " +
            "WHERE CAST(a.appointmentDateTime AS date) = :date " +
            "AND a.status != 'CANCELLED'")
    List<Appointment> findByDate(@Param("date") LocalDate date);

    @Query("SELECT a FROM Appointment a " +
            "LEFT JOIN FETCH a.appointmentSlot slot " +
            "WHERE CAST(a.appointmentDateTime AS date) = :date " +
            "AND slot IS NOT NULL " +
            "AND slot.sessionPeriod = :period " +
            "AND a.status != 'CANCELLED'")
    List<Appointment> findByAppointmentDateAndSessionPeriod(
            @Param("date") LocalDate date,
            @Param("period") SESSION_PERIOD_TYPES period
    );
}
