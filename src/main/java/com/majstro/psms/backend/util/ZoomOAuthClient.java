package com.majstro.psms.backend.util;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Base64;
import java.util.Map;

@Component
public class ZoomOAuthClient {
    private final WebClient webClient;

    public ZoomOAuthClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://zoom.us").build();
    }

    public Map<String, Object> exchangeCodeForTokens(String code, String clientId, String clientSecret, String redirectUri) {
        try {
            // Zoom requires Basic Auth header with Base64 encoded clientId:clientSecret
            String credentials = clientId + ":" + clientSecret;
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

            @SuppressWarnings("unchecked")
            Map<String, Object> response = this.webClient.post()
                    .uri("/oauth/token")
                    .header("Authorization", "Basic " + encodedCredentials)
                    .body(BodyInserters.fromFormData("code", code)
                            .with("redirect_uri", redirectUri)
                            .with("grant_type", "authorization_code"))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return response;
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Failed to exchange Zoom code for tokens: " + e.getResponseBodyAsString(), e);
        }
    }
}

