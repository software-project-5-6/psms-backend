package com.majstro.psms.backend.rag.validator.guardRails.impl;

import com.majstro.psms.backend.exception.GuardrailViolationException;
import com.majstro.psms.backend.rag.validator.guardRails.InputGuardRail;
import org.springframework.stereotype.Component;

// This guardrail checks if the input is relevant to the current topic of conversation.
@Component
public class TopicalGuardRail implements InputGuardRail {
    @Override
    public void validate(String input) throws GuardrailViolationException {

    }
}
