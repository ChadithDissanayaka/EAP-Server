package com.automobileproject.eap.service.impl;

import com.automobileproject.eap.dto.request.ServiceCategoryRequestDTO;
import com.automobileproject.eap.dto.response.ServiceCategoryResponseDTO;
import com.automobileproject.eap.entity.ServiceCategory;
import com.automobileproject.eap.exception.DuplicateEntryException;
import com.automobileproject.eap.exception.EntryNotFoundException;
import com.automobileproject.eap.mapper.ServiceCategoryMapper;
import com.automobileproject.eap.repo.ServiceCategoryRepo;
import com.automobileproject.eap.service.ServiceCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceCategoryServiceImpl implements ServiceCategoryService {

    private final ServiceCategoryRepo serviceCategoryRepo;
    private final ServiceCategoryMapper serviceCategoryMapper;

    @Override
    public ServiceCategoryResponseDTO createCategory(ServiceCategoryRequestDTO dto) {
        if (serviceCategoryRepo.existsByName(dto.getName())) {
            throw new DuplicateEntryException("Service category already exists: " + dto.getName());
        }
        ServiceCategory saved = serviceCategoryRepo.save(serviceCategoryMapper.toEntity(dto));
        log.info("Service category created: {}", saved.getId());
        return serviceCategoryMapper.toResponseDTO(saved);
    }

    @Override
    public ServiceCategoryResponseDTO updateCategory(UUID id, ServiceCategoryRequestDTO dto) {
        ServiceCategory category = serviceCategoryRepo.findById(id)
                .orElseThrow(() -> new EntryNotFoundException("Service category not found with ID: " + id));

        // Check duplicate name only if name is changing
        if (!category.getName().equals(dto.getName()) && serviceCategoryRepo.existsByName(dto.getName())) {
            throw new DuplicateEntryException("Service category already exists: " + dto.getName());
        }

        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        ServiceCategory updated = serviceCategoryRepo.save(category);
        log.info("Service category updated: {}", updated.getId());
        return serviceCategoryMapper.toResponseDTO(updated);
    }

    @Override
    public void deleteCategory(UUID id) {
        if (!serviceCategoryRepo.existsById(id)) {
            throw new EntryNotFoundException("Service category not found with ID: " + id);
        }
        serviceCategoryRepo.deleteById(id);
        log.info("Service category deleted: {}", id);
    }

    @Override
    public List<ServiceCategoryResponseDTO> getAllCategories() {
        return serviceCategoryRepo.findAll()
                .stream()
                .map(serviceCategoryMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ServiceCategoryResponseDTO getCategoryById(UUID id) {
        ServiceCategory category = serviceCategoryRepo.findById(id)
                .orElseThrow(() -> new EntryNotFoundException("Service category not found with ID: " + id));
        return serviceCategoryMapper.toResponseDTO(category);
    }
}
