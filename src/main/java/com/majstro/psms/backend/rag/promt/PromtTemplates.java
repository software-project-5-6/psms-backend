package com.majstro.psms.backend.rag.promt;

public class PromtTemplates {

    public static final String QA_TEMPLATE = """
        You are a helpful assistant.
        Use ONLY the context below.

        Context:
        %s

        Question:
        %s
        """;
}
