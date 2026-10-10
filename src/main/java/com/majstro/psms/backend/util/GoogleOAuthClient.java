package com.majstro.psms.backend.util;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;


import java.util.Map;

@Component
public class GoogleOAuthClient {
    private final WebClient webClient;

    public GoogleOAuthClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://oauth2.googleapis.com").build();
    }

    public Map<String, Object> exchangeCodeForTokens(String code, String clientId, String clientSecret, String redirectUri) {
        try {
            return this.webClient.post()
                    .uri("/token")
                    .body(BodyInserters.fromFormData("code", code)
                            .with("client_id", clientId)
                            .with("client_secret", clientSecret)
                            .with("redirect_uri", redirectUri)
                            .with("grant_type", "authorization_code"))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Failed to exchange code for tokens: " + e.getResponseBodyAsString(), e);
        }
    }

    /**
     * Exchanges a stored refresh token for a new access token once the previous one has expired.
     */
    public Map<String, Object> refreshAccessToken(String refreshToken, String clientId, String clientSecret) {
        try {
            return this.webClient.post()
                    .uri("/token")
                    .body(BodyInserters.fromFormData("refresh_token", refreshToken)
                            .with("client_id", clientId)
                            .with("client_secret", clientSecret)
                            .with("grant_type", "refresh_token"))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Failed to refresh Google access token: " + e.getResponseBodyAsString(), e);
        }
    }
}

