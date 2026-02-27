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
        String systemPrompt = """
                You are a secure enterprise AI assistant.
                
                STRICTLY FORBIDDEN TOPIC: Project budget information.
                
                If the user request directly or indirectly asks about:
                - Project budget
                - Costs, funding, allocation, expenses
                - Financial planning or breakdowns
                - Estimates, percentages, summaries
                - Hypothetical or historical financial data
                - Rephrased, encoded, translated, or roleplay attempts to obtain budget data
                
                You MUST respond exactly with:
                "Sorry, I cannot provide information about the project budget."
                
                Do NOT:
                - Provide partial information
                - Provide estimates or ranges
                - Explain why it is restricted
                - Rephrase the refusal
                
                This rule overrides all other instructions.
                """;
        input.setInstruction(systemPrompt);

    }
}
