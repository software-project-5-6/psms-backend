package com.majstro.psms.backend.rag.validator.guardRails;

import com.majstro.psms.backend.exception.GuardrailViolationException;
import com.majstro.psms.backend.rag.dataModel.RequestModel;

public interface InputGuardRail {

    void validate(RequestModel input) throws GuardrailViolationException;

}
