package com.majstro.psms.backend.rag.dataModel;

import com.majstro.psms.backend.entity.Message;
import com.majstro.psms.backend.entity.Project;

import java.util.ArrayList;
import java.util.List;

public class RequestModel {

    private final Project project;
    private final String contextFromDocuments;
    private final String contextFromChatHistory;
    private final List<String> topRecentChats;
    private final String userQuery;
    private final List<String> instructions = new ArrayList<>();


    public RequestModel(
            String contextFromDocuments,
            String contextFromChatHistory,
            String userQuery,
            Project project,
            List<String> topRecentChats) {

        this.contextFromDocuments = contextFromDocuments;
        this.contextFromChatHistory = contextFromChatHistory;
        this.userQuery = userQuery;
        this.project = project;
        this.topRecentChats = topRecentChats;
    }

    public Project getProject() {
        return project;
    }

    public void setInstruction(String instruction) {
        this.instructions.add(instruction);
    }

    public String buildPrompt() {
        // Static system instruction for project space management assistant
        final String staticInstruction = "You are a project space management assistant. Answer only questions related to managing the project's workspace. If a user asks about anything outside project space management, respond with a polite rejection indicating the query is out of scope.";
        StringBuilder prompt = new StringBuilder();

        prompt.append(
                "PROJECT SPACE MANAGEMENT ASSISTANT MAIN PROMPT\n"
                        + "you are a helpful assistance inside a project space management system.your" +
                        "main responsibility is providing answer to the user question , using " +
                        "the given details about the project and the context from documents and chat history. " +
                        "if the user question is not related to the project space management, you should politely" +
                        " reject the question and inform the user that the question is out of scope.\n\n"


        );
        prompt.append("SYSTEM INSTRUCTIONS:\n");
        for (String instruction : instructions) {
            prompt.append("- ").append(instruction).append("\n");
        }
        prompt.append("\n");


        prompt.append("DOCUMENT CONTEXT :\n");
        prompt.append(contextFromDocuments).append("\n\n");

        prompt.append("CRITICAL PAST CHAT CONTEXT :\n");
        prompt.append(contextFromChatHistory).append("\n\n");

        prompt.append("MOST RECENT CHATS:\n");
        for (String message : topRecentChats) {
            prompt.append("- ").append(message).append("\n");
        }

        prompt.append("USER QUERY:\n");
        prompt.append(userQuery);

        return prompt.toString();
    }

}
