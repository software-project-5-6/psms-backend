package com.majstro.psms.backend.rag.validator.guardRails.impl;

import com.majstro.psms.backend.exception.GuardrailViolationException;
import com.majstro.psms.backend.rag.validator.guardRails.InputGuardRail;
import org.springframework.stereotype.Component;

//this prevent user getting data about project budget
@Component
public class BudgetGuardRail implements InputGuardRail {
    @Override
    public void validate(String input) throws GuardrailViolationException {

    }
}
