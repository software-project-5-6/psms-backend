package com.majstro.psms.backend.service.impl;

import com.majstro.psms.backend.exception.EmailSendingException;
import com.majstro.psms.backend.service.IEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

/**
 * Sends email through SendGrid's HTTPS API rather than raw SMTP.
 * Render (and many PaaS hosts) block outbound SMTP ports (25/465/587) at the
 * network level, so a direct SMTP connection to smtp.gmail.com times out in
 * production even though it works fine locally. SendGrid's API runs over
 * standard HTTPS (443), which isn't blocked.
 *
 * The "from" address must be verified in SendGrid under Settings > Sender
 * Authentication > Single Sender Verification (no domain ownership needed).
 */
@Service
public class EmailServiceImpl implements IEmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final WebClient webClient;

    @Value("${sendgrid.from}")
    private String fromAddress;

    public EmailServiceImpl(WebClient.Builder webClientBuilder, @Value("${sendgrid.api-key}") String apiKey) {
        this.webClient = webClientBuilder
                .baseUrl("https://api.sendgrid.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        send(to, subject, "text/plain", body);
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        send(to, subject, "text/html", htmlBody);
    }

    private void send(String to, String subject, String contentType, String content) {
        Map<String, Object> payload = Map.of(
                "personalizations", List.of(Map.of("to", List.of(Map.of("email", to)))),
                "from", Map.of("email", fromAddress),
                "subject", subject,
                "content", List.of(Map.of("type", contentType, "value", content))
        );

        try {
            webClient.post()
                    .uri("/v3/mail/send")
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("Email sent to: {}", to);
        } catch (WebClientResponseException e) {
            log.error("Failed to send email to {}: {}", to, e.getResponseBodyAsString(), e);
            throw new EmailSendingException("Error sending email: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
            throw new EmailSendingException("Error sending email", e);
        }
    }
}
