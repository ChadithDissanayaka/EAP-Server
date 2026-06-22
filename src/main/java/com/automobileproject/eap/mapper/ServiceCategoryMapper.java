package com.automobileproject.eap.mapper;

import com.automobileproject.eap.dto.request.ServiceCategoryRequestDTO;
import com.automobileproject.eap.dto.response.ServiceCategoryResponseDTO;
import com.automobileproject.eap.entity.ServiceCategory;
import com.automobileproject.eap.exception.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class ServiceCategoryMapper {

    public ServiceCategory toEntity(ServiceCategoryRequestDTO dto) {
        if (dto == null) {
            throw new ValidationException("ServiceCategoryRequestDTO must not be null");
        }
        return ServiceCategory.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
    }

    public ServiceCategoryResponseDTO toResponseDTO(ServiceCategory category) {
        if (category == null) {
            throw new ValidationException("ServiceCategory entity must not be null");
        }
        return ServiceCategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }
}
