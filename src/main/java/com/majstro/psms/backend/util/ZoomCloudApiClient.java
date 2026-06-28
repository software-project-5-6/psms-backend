package com.majstro.psms.backend.util;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ZoomCloudApiClient {
    private final WebClient webClient;

    public ZoomCloudApiClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.zoom.us/v2").build();
    }

    /**
     * Fetches all transcript segments of a Zoom meeting using the meeting ID and JWT access token.
     *
     * @param meetingId   The Zoom meeting ID.
     * @param accessToken The JWT access token (Bearer token).
     * @return List of transcript data objects, or empty list if none found.
     */
    public List<Map<String, Object>> fetchMeetingTranscripts(String meetingId, String accessToken) {
        List<Map<String, Object>> transcripts = new ArrayList<>();
        try {
            Map<String, Object> recordingsResponse = this.webClient.get()
                    .uri("/meetings/" + meetingId + "/recordings")
                    .headers(headers -> headers.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            if (recordingsResponse == null || !recordingsResponse.containsKey("recording_files")) return transcripts;
            var files = (java.util.List<Map<String, Object>>) recordingsResponse.get("recording_files");
            for (Map<String, Object> file : files) {
                if ("TRANSCRIPT".equals(file.get("file_type"))) {
                    String downloadUrl = (String) file.get("download_url");
                    Map<String, Object> transcript = this.webClient.get()
                            .uri(uriBuilder -> uriBuilder
                                    .path(downloadUrl.replace("https://api.zoom.us/v2", ""))
                                    .queryParam("access_token", accessToken)
                                    .build())
                            .retrieve()
                            .bodyToMono(Map.class)
                            .block();
                    if (transcript != null) {
                        transcripts.add(transcript);
                    }
                }
            }
            return transcripts;
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Failed to fetch meeting transcripts: " + e.getResponseBodyAsString(), e);
        }
    }
}
