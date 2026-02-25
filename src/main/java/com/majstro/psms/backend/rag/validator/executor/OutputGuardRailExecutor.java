package com.majstro.psms.backend.rag.validator.executor;

import com.majstro.psms.backend.rag.validator.guardRails.InputGuardRail;
import com.majstro.psms.backend.rag.validator.guardRails.OutputGuardRail;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutputGuardRailExecutor {

    private final List<OutputGuardRail> guardRails;

    public OutputGuardRailExecutor(List<OutputGuardRail> guardRails) {
        this.guardRails = guardRails;
    }

    public void execute(String input) {
        for (OutputGuardRail guardRail : guardRails) {
            guardRail.validate(input);
        }
    }
}
