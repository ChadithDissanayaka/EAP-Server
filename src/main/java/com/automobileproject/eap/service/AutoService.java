package com.automobileproject.eap.service;

import com.automobileproject.eap.dto.request.ServiceRequestDTO;
import com.automobileproject.eap.dto.response.ServiceResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface AutoService {

    ServiceResponseDTO createService(ServiceRequestDTO dto, MultipartFile imageFile) throws IOException;

    ServiceResponseDTO updateService(UUID id, ServiceRequestDTO dto, MultipartFile imageFile) throws IOException;

    void deleteService(UUID id) throws IOException;

    List<ServiceResponseDTO> getAllServices();

    List<ServiceResponseDTO> getServicesByCategory(UUID categoryId);

    ServiceResponseDTO getServiceById(UUID id);
}
