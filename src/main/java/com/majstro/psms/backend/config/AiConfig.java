package com.majstro.psms.backend.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AiConfig {

    /**
     * Create ChatClient bean using the configured ChatModel (Ollama).
     * The ChatModel is auto-configured by Spring AI based on application.properties.
     *
     * @param chatModel The ChatModel provided by Spring AI auto-configuration
     * @return ChatClient instance for use in RAG services (QueryService)
     */
    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.create(chatModel);
    }

    /**
     * Create TokenTextSplitter bean for chunking documents.
     * Used by IngestionService to split documents into manageable chunks before embedding.
     *
     * @return TokenTextSplitter instance configured with default settings
     */
    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        return new TokenTextSplitter();
    }
}

