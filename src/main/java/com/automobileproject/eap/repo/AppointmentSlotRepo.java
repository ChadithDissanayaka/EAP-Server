package com.automobileproject.eap.repo;

import com.automobileproject.eap.entity.AppointmentSlot;
import com.automobileproject.eap.entity.SESSION_PERIOD_TYPES;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentSlotRepo extends JpaRepository<AppointmentSlot, UUID> {

    List<AppointmentSlot> findBySessionPeriod(SESSION_PERIOD_TYPES sessionPeriod);

    Optional<AppointmentSlot> findBySessionPeriodAndSlotNumber(SESSION_PERIOD_TYPES sessionPeriod, Integer slotNumber);

    boolean existsBySessionPeriodAndSlotNumber(SESSION_PERIOD_TYPES sessionPeriod, Integer slotNumber);

    List<AppointmentSlot> findByShopId(UUID shopId);

    boolean existsBySessionPeriodAndSlotNumberAndShopId(SESSION_PERIOD_TYPES sessionPeriod, Integer slotNumber, UUID shopId);
}
