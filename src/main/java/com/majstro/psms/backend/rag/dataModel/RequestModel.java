package com.majstro.psms.backend.rag.dataModel;

import com.majstro.psms.backend.entity.Project;

import java.util.ArrayList;
import java.util.List;

public class RequestModel {

    private Project project;
    private String context;
    private String userQuery;
    private List<String> instructions = new ArrayList<>();

    public RequestModel(String context, String userQuery, Project project) {
        this.context = context;
        this.userQuery = userQuery;
        this.project = project;
    }

    public Project getProject() {
        return project;
    }

    public void setInstruction(String instruction) {
        this.instructions.add(instruction);
    }

    public String buildPrompt() {
        StringBuilder prompt = new StringBuilder();


        prompt.append("SYSTEM INSTRUCTIONS:\n");
        for (String instruction : instructions) {
            prompt.append("- ").append(instruction).append("\n");
        }

        prompt.append("\n");

        prompt.append("CONTEXT:\n");
        prompt.append(context).append("\n\n");

        prompt.append("USER QUERY:\n");
        prompt.append(userQuery);

        return prompt.toString();
    }

}
