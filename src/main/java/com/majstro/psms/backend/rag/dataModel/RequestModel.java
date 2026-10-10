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
        StringBuilder prompt = new StringBuilder();

        prompt.append(
                "PROJECT SPACE MANAGEMENT ASSISTANT\n"
                        + "You are a friendly, helpful assistant inside a project space management system. "
                        + "Your job is to help the user with this project — its tasks, artifacts, team, status, "
                        + "and documents — using the project details and the context from documents and chat "
                        + "history provided below.\n"
                        + "Feel free to respond naturally to greetings, thanks, and clarifying small talk.\n"
                        + "If the user asks about something clearly unrelated to this project or project "
                        + "management in general, let them know warmly that it's outside what you can help "
                        + "with here, and steer the conversation back to what you can do for their project. "
                        + "Keep the tone friendly and never scold or lecture the user.\n\n"
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
