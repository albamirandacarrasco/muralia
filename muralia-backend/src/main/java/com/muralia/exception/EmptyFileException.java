package com.muralia.exception;

/**
 * Exception thrown when an uploaded file is empty.
 */
public class EmptyFileException extends RuntimeException {

    public EmptyFileException() {
        super("File is empty. Please provide a valid file.");
    }
}
