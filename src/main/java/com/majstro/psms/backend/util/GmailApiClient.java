package com.majstro.psms.backend.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GmailApiClient {
    private final WebClient webClient;

    public GmailApiClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://gmail.googleapis.com").build();
    }

    /**
     * Fetches up to 20 emails using Gmail API with a list of queries and access token.
     * Combines all queries into a single query string (joined by ' OR '),
     * fetches message IDs, then fetches each message's details and returns the list.
     */
    public List<Map<String, Object>> fetchEmails(String accessToken, List<String> queries) {
        if (queries == null || queries.isEmpty()) return List.of();
        String combinedQuery = String.join(" OR ", queries);
        try {
            // Step 1: Fetch message IDs for the combined query
            Map<String, Object> response = this.webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/gmail/v1/users/me/messages")
                            .queryParam("q", combinedQuery)
                            .queryParam("maxResults", 20)
                            .build())
                    .headers(headers -> headers.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            if (response == null || !response.containsKey("messages")) return List.of();
            List<Map<String, Object>> messages = (List<Map<String, Object>>) response.get("messages");
            // Step 2: Fetch each message's details
            return messages.stream()
                    .map(msg -> (String) msg.get("id"))
                    .map(id -> {
                        try {
                            return (Map<String, Object>) this.webClient.get()
                                    .uri("/gmail/v1/users/me/messages/" + id)
                                    .headers(headers -> headers.setBearerAuth(accessToken))
                                    .retrieve()
                                    .bodyToMono(Map.class)
                                    .block();
                        } catch (WebClientResponseException e) {
                            return null;
                        }
                    })
                    .filter(m -> m != null)
                    .collect(Collectors.toList());
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Failed to fetch emails: " + e.getResponseBodyAsString(), e);
        }
    }
}
