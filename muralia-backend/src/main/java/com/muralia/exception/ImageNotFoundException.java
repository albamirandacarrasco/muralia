package com.muralia.exception;

import java.util.UUID;

/**
 * Exception thrown when an image is not found by ID.
 */
public class ImageNotFoundException extends RuntimeException {

    private final UUID imageId;

    public ImageNotFoundException(UUID imageId) {
        super("Image not found with id: " + imageId);
        this.imageId = imageId;
    }

    public UUID getImageId() {
        return imageId;
    }
}
