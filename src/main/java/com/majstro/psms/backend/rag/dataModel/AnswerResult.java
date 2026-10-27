package com.majstro.psms.backend.rag.dataModel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class AnswerResult {

    private String prompt;
    private String answer;

}
