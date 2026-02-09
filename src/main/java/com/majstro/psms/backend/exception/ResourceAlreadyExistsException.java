package com.majstro.psms.backend.exception;

/**
 * Exception thrown when a resource already exists (duplicate)
 */
public class ResourceAlreadyExistsException extends RuntimeException {
    public ResourceAlreadyExistsException(String message) {
        super(message);
    }
}
