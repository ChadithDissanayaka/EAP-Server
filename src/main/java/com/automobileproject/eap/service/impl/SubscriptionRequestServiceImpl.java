package com.automobileproject.eap.service.impl;

import com.automobileproject.eap.dto.request.SubscriptionRequestCreateDTO;
import com.automobileproject.eap.dto.request.SubscriptionRequestReviewDTO;
import com.automobileproject.eap.dto.response.SubscriptionRequestResponseDTO;
import com.automobileproject.eap.entity.Shop;
import com.automobileproject.eap.entity.SubscriptionRequest;
import com.automobileproject.eap.entity.User;
import com.automobileproject.eap.exception.EntryNotFoundException;
import com.automobileproject.eap.exception.ValidationException;
import com.automobileproject.eap.repo.ShopRepo;
import com.automobileproject.eap.repo.SubscriptionRequestRepo;
import com.automobileproject.eap.repo.UserRepo;
import com.automobileproject.eap.service.NotificationService;
import com.automobileproject.eap.service.SubscriptionRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionRequestServiceImpl implements SubscriptionRequestService {

    private final SubscriptionRequestRepo requestRepo;
    private final ShopRepo shopRepo;
    private final UserRepo userRepo;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public SubscriptionRequestResponseDTO createRequest(UUID shopId, UUID userId, SubscriptionRequestCreateDTO dto) {
        Shop shop = shopRepo.findById(shopId)
                .orElseThrow(() -> new EntryNotFoundException("Shop not found with ID: " + shopId));
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new EntryNotFoundException("User not found with ID: " + userId));

        if (shop.getSubscriptionPlan() == dto.getRequestedPlan()) {
            throw new ValidationException("Shop is already on the " + dto.getRequestedPlan() + " plan.");
        }

        SubscriptionRequest request = SubscriptionRequest.builder()
                .shop(shop)
                .requestedBy(user)
                .currentPlan(shop.getSubscriptionPlan())
                .requestedPlan(dto.getRequestedPlan())
                .status("PENDING")
                .message(dto.getMessage())
                .build();

        SubscriptionRequest saved = requestRepo.save(request);
        log.info("Created subscription plan request for shop {} to move to {}", shop.getName(), dto.getRequestedPlan());

        // Notify shop owner confirmation
        notificationService.sendToAdmins(shopId, "SYSTEM_ALERT", saved.getId().toString(),
                "Subscription request to move to " + dto.getRequestedPlan() + " submitted to SaaS Administrator.");

        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionRequestResponseDTO> getRequestsByShop(UUID shopId) {
        return requestRepo.findByShopIdOrderByCreatedAtDesc(shopId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionRequestResponseDTO> getAllRequests() {
        return requestRepo.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SubscriptionRequestResponseDTO reviewRequest(UUID requestId, SubscriptionRequestReviewDTO dto) {
        SubscriptionRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new EntryNotFoundException("Subscription request not found with ID: " + requestId));

        String newStatus = dto.getStatus().toUpperCase();
        if (!"APPROVED".equals(newStatus) && !"REJECTED".equals(newStatus)) {
            throw new ValidationException("Status must be APPROVED or REJECTED");
        }

        request.setStatus(newStatus);
        request.setAdminNote(dto.getAdminNote());

        if ("APPROVED".equals(newStatus)) {
            Shop shop = request.getShop();
            shop.setSubscriptionPlan(request.getRequestedPlan());
            shopRepo.save(shop);
            log.info("Approved subscription change for shop {}. New plan: {}", shop.getName(), shop.getSubscriptionPlan());

            notificationService.sendToAdmins(shop.getId(), "SYSTEM_ALERT", requestId.toString(),
                    "Your subscription change request to " + request.getRequestedPlan() + " has been APPROVED!");
        } else {
            log.info("Rejected subscription change for shop {}", request.getShop().getName());

            notificationService.sendToAdmins(request.getShop().getId(), "SYSTEM_ALERT", requestId.toString(),
                    "Your subscription change request to " + request.getRequestedPlan() + " was reviewed.");
        }

        SubscriptionRequest updated = requestRepo.save(request);
        return mapToDTO(updated);
    }

    private SubscriptionRequestResponseDTO mapToDTO(SubscriptionRequest req) {
        return SubscriptionRequestResponseDTO.builder()
                .id(req.getId())
                .shopId(req.getShop().getId())
                .shopName(req.getShop().getName())
                .requestedById(req.getRequestedBy().getId())
                .requestedByName(req.getRequestedBy().getFirstName() + " " + req.getRequestedBy().getLastName())
                .requestedByEmail(req.getRequestedBy().getEmail())
                .currentPlan(req.getCurrentPlan().name())
                .requestedPlan(req.getRequestedPlan().name())
                .status(req.getStatus())
                .message(req.getMessage())
                .adminNote(req.getAdminNote())
                .createdAt(req.getCreatedAt())
                .updatedAt(req.getUpdatedAt())
                .build();
    }
}
