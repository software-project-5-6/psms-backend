package com.majstro.psms.backend.rag.pipeline;

import com.majstro.psms.backend.entity.Message;
import com.majstro.psms.backend.entity.Project;
import com.majstro.psms.backend.rag.dataModel.RequestModel;
import com.majstro.psms.backend.rag.validator.executor.InputGuardRailExecutor;
import com.majstro.psms.backend.repository.MessageRepository;
import com.majstro.psms.backend.service.IProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QueryService {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    private final IProjectService projectService;
    private final InputGuardRailExecutor inputGuardRailExecutor;
    private final MessageRepository messageRepository;

    public String answerUserQuery(String userQuery, String projectId, String conversationId) {

        Project project = projectService.getProjectEntityById(projectId);


        SearchRequest requestToKnowledgeBase = SearchRequest.builder()
                .query(userQuery)
                .topK(5)
                .filterExpression("type == 'knowledge' AND projectId == '" + projectId + "'")
                .build();

        SearchRequest requestToChatHistory = SearchRequest.builder()
                .query(userQuery)
                .topK(5)
                .filterExpression("type == 'chat' AND projectId == '" + projectId +
                        "' AND conversationId == '" + conversationId + "'")
                .build();

        //data taken from vector database related to existing documents
        List<Document> docs = vectorStore.similaritySearch(requestToKnowledgeBase);

        //data taken from vector database related to existing chat history of the conversation
        List<Document> chats = vectorStore.similaritySearch(requestToChatHistory);


        String contextFromDocuments = docs.stream()
                .map(Document::getFormattedContent)
                .collect(Collectors.joining("\n---\n"));

        String contextFromChatHistory = chats.stream()
                .map(Document::getFormattedContent)
                .collect(Collectors.joining("\n---\n"));


        Pageable pageable = PageRequest.of(0, 10);

        //find the latest 10 messages of the conversation
        List<Message> latestChatHistory = messageRepository.findByConversationIdOrderByCreatedAtDesc(
                UUID.fromString(conversationId), pageable
        );

        //because llm need the flow of old->new messages
        Collections.reverse(latestChatHistory);

        List<String> messages = latestChatHistory.stream()
                .map(m -> m.getRole() + ": " + m.getContent())
                .toList();


        var requestModel = new RequestModel(contextFromDocuments, contextFromChatHistory, userQuery, project, messages);
        var guardedRequest = inputGuardRailExecutor.execute(requestModel);
        String prompt = guardedRequest.buildPrompt();
        System.out.print(prompt);

        // call LLM (CURRENT API)
        return chatClient
                .prompt(prompt)
                .call()
                .content();
    }

    public String generateTitle(String firstMessage) {
        String safeMessage = firstMessage.replace("\"", "'");
        String prompt = String.format(
                "Generate a concise, descriptive title (3 to 6 words) for a chat conversation based on this opening message: \"%s\"\n\n" +
                "Rules:\n" +
                "- Return ONLY the title text, nothing else\n" +
                "- No quotes, no trailing punctuation\n" +
                "- Be specific and meaningful\n" +
                "- Do not start with generic words like Chat, Discussion, Help, Query, or Request",
                safeMessage
        );
        return chatClient.prompt(prompt).call().content().trim();
    }
}
