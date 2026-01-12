package com.muralia.controller;

import com.muralia.api.ImagesApi;
import com.muralia.api.model.Image;
import com.muralia.api.model.ImageListResponse;
import com.muralia.service.ImageService;
import com.muralia.service.impl.ImageServiceImpl;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
public class ImagesController implements ImagesApi {

    private final ImageService imageService;
    private final ImageServiceImpl imageServiceImpl;

    public ImagesController(ImageService imageService, ImageServiceImpl imageServiceImpl) {
        this.imageService = imageService;
        this.imageServiceImpl = imageServiceImpl;
    }

    @Override
    public ResponseEntity<Void> _deleteImage(UUID imageId) {
        imageService.deleteImage(imageId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Image> _getImageById(UUID imageId) {
        Image image = imageService.getImageById(imageId);
        return ResponseEntity.ok(image);
    }

    @Override
    public ResponseEntity<Resource> _getImageFile(UUID imageId) {
        byte[] imageBytes = imageService.getImageFileBytes(imageId);
        String mimeType = imageServiceImpl.getImageMimeType(imageId);

        ByteArrayResource resource = new ByteArrayResource(imageBytes);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .contentLength(imageBytes.length)
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000")
                .body(resource);
    }

    @Override
    public ResponseEntity<ImageListResponse> _getLatestImages(Integer limit, Integer offset) {
        ImageListResponse response = imageService.getLatestImages(limit, offset);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Image> _uploadImage(MultipartFile file, String title, String description) {
        Image image = imageService.uploadImage(file, title, description);
        return ResponseEntity.status(HttpStatus.CREATED).body(image);
    }
}
