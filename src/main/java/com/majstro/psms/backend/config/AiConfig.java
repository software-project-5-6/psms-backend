package com.majstro.psms.backend.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AiConfig {

    //llm model bean based on the properties defined in application.properties
    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.create(chatModel);
    }


    //text splitter bean that we will use to split the documents into chunks
    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        return new TokenTextSplitter();
    }
}

