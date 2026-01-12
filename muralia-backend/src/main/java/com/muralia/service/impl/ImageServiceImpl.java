package com.muralia.service.impl;

import com.muralia.api.model.Image;
import com.muralia.api.model.ImageListResponse;
import com.muralia.entity.CustomerEntity;
import com.muralia.entity.ImageEntity;
import com.muralia.exception.CustomerNotFoundException;
import com.muralia.exception.EmptyFileException;
import com.muralia.exception.ImageNotFoundException;
import com.muralia.exception.InvalidFileTypeException;
import com.muralia.repository.CustomerRepository;
import com.muralia.repository.ImageRepository;
import com.muralia.service.ImageService;
import com.muralia.service.mapper.ImageMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    private final CustomerRepository customerRepository;
    private final ImageMapper imageMapper;
    // TODO: Add FileStorageService for file upload/deletion

    public ImageServiceImpl(ImageRepository imageRepository,
                            CustomerRepository customerRepository,
                            ImageMapper imageMapper) {
        this.imageRepository = imageRepository;
        this.customerRepository = customerRepository;
        this.imageMapper = imageMapper;
    }

    @Override
    @Transactional
    public Image uploadImage(MultipartFile file, String title, String description) {
        // Get current authenticated customer from SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof CustomerEntity customer)) {
            throw new CustomerNotFoundException("User not authenticated");
        }

        // Validate file
        if (file.isEmpty()) {
            throw new EmptyFileException();
        }

        // Validate file type (only images)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidFileTypeException(contentType);
        }

        // TODO: Validate file size (already configured in application.yml to 10MB max)

        try {
            // Read image bytes
            byte[] imageBytes = file.getBytes();

            // TODO: Extract image dimensions (width, height) using ImageIO
            // BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
            // Integer width = bufferedImage.getWidth();
            // Integer height = bufferedImage.getHeight();

            // TODO: Generate thumbnail
            // byte[] thumbnailBytes = generateThumbnail(imageBytes);

            // Create entity (URL will be generated after we have the ID)
            ImageEntity imageEntity = ImageEntity.builder()
                    .url("") // Will be updated after save
                    .thumbnailUrl(null) // Will be updated after save if thumbnail exists
                    .title(title)
                    .description(description)
                    .fileName(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .mimeType(contentType)
                    .width(null) // TODO: Extract actual dimensions
                    .height(null) // TODO: Extract actual dimensions
                    .imageData(imageBytes)
                    .thumbnailData(null) // TODO: Generate thumbnail
                    .customer(customer)
                    .build();

            imageEntity = imageRepository.save(imageEntity);

            // Generate URLs pointing to file serving endpoints
            String baseUrl = "http://localhost:8080"; // TODO: Get from configuration
            imageEntity.setUrl(baseUrl + "/api/images/" + imageEntity.getId() + "/file");
            if (imageEntity.getThumbnailData() != null) {
                imageEntity.setThumbnailUrl(baseUrl + "/api/images/" + imageEntity.getId() + "/thumbnail");
            }

            imageEntity = imageRepository.save(imageEntity);

            return imageMapper.toDto(imageEntity);

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image: " + e.getMessage()); // TODO: Create proper exception
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ImageListResponse getLatestImages(Integer limit, Integer offset) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<ImageEntity> page = imageRepository.findLatestImages(pageable);

        List<Image> images = page.getContent().stream()
                .map(imageMapper::toDto)
                .collect(Collectors.toList());

        ImageListResponse response = new ImageListResponse();
        response.setImages(images);
        response.setTotal((int) page.getTotalElements());
        response.setLimit(limit);
        response.setOffset(offset);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Image getImageById(UUID imageId) {
        ImageEntity imageEntity = imageRepository.findById(imageId)
                .orElseThrow(() -> new ImageNotFoundException(imageId));

        return imageMapper.toDto(imageEntity);
    }

    @Override
    @Transactional
    public void deleteImage(UUID imageId) {
        // Get current authenticated customer from SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof CustomerEntity currentCustomer)) {
            throw new CustomerNotFoundException("User not authenticated");
        }

        ImageEntity imageEntity = imageRepository.findById(imageId)
                .orElseThrow(() -> new ImageNotFoundException(imageId));

        // Check if current customer is the owner
        if (!imageEntity.getCustomer().getId().equals(currentCustomer.getId())) {
            throw new RuntimeException("Not authorized to delete this image"); // TODO: Create ForbiddenException
        }

        imageRepository.delete(imageEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getImageFileBytes(UUID imageId) {
        ImageEntity imageEntity = imageRepository.findById(imageId)
                .orElseThrow(() -> new ImageNotFoundException(imageId));

        return imageEntity.getImageData();
    }

    public String getImageMimeType(UUID imageId) {
        ImageEntity imageEntity = imageRepository.findById(imageId)
                .orElseThrow(() -> new ImageNotFoundException(imageId));

        return imageEntity.getMimeType();
    }
}
