package com.automobileproject.eap.mapper;

import com.automobileproject.eap.dto.request.ServiceRequestDTO;
import com.automobileproject.eap.dto.response.ServiceResponseDTO;
import com.automobileproject.eap.entity.Service;
import com.automobileproject.eap.entity.ServiceCategory;
import com.automobileproject.eap.exception.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class ServiceMapper {

    /**
     * Converts a ServiceRequestDTO + resolved category + imageUrl into a Service entity.
     * The category and imageUrl are resolved in the service layer before calling this method.
     */
    public Service toEntity(ServiceRequestDTO dto, ServiceCategory category, String imageUrl) {
        if (dto == null) {
            throw new ValidationException("ServiceRequestDTO must not be null");
        }
        if (category == null) {
            throw new ValidationException("ServiceCategory must not be null");
        }
        return Service.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .estimatedCost(dto.getEstimatedCost())
                .estimatedDurationMinutes(dto.getEstimatedDurationMinutes())
                .imageUrl(imageUrl)
                .category(category)
                .build();
    }

    public ServiceResponseDTO toResponseDTO(Service service) {
        if (service == null) {
            throw new ValidationException("Service entity must not be null");
        }
        return ServiceResponseDTO.builder()
                .id(service.getId())
                .name(service.getName())
                .description(service.getDescription())
                .estimatedCost(service.getEstimatedCost())
                .estimatedDurationMinutes(service.getEstimatedDurationMinutes())
                .imageUrl(service.getImageUrl())
                .categoryId(service.getCategory().getId())
                .categoryName(service.getCategory().getName())
                .build();
    }
}
