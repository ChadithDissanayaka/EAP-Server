package com.automobileproject.eap.repo;

import com.automobileproject.eap.entity.SubscriptionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubscriptionRequestRepo extends JpaRepository<SubscriptionRequest, UUID> {
    List<SubscriptionRequest> findByShopIdOrderByCreatedAtDesc(UUID shopId);
    List<SubscriptionRequest> findAllByOrderByCreatedAtDesc();
}
