package com.majstro.psms.backend.exception;

/**
 * Exception thrown for invitation-related validation errors
 */
public class InvalidInvitationException extends RuntimeException {
    public InvalidInvitationException(String message) {
        super(message);
    }
}
