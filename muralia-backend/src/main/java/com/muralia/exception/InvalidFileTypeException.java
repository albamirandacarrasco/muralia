package com.muralia.exception;

/**
 * Exception thrown when a file is not a valid image type.
 */
public class InvalidFileTypeException extends RuntimeException {

    private final String contentType;

    public InvalidFileTypeException(String contentType) {
        super("File must be an image. Invalid content type: " + contentType);
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }
}
