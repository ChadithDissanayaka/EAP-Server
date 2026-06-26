package com.automobileproject.eap.repo;

import com.automobileproject.eap.entity.TimeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TimeLogRepo extends JpaRepository<TimeLog, UUID> {

    @Query("SELECT tl FROM TimeLog tl " +
            "LEFT JOIN FETCH tl.appointment " +
            "LEFT JOIN FETCH tl.employee " +
            "WHERE tl.appointment.id = :appointmentId")
    List<TimeLog> findByAppointmentIdWithRelations(@Param("appointmentId") UUID appointmentId);

    List<TimeLog> findByAppointmentId(UUID appointmentId);

    List<TimeLog> findByAppointmentIdAndEndTimeIsNull(UUID appointmentId);

    Optional<TimeLog> findByAppointmentIdAndEmployeeIdAndEndTimeIsNull(UUID appointmentId, UUID employeeId);

    @Query("SELECT tl FROM TimeLog tl " +
            "LEFT JOIN FETCH tl.appointment a " +
            "LEFT JOIN FETCH a.vehicle " +
            "LEFT JOIN FETCH tl.employee " +
            "WHERE tl.employee.id = :employeeId " +
            "ORDER BY tl.startTime DESC")
    List<TimeLog> findByEmployeeIdWithRelations(@Param("employeeId") UUID employeeId);

    @Query("SELECT tl FROM TimeLog tl " +
            "LEFT JOIN FETCH tl.appointment a " +
            "LEFT JOIN FETCH a.vehicle " +
            "LEFT JOIN FETCH tl.employee " +
            "ORDER BY tl.startTime DESC")
    List<TimeLog> findAllWithRelations();
}
