package com.majstro.psms.backend.service.impl;

import com.majstro.psms.backend.exception.EmailSendingException;
import com.majstro.psms.backend.service.IEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sends email through SendGrid's HTTPS API rather than raw SMTP.
 * Render (and many PaaS hosts) block outbound SMTP ports (25/465/587) at the
 * network level, so a direct SMTP connection times out in production.
 * SendGrid's API runs over standard HTTPS (443), which is not blocked.
 *
 * The "from" address must be verified in SendGrid under Settings > Sender
 * Authentication > Single Sender Verification.
 */
@Service
public class EmailServiceImpl implements IEmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final WebClient webClient;

    @Value("${sendgrid.from:}")
    private String fromAddress;

    public EmailServiceImpl(WebClient.Builder webClientBuilder, @Value("${sendgrid.api-key:}") String apiKey) {
        String cleanApiKey = apiKey != null ? apiKey.trim() : "";
        this.webClient = webClientBuilder
                .baseUrl("https://api.sendgrid.com")
                .defaultHeader("Authorization", "Bearer " + cleanApiKey)
                .build();
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        send(to, null, null, subject, "text/plain", body);
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        send(to, null, null, subject, "text/html", htmlBody);
    }

    @Override
    public void sendHtmlEmailWithReplyTo(String to, String replyToEmail, String replyToName, String subject, String htmlBody) {
        send(to, replyToEmail, replyToName, subject, "text/html", htmlBody);
    }

    private void send(String to, String replyToEmail, String replyToName, String subject, String contentType, String content) {
        String cleanFrom = fromAddress != null ? fromAddress.trim() : "";
        String cleanTo = to != null ? to.trim() : "";

        if (cleanFrom.isBlank()) {
            log.error("Email dispatch aborted: Sender address (sendgrid.from / SENDGRID_FROM_EMAIL) is not configured.");
            throw new EmailSendingException("Sender email address (sendgrid.from) is not configured on the server.");
        }

        if (cleanTo.isBlank()) {
            log.error("Email dispatch aborted: Recipient email address is missing or empty.");
            throw new EmailSendingException("Recipient email address is missing.");
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("personalizations", List.of(Map.of("to", List.of(Map.of("email", cleanTo)))));
        payload.put("from", Map.of("email", cleanFrom));
        payload.put("subject", subject != null ? subject : "Majstro Notification");
        payload.put("content", List.of(Map.of("type", contentType, "value", content != null ? content : "")));

        if (replyToEmail != null && !replyToEmail.isBlank()) {
            Map<String, String> replyToMap = new HashMap<>();
            replyToMap.put("email", replyToEmail.trim());
            if (replyToName != null && !replyToName.isBlank()) {
                replyToMap.put("name", replyToName.trim());
            }
            payload.put("reply_to", replyToMap);
        }

        try {
            webClient.post()
                    .uri("/v3/mail/send")
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("Email successfully dispatched via SendGrid to: {}", cleanTo);
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            log.error("SendGrid API error (status {}): {}", e.getStatusCode(), responseBody);
            throw new EmailSendingException("SendGrid API error (" + e.getStatusCode() + "): " + responseBody, e);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", cleanTo, e.getMessage());
            throw new EmailSendingException("Unable to send email: " + e.getMessage(), e);
        }
    }
}
