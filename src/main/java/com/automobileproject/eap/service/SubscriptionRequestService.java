package com.automobileproject.eap.service;

import com.automobileproject.eap.dto.request.SubscriptionRequestCreateDTO;
import com.automobileproject.eap.dto.request.SubscriptionRequestReviewDTO;
import com.automobileproject.eap.dto.response.SubscriptionRequestResponseDTO;

import java.util.List;
import java.util.UUID;

public interface SubscriptionRequestService {
    SubscriptionRequestResponseDTO createRequest(UUID shopId, UUID userId, SubscriptionRequestCreateDTO dto);
    List<SubscriptionRequestResponseDTO> getRequestsByShop(UUID shopId);
    List<SubscriptionRequestResponseDTO> getAllRequests();
    SubscriptionRequestResponseDTO reviewRequest(UUID requestId, SubscriptionRequestReviewDTO dto);
}
