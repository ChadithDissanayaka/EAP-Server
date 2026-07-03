package com.automobileproject.eap.service.impl;

import com.automobileproject.eap.exception.ValidationException;
import com.automobileproject.eap.service.CloudinaryService;
import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("File is empty or null");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ValidationException("File must be an image");
        }

        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "folder", folder,
                        "resource_type", "image",
                        "transformation", new Transformation<>()
                                .width(800).height(600).crop("limit")
                                .quality("auto").fetchFormat("auto")
                ));

        return uploadResult.get("secure_url").toString();
    }

    @Override
    public void deleteImage(String imageUrl) throws IOException {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }
        String publicId = extractPublicIdFromUrl(imageUrl);
        if (publicId != null) {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        }
    }

    @Override
    public String updateImage(MultipartFile newFile, String oldImageUrl, String folder) throws IOException {
        if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
            try {
                deleteImage(oldImageUrl);
            } catch (Exception e) {
                log.warn("Failed to delete old image during update: {}", e.getMessage());
            }
        }
        return uploadImage(newFile, folder);
    }

    private String extractPublicIdFromUrl(String imageUrl) {
        try {
            String[] parts = imageUrl.split("/upload/");
            if (parts.length > 1) {
                String afterUpload = parts[1].replaceFirst("v\\d+/", "");
                int lastDot = afterUpload.lastIndexOf('.');
                return lastDot > 0 ? afterUpload.substring(0, lastDot) : afterUpload;
            }
        } catch (Exception e) {
            log.warn("Could not extract public ID from Cloudinary URL: {}", imageUrl);
        }
        return null;
    }
}
