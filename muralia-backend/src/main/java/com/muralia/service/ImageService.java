package com.muralia.service;

import com.muralia.api.model.Image;
import com.muralia.api.model.ImageListResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ImageService {
    Image uploadImage(MultipartFile file, String title, String description);
    ImageListResponse getLatestImages(Integer limit, Integer offset);
    Image getImageById(UUID imageId);
    void deleteImage(UUID imageId);
    byte[] getImageFileBytes(UUID imageId);
}
