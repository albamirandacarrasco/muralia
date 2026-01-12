package com.muralia.service.mapper;

import com.muralia.api.model.Image;
import com.muralia.entity.ImageEntity;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class ImageMapper {

    public Image toDto(ImageEntity entity) {
        Image dto = new Image();
        dto.setId(entity.getId());
        dto.setUrl(URI.create(entity.getUrl()));
        if (entity.getThumbnailUrl() != null) {
            dto.setThumbnailUrl(URI.create(entity.getThumbnailUrl()));
        }
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setFileName(entity.getFileName());
        dto.setFileSize(entity.getFileSize());
        dto.setMimeType(entity.getMimeType());
        dto.setWidth(entity.getWidth());
        dto.setHeight(entity.getHeight());
        dto.setCustomerId(entity.getCustomer().getId());
        dto.setCustomerUsername(entity.getCustomer().getUsername());
        dto.setUploadedAt(entity.getUploadedAt());
        return dto;
    }
}
