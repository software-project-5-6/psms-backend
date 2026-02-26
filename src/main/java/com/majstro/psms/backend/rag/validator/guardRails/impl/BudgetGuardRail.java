package com.majstro.psms.backend.rag.validator.guardRails.impl;

import com.majstro.psms.backend.entity.Project;
import com.majstro.psms.backend.exception.GuardrailViolationException;
import com.majstro.psms.backend.rag.dataModel.RequestModel;
import com.majstro.psms.backend.rag.validator.guardRails.InputGuardRail;
import org.springframework.stereotype.Component;

//this prevent user getting data about project budget
@Component
public class BudgetGuardRail implements InputGuardRail {
    @Override
    public void validate(RequestModel input) throws GuardrailViolationException {

        Project project = input.getProject();
        input.setInstruction("if user ask anything about project budget, answer with 'Sorry, I cannot provide information about the project budget.'");

    }
}
