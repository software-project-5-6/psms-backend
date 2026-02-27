package com.majstro.psms.backend.exception;

public class GuardrailViolationException extends RuntimeException {
    public GuardrailViolationException(String message) {
        super(message);
    }
}
