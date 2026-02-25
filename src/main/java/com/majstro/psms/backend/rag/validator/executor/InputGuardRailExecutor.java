package com.majstro.psms.backend.rag.validator.executor;

import com.majstro.psms.backend.rag.validator.guardRails.InputGuardRail;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InputGuardRailExecutor {

    private final List<InputGuardRail> guardRails;

    public InputGuardRailExecutor(List<InputGuardRail> guardRails) {
        this.guardRails = guardRails;
    }

    public void execute(String input) {
        for (InputGuardRail guardRail : guardRails) {
            guardRail.validate(input);
        }
    }
}
