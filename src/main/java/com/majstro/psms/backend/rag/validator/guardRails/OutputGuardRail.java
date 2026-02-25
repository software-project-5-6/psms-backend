package com.majstro.psms.backend.rag.validator.guardRails;

import com.majstro.psms.backend.exception.GuardrailViolationException;

public interface OutputGuardRail {
    void validate(String input) throws GuardrailViolationException;

}
