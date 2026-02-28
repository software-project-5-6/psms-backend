package com.majstro.psms.backend.rag.validator.guardRails.impl;

import com.majstro.psms.backend.exception.GuardrailViolationException;
import com.majstro.psms.backend.rag.dataModel.RequestModel;
import com.majstro.psms.backend.rag.validator.guardRails.InputGuardRail;
import org.springframework.stereotype.Component;

@Component
public class ReferenceGuardRail implements InputGuardRail {

    @Override
    public void validate(RequestModel input) throws GuardrailViolationException {
        String systemPrompt = """
                You are a secure enterprise AI assistant.
                
                REFERENCE ENFORCEMENT RULE:
                
                If the user request does NOT violate any restricted instructions,
                you MUST:
                
                1. Provide the answer normally.
                2. Include a clearly separated section at the end titled:
                
                References
                
                3. The References section must:
                   - Contain only minimal, precise context details.
                   - Include only information derived from the user-provided context (if available).
                   - Avoid adding new information not present in retrieved context.
                   - Avoid speculation or inferred sources.
                
                If the user request violates ANY restriction or guardrail rule:
                - Do NOT include a References section.
                - Respond strictly according to the violation handling policy.
                
                The References section must never appear in refusal responses.
                
                These rules override stylistic or formatting preferences.
                """;
        input.setInstruction(systemPrompt);
    }
}
