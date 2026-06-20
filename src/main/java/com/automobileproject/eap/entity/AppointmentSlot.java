package com.automobileproject.eap.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(
        name = "appointment_slots",
        uniqueConstraints = @UniqueConstraint(columnNames = {"session_period", "slot_number"})
)
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AppointmentSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "session_period")
    private SESSION_PERIOD_TYPES sessionPeriod;

    @Column(nullable = false, name = "slot_number")
    private Integer slotNumber; // 1–5

    @Column(nullable = false, name = "start_time")
    private LocalTime startTime;

    @Column(nullable = false, name = "end_time")
    private LocalTime endTime;

    // ── Static helper methods (template logic) ────────────────────────────────

    public String getSlotDescription() {
        return String.format("%s Slot %d (%s - %s)",
                sessionPeriod.name(), slotNumber,
                startTime.toString(), endTime.toString());
    }

    public static LocalTime getDefaultStartTime(SESSION_PERIOD_TYPES period, Integer slotNumber) {
        if (period == SESSION_PERIOD_TYPES.MORNING) {
            return LocalTime.of(6 + slotNumber, 0); // 7:00, 8:00, 9:00, 10:00, 11:00
        } else {
            return LocalTime.of(12 + slotNumber, 0); // 13:00, 14:00, 15:00, 16:00, 17:00
        }
    }

    public static LocalTime getDefaultEndTime(SESSION_PERIOD_TYPES period, Integer slotNumber) {
        return getDefaultStartTime(period, slotNumber).plusHours(1);
    }
}
