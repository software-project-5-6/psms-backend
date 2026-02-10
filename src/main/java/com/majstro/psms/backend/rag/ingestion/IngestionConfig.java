package com.majstro.psms.backend.rag.ingestion;

import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IngestionConfig {

    @Bean
    public TokenTextSplitter splitter(){
        return new TokenTextSplitter();
    }
}
