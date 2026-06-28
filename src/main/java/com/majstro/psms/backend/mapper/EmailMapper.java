package com.majstro.psms.backend.mapper;

import com.majstro.psms.backend.dto.EmailDto;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
public class EmailMapper {
    public EmailDto mapGmailMessageToEmailDto(Map<String, Object> gmailMessage) {
        String to = "";
        String subject = "";
        String body = "";
        String from = "";
        // Extract headers
        Map<String, Object> payload = (Map<String, Object>) gmailMessage.get("payload");
        if (payload != null) {
            List<Map<String, String>> headers = (List<Map<String, String>>) payload.get("headers");
            if (headers != null) {
                for (Map<String, String> header : headers) {
                    String name = header.get("name");
                    if ("To".equalsIgnoreCase(name)) {
                        to = header.getOrDefault("value", "");
                    } else if ("Subject".equalsIgnoreCase(name)) {
                        subject = header.getOrDefault("value", "");
                    } else if ("From".equalsIgnoreCase(name)) {
                        from = header.getOrDefault("value", "");
                    }

                }
            }
            // Extract body (plain text preferred)
            body = extractBody(payload);
        }
        return new EmailDto(to, subject, body, from);
    }

    private String extractBody(Map<String, Object> payload) {
        // Try to get plain text body
        String body = extractBodyFromPart(payload, "text/plain");
        if (body.isEmpty()) {
            // Fallback to HTML
            body = extractBodyFromPart(payload, "text/html");
        }
        return body;
    }

    private String extractBodyFromPart(Map<String, Object> part, String mimeType) {
        if (mimeType.equals(part.get("mimeType"))) {
            Map<String, Object> bodyObj = (Map<String, Object>) part.get("body");
            if (bodyObj != null && bodyObj.containsKey("data")) {
                return decodeBase64Url((String) bodyObj.get("data"));
            }
        }
        // Check parts recursively
        List<Map<String, Object>> parts = (List<Map<String, Object>>) part.get("parts");
        if (parts != null) {
            for (Map<String, Object> subPart : parts) {
                String result = extractBodyFromPart(subPart, mimeType);
                if (!result.isEmpty()) return result;
            }
        }
        return "";
    }

    private String decodeBase64Url(String encoded) {
        if (encoded == null) return "";
        byte[] decodedBytes = Base64.getUrlDecoder().decode(encoded);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }
}
