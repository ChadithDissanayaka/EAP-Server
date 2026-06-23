package com.automobileproject.eap.service;

import com.automobileproject.eap.dto.request.ServiceCategoryRequestDTO;
import com.automobileproject.eap.dto.response.ServiceCategoryResponseDTO;

import java.util.List;
import java.util.UUID;

public interface ServiceCategoryService {

    ServiceCategoryResponseDTO createCategory(ServiceCategoryRequestDTO dto);

    ServiceCategoryResponseDTO updateCategory(UUID id, ServiceCategoryRequestDTO dto);

    void deleteCategory(UUID id);

    List<ServiceCategoryResponseDTO> getAllCategories();

    ServiceCategoryResponseDTO getCategoryById(UUID id);
}
