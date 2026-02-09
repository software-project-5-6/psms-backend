package com.majstro.psms.backend.exception;

/**
 * Exception thrown when a user attempts an action they don't have permission for
 */
public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
