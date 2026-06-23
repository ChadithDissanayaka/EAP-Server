package com.automobileproject.eap.service.impl;

import com.automobileproject.eap.dto.request.ServiceRequestDTO;
import com.automobileproject.eap.dto.response.ServiceResponseDTO;
import com.automobileproject.eap.entity.Service;
import com.automobileproject.eap.entity.ServiceCategory;
import com.automobileproject.eap.exception.DuplicateEntryException;
import com.automobileproject.eap.exception.EntryNotFoundException;
import com.automobileproject.eap.mapper.ServiceMapper;
import com.automobileproject.eap.repo.ServiceCategoryRepo;
import com.automobileproject.eap.repo.ServiceRepo;
import com.automobileproject.eap.service.AutoService;
import com.automobileproject.eap.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
public class AutoServiceImpl implements AutoService {

    private final ServiceRepo serviceRepo;
    private final ServiceCategoryRepo serviceCategoryRepo;
    private final CloudinaryService cloudinaryService;
    private final ServiceMapper serviceMapper;

    @Override
    public ServiceResponseDTO createService(ServiceRequestDTO dto, MultipartFile imageFile) throws IOException {
        ServiceCategory category = serviceCategoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() -> new EntryNotFoundException("Service category not found with ID: " + dto.getCategoryId()));

        if (serviceRepo.existsByName(dto.getName())) {
            throw new DuplicateEntryException("Service already exists with name: " + dto.getName());
        }

        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = cloudinaryService.uploadImage(imageFile, "services");
        }

        Service service = serviceMapper.toEntity(dto, category, imageUrl);
        Service saved = serviceRepo.save(service);
        log.info("Service created: {}", saved.getId());
        return serviceMapper.toResponseDTO(saved);
    }

    @Override
    public ServiceResponseDTO updateService(UUID id, ServiceRequestDTO dto, MultipartFile imageFile) throws IOException {
        Service service = serviceRepo.findById(id)
                .orElseThrow(() -> new EntryNotFoundException("Service not found with ID: " + id));

        ServiceCategory category = serviceCategoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() -> new EntryNotFoundException("Service category not found with ID: " + dto.getCategoryId()));

        // Check duplicate name only if changing
        if (!service.getName().equals(dto.getName()) && serviceRepo.existsByName(dto.getName())) {
            throw new DuplicateEntryException("Service already exists with name: " + dto.getName());
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            String newImageUrl = cloudinaryService.updateImage(imageFile, service.getImageUrl(), "services");
            service.setImageUrl(newImageUrl);
        }

        service.setName(dto.getName());
        service.setDescription(dto.getDescription());
        service.setEstimatedCost(dto.getEstimatedCost());
        service.setEstimatedDurationMinutes(dto.getEstimatedDurationMinutes());
        service.setCategory(category);

        Service updated = serviceRepo.save(service);
        log.info("Service updated: {}", updated.getId());
        return serviceMapper.toResponseDTO(updated);
    }

    @Override
    public void deleteService(UUID id) throws IOException {
        Service service = serviceRepo.findById(id)
                .orElseThrow(() -> new EntryNotFoundException("Service not found with ID: " + id));

        if (service.getImageUrl() != null) {
            try {
                cloudinaryService.deleteImage(service.getImageUrl());
            } catch (Exception e) {
                log.warn("Failed to delete service image from Cloudinary: {}", e.getMessage());
            }
        }

        serviceRepo.deleteById(id);
        log.info("Service deleted: {}", id);
    }

    @Override
    public List<ServiceResponseDTO> getAllServices() {
        return serviceRepo.findAll()
                .stream()
                .map(serviceMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ServiceResponseDTO> getServicesByCategory(UUID categoryId) {
        return serviceRepo.findByCategoryId(categoryId)
                .stream()
                .map(serviceMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ServiceResponseDTO getServiceById(UUID id) {
        Service service = serviceRepo.findById(id)
                .orElseThrow(() -> new EntryNotFoundException("Service not found with ID: " + id));
        return serviceMapper.toResponseDTO(service);
    }
}
