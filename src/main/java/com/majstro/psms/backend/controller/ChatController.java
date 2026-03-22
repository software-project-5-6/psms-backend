package com.majstro.psms.backend.controller;


import com.majstro.psms.backend.dto.*;
import com.majstro.psms.backend.entity.Conversation;
import com.majstro.psms.backend.entity.Message;
import com.majstro.psms.backend.mapper.ChatMapper;
import com.majstro.psms.backend.rag.RagServices;
import com.majstro.psms.backend.service.IProjectService;
import com.majstro.psms.backend.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final IProjectService projectService;
    private final IUserService userService;
    private final RagServices ragServices;

    @PostMapping("/ask")
    public ResponseEntity<AskResponse> askProject(
            @RequestBody AskRequest request) {

        String answer = ragServices.query(request.getQuestion(), request.getProjectId(), request.getConversationId());
        return ResponseEntity.ok(new AskResponse(answer));
    }

    @PostMapping("/store")
    public ResponseEntity<Void> storeChatHistory(
            @RequestBody MessageStoreRequest request

    ) {
        ragServices.embbedAndStoreChat(
                request.getMessage(),
                request.getRole(), request.getUserId(),
                request.getConversationId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/conversation")
    public ResponseEntity<String> createConversation(@RequestBody NewConversationRequest request) {

        String projectId = request.getProjectId();
        String title = request.getTitle();
        String userId = userService.getCurrentUser().getId();
        String convsersationId = ragServices.createConversation(projectId, title, userId);

        return ResponseEntity.ok(convsersationId);
    }

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<ConversationMessages> getConversationMessages(@PathVariable String conversationId) {
        List<Message> messages = ragServices.getConversationMessages(conversationId);

        ConversationMessages response = ChatMapper.toConversationMessages(conversationId, messages);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/conversations/{projectId}")
    public ResponseEntity<ProjectConversations> getProjectConversations(@PathVariable String projectId) {
        List<Conversation> conversations = ragServices.getProjectConversations(projectId);

        var response = ChatMapper.toProjectConversations(projectId, conversations);
        return ResponseEntity.ok(response);
    }

}
