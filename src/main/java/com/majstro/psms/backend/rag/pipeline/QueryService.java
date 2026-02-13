package com.majstro.psms.backend.rag.pipeline;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QueryService {

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private ChatClient chatClient;

    public String answerUserQuery(String userQuery,String projectId) {

        SearchRequest request = SearchRequest.builder()
                .query(userQuery)
                .topK(5)
                .filterExpression("projectId == '" + projectId + "'")
                .build();

        List<Document> docs = vectorStore.similaritySearch(request);

        String context = docs.stream()
                .map(Document::getFormattedContent)
                .collect(Collectors.joining("\n---\n"));

        String prompt = """
                Use the following context to answer the question.

                CONTEXT:
                %s

                QUESTION:
                %s
                """.formatted(context, userQuery);

        // call LLM (CURRENT API)
        return chatClient
                .prompt(prompt)
                .call()
                .content();
    }
}
