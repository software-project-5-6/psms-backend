package com.majstro.psms.backend.rag.validator.executor;

import com.majstro.psms.backend.rag.dataModel.RequestModel;
import com.majstro.psms.backend.rag.validator.guardRails.InputGuardRail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InputGuardRailExecutor {

    private final List<InputGuardRail> guardRails;

    public RequestModel execute(RequestModel input) {
        for (InputGuardRail guardRail : guardRails) {
            guardRail.validate(input);
        }
        return input;
    }
}
