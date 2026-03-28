package com.majstro.psms.backend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ConversationMessages {

    public String conversationId;
    public List<OutputChatMessage> messages;


    public ConversationMessages(String conversationId, List<OutputChatMessage> messages) {
        this.conversationId = conversationId;
        this.messages = messages;
    }
}
