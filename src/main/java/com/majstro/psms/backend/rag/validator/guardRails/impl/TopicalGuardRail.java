package com.majstro.psms.backend.rag.validator.guardRails.impl;

import com.majstro.psms.backend.entity.Project;
import com.majstro.psms.backend.exception.GuardrailViolationException;
import com.majstro.psms.backend.rag.dataModel.RequestModel;
import com.majstro.psms.backend.rag.validator.guardRails.InputGuardRail;
import org.springframework.stereotype.Component;

// This guardrail checks if the input is relevant to the current topic of conversation.
@Component
public class TopicalGuardRail implements InputGuardRail {
    @Override
    public void validate(RequestModel input) throws GuardrailViolationException {

        Project project = input.getProject();

        input.setInstruction("this project's name is " + project.getProjectName() +
                "it involves with " + project.getDescription() +
                "if user ask anything likely outside of this project reply with" +
                " 'you are asking about something that is not related to this project," +
                " please ask about this project only'");
    }
}
