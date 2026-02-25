package com.majstro.psms.backend.rag.validator.guardRails.impl;

import com.majstro.psms.backend.exception.GuardrailViolationException;
import com.majstro.psms.backend.rag.validator.guardRails.OutputGuardRail;
import org.springframework.stereotype.Component;

//this check whether llm response is relevant
@Component
public class ContentValidationGuardRail implements OutputGuardRail {
    @Override
    public void validate(String input) throws GuardrailViolationException {

    }
}
