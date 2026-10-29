package com.majstro.psms.backend.service.impl;

import com.majstro.psms.backend.dto.SupportRequestDto;
import com.majstro.psms.backend.dto.SupportResponseDto;
import com.majstro.psms.backend.entity.User;
import com.majstro.psms.backend.exception.EmailSendingException;
import com.majstro.psms.backend.repository.UserRepository;
import com.majstro.psms.backend.service.IEmailService;
import com.majstro.psms.backend.service.ISupportService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SupportServiceImpl implements ISupportService {

    private static final Logger log = LoggerFactory.getLogger(SupportServiceImpl.class);

    private final IEmailService emailService;
    private final UserRepository userRepository;

    @Value("${support.recipient.email:}")
    private String configuredSupportRecipient;

    @Value("${sendgrid.from:}")
    private String defaultSenderEmail;

    // In-memory sliding window rate limiter: sub -> list of epoch timestamps
    private final ConcurrentHashMap<String, List<Long>> rateLimitMap = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS_PER_WINDOW = 5;
    private static final long WINDOW_DURATION_MILLIS = 15 * 60 * 1000L; // 15 minutes

    @Override
    public SupportResponseDto submitSupportRequest(SupportRequestDto request, Jwt jwt) {
        String authSub = jwt != null ? jwt.getSubject() : "anonymous";

        // Rate limiting check
        checkRateLimit(authSub);

        // Resolve verified user identity
        User verifiedUser = null;
        if (jwt != null && authSub != null) {
            verifiedUser = userRepository.findByAuthSub(authSub).orElse(null);
        }

        String verifiedEmail = verifiedUser != null && verifiedUser.getEmail() != null
                ? verifiedUser.getEmail()
                : (jwt != null ? jwt.getClaimAsString("email") : null);

        String verifiedName = verifiedUser != null && verifiedUser.getFullName() != null
                ? verifiedUser.getFullName()
                : (request.name() != null && !request.name().isBlank() ? request.name().trim() : "Valued User");

        String verifiedRole = verifiedUser != null && verifiedUser.getGlobalRole() != null
                ? verifiedUser.getGlobalRole()
                : "APP_USER";

        String contactEmail = (request.email() != null && !request.email().isBlank())
                ? request.email().trim()
                : verifiedEmail;

        // Determine support recipient destination from configuration
        String recipient = resolveSupportRecipient();
        if (recipient == null || recipient.isBlank()) {
            log.error("Support request cannot be delivered: Neither support.recipient.email nor sendgrid.from is configured.");
            throw new IllegalStateException("Support recipient email is not configured on the server. Please ensure SENDGRID_FROM_EMAIL or SUPPORT_RECIPIENT_EMAIL is configured.");
        }

        // Generate Ticket ID
        String ticketId = "SUP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        LocalDateTime now = LocalDateTime.now();
        String formattedDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Sanitize user inputs for HTML email body
        String safeCategory = HtmlUtils.htmlEscape(request.category());
        String safeSubject = HtmlUtils.htmlEscape(request.subject());
        String safeMessage = HtmlUtils.htmlEscape(request.message()).replace("\n", "<br/>");
        String safeName = HtmlUtils.htmlEscape(verifiedName);
        String safeContactEmail = HtmlUtils.htmlEscape(contactEmail != null ? contactEmail : "Not provided");
        String safeVerifiedEmail = HtmlUtils.htmlEscape(verifiedEmail != null ? verifiedEmail : "Unknown");
        String safeRole = HtmlUtils.htmlEscape(verifiedRole);
        String safeSub = HtmlUtils.htmlEscape(authSub);

        String emailSubject = String.format("[Majstro PSMS Support] [%s] %s (#%s)", safeCategory, request.subject(), ticketId);

        String htmlBody = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <style>
                        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; color: #1e293b; background-color: #f8fafc; margin: 0; padding: 24px; }
                        .card { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 12px; border: 1px solid #e2e8f0; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
                        .header { background: linear-gradient(135deg, #6A11CB 0%%, #2575FC 100%%); color: #ffffff; padding: 24px; }
                        .header h2 { margin: 0 0 6px 0; font-size: 20px; font-weight: 700; }
                        .header p { margin: 0; opacity: 0.9; font-size: 13px; }
                        .ticket-badge { display: inline-block; background: rgba(255,255,255,0.2); padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; margin-top: 8px; }
                        .content { padding: 24px; }
                        .section-title { font-size: 12px; font-weight: 700; color: #64748b; text-transform: uppercase; letter-spacing: 0.5px; margin: 16px 0 8px 0; }
                        .info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; background: #f1f5f9; padding: 14px; border-radius: 8px; font-size: 13px; }
                        .info-item strong { display: block; color: #475569; font-size: 11px; text-transform: uppercase; margin-bottom: 2px; }
                        .message-box { background: #faf5ff; border: 1px solid #e9d5ff; border-left: 4px solid #6A11CB; padding: 16px; border-radius: 8px; font-size: 14px; color: #334155; margin-top: 8px; }
                        .footer { padding: 16px 24px; background: #f8fafc; border-top: 1px solid #e2e8f0; font-size: 12px; color: #94a3b8; text-align: center; }
                    </style>
                </head>
                <body>
                    <div class="card">
                        <div class="header">
                            <h2>Majstro PSMS — New Support Request</h2>
                            <p>A user submitted a support inquiry from the Majstro PSMS application.</p>
                            <span class="ticket-badge">Ticket: %s</span>
                        </div>
                        <div class="content">
                            <div class="section-title">Ticket Information</div>
                            <div class="info-grid">
                                <div class="info-item"><strong>Category</strong>%s</div>
                                <div class="info-item"><strong>Submitted At</strong>%s</div>
                                <div class="info-item"><strong>User Name</strong>%s</div>
                                <div class="info-item"><strong>Contact Email</strong>%s</div>
                                <div class="info-item"><strong>Account Email</strong>%s</div>
                                <div class="info-item"><strong>Account Role</strong>%s</div>
                            </div>
                            <div style="font-size: 11px; color: #94a3b8; margin-top: 6px;">Verified Auth Sub: <code>%s</code></div>

                            <div class="section-title" style="margin-top: 20px;">Subject</div>
                            <div style="font-size: 16px; font-weight: 600; color: #0f172a;">%s</div>

                            <div class="section-title" style="margin-top: 20px;">Description / Message</div>
                            <div class="message-box">%s</div>
                        </div>
                        <div class="footer">
                            Sent securely by Majstro PSMS Backend • Please reply directly to <a href="mailto:%s">%s</a>
                        </div>
                    </div>
                </body>
                </html>
                """,
                ticketId,
                safeCategory,
                formattedDate,
                safeName,
                safeContactEmail,
                safeVerifiedEmail,
                safeRole,
                safeSub,
                safeSubject,
                safeMessage,
                safeContactEmail,
                safeContactEmail
        );

        try {
            emailService.sendHtmlEmailWithReplyTo(recipient, contactEmail, safeName, emailSubject, htmlBody);
            log.info("Support request {} successfully delivered to {} on behalf of {}", ticketId, recipient, verifiedEmail);
        } catch (EmailSendingException e) {
            log.error("Failed to deliver support request email {}: {}", ticketId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected failure delivering support request {}: {}", ticketId, e.getMessage(), e);
            throw new EmailSendingException("Unable to deliver support request to destination: " + e.getMessage(), e);
        }

        return new SupportResponseDto(
                true,
                "Your support request has been received. Ticket reference: " + ticketId,
                ticketId,
                now
        );
    }

    private String resolveSupportRecipient() {
        if (configuredSupportRecipient != null && !configuredSupportRecipient.isBlank()) {
            return configuredSupportRecipient.trim();
        }
        if (defaultSenderEmail != null && !defaultSenderEmail.isBlank()) {
            return defaultSenderEmail.trim();
        }
        return null;
    }

    private void checkRateLimit(String sub) {
        long now = System.currentTimeMillis();
        rateLimitMap.compute(sub, (k, timestamps) -> {
            if (timestamps == null) {
                List<Long> newList = new ArrayList<>();
                newList.add(now);
                return newList;
            }
            // Remove timestamps older than window
            timestamps.removeIf(ts -> now - ts > WINDOW_DURATION_MILLIS);
            if (timestamps.size() >= MAX_REQUESTS_PER_WINDOW) {
                throw new IllegalArgumentException(
                        "Too many support requests submitted. Please wait a few minutes before submitting another request."
                );
            }
            timestamps.add(now);
            return timestamps;
        });
    }
}
