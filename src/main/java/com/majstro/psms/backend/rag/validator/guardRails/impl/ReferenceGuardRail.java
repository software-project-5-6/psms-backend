package com.majstro.psms.backend.rag.validator.guardRails.impl;

import com.majstro.psms.backend.exception.GuardrailViolationException;
import com.majstro.psms.backend.rag.dataModel.RequestModel;
import com.majstro.psms.backend.rag.validator.guardRails.InputGuardRail;
import org.springframework.stereotype.Component;

@Component
public class ReferenceGuardRail implements InputGuardRail {

    @Override
    public void validate(RequestModel input) throws GuardrailViolationException {
        input.setInstruction("with the response to the user question,include section called 'references' with minimal context" +
                "details, that taken from user provided context");
    }
}
