package com.majstro.psms.backend.rag.pipeline;

import com.majstro.psms.backend.entity.Project;
import com.majstro.psms.backend.rag.dataModel.RequestModel;
import com.majstro.psms.backend.rag.validator.executor.InputGuardRailExecutor;
import com.majstro.psms.backend.service.IProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QueryService {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    private final IProjectService projectService;
    private final InputGuardRailExecutor inputGuardRailExecutor;

    public String answerUserQuery(String userQuery, String projectId) {

        Project project = projectService.getProjectEntityById(projectId);


        SearchRequest request = SearchRequest.builder()
                .query(userQuery)
                .topK(5)
                .filterExpression("projectId == '" + projectId + "'")
                .build();

        List<Document> docs = vectorStore.similaritySearch(request);

        String context = docs.stream()
                .map(Document::getFormattedContent)
                .collect(Collectors.joining("\n---\n"));

        var requestModel = new RequestModel(context, userQuery, project);
        var guardedRequest = inputGuardRailExecutor.execute(requestModel);
        String prompt = guardedRequest.buildPrompt();

        // call LLM (CURRENT API)
        return chatClient
                .prompt(prompt)
                .call()
                .content();
    }
}
