package com.majstro.psms.backend.controller;


import com.majstro.psms.backend.dto.AskRequest;
import com.majstro.psms.backend.dto.AskResponse;
import com.majstro.psms.backend.rag.RagServices;
import com.majstro.psms.backend.service.IProjectService;
import com.majstro.psms.backend.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final IProjectService projectService;
    private final RagServices ragServices;

    @PostMapping("/ask")
    public ResponseEntity<AskResponse> askProject(
            @RequestBody String projectId,
            @RequestBody String conversationId,
            @RequestBody AskRequest request) {

        String answer = ragServices.query(request.getQuestion(), projectId, conversationId);
        return ResponseEntity.ok(new AskResponse(answer));
    }

    @PostMapping("/store")
    public ResponseEntity<Void> storeChatHistory(
            @RequestBody String conversationId,
            @RequestBody String message,
            @RequestBody String role,
            @RequestBody String userId
    ) {
        ragServices.embbedAndStoreChat(message, role, userId, conversationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/conversation")
    public ResponseEntity<String> createConversation(
            @RequestBody String projectId,
            @RequestBody String title,
            @RequestBody String userId) {

        String convsersationId = ragServices.createConversation(projectId, title, userId);

        return ResponseEntity.ok(convsersationId);
    }


}
