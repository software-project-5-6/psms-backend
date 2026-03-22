package com.majstro.psms.backend.mapper;

import com.majstro.psms.backend.dto.ConversationMessages;
import com.majstro.psms.backend.dto.OutputChatMessage;
import com.majstro.psms.backend.dto.OutputConversation;
import com.majstro.psms.backend.dto.ProjectConversations;
import com.majstro.psms.backend.entity.Conversation;
import com.majstro.psms.backend.entity.Message;

import java.util.List;

public class ChatMapper {
    public static ConversationMessages toConversationMessages(String conversationId, List<Message> messages) {

        List<OutputChatMessage> outputChatMessages = messages.stream()
                .map(ChatMapper::toOutputChatMessage)
                .toList();

        return new ConversationMessages(conversationId, outputChatMessages);
    }

    public static ProjectConversations toProjectConversations(String projectId, List<Conversation> conversations) {
        List<OutputConversation> outputConversations = conversations.stream()
                .map(ChatMapper::toOutputConversation)
                .toList();

        return new ProjectConversations(projectId, outputConversations);
    }

    public static OutputChatMessage toOutputChatMessage(Message message) {
        return new OutputChatMessage(message.getRole(), message.getContent());
    }

    public static OutputConversation toOutputConversation(Conversation conversation) {
        return new OutputConversation(conversation.getId().toString(), conversation.getTitle());
    }
}
