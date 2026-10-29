package com.majstro.psms.backend.service.impl;

import com.majstro.psms.backend.dto.SupportRequestDto;
import com.majstro.psms.backend.dto.SupportResponseDto;
import com.majstro.psms.backend.entity.User;
import com.majstro.psms.backend.exception.EmailSendingException;
import com.majstro.psms.backend.repository.UserRepository;
import com.majstro.psms.backend.service.IEmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupportServiceImplTest {

    @Mock
    private IEmailService emailService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Jwt jwt;

    private SupportServiceImpl supportService;

    @BeforeEach
    void setUp() {
        supportService = new SupportServiceImpl(emailService, userRepository);
    }

    @Test
    void shouldSuccessfullySubmitSupportRequestWhenConfiguredRecipientProvided() {
        ReflectionTestUtils.setField(supportService, "configuredSupportRecipient", "helpdesk@majstro.com");
        ReflectionTestUtils.setField(supportService, "defaultSenderEmail", "notifications@majstro.com");

        given(jwt.getSubject()).willReturn("user-auth-123");

        User user = new User();
        user.setEmail("charuka@example.com");
        user.setFullName("Charuka User");
        user.setGlobalRole("APP_USER");
        given(userRepository.findByAuthSub("user-auth-123")).willReturn(Optional.of(user));

        SupportRequestDto request = new SupportRequestDto(
                "Technical Issue",
                "Cannot load dashboard",
                "The dashboard hangs on loading projects for several minutes.",
                "Charuka User",
                "charuka@example.com"
        );

        SupportResponseDto response = supportService.submitSupportRequest(request, jwt);

        assertThat(response).isNotNull();
        assertThat(response.success()).isTrue();
        assertThat(response.ticketId()).startsWith("SUP-");
        assertThat(response.message()).contains(response.ticketId());

        verify(emailService, times(1)).sendHtmlEmailWithReplyTo(
                eq("helpdesk@majstro.com"),
                eq("charuka@example.com"),
                eq("Charuka User"),
                contains("[Technical Issue] Cannot load dashboard"),
                contains("Ticket Information")
        );
    }

    @Test
    void shouldFallbackToSendGridFromEmailWhenRecipientNotConfigured() {
        // Neither SUPPORT_RECIPIENT_EMAIL nor support.recipient.email set; fallback to sendgrid.from
        ReflectionTestUtils.setField(supportService, "configuredSupportRecipient", "");
        ReflectionTestUtils.setField(supportService, "defaultSenderEmail", "verified-sender@majstro.com");

        given(jwt.getSubject()).willReturn("user-sub-fallback");
        given(userRepository.findByAuthSub("user-sub-fallback")).willReturn(Optional.empty());

        SupportRequestDto request = new SupportRequestDto(
                "Feature Request",
                "Dark mode contrast",
                "Please improve dark mode text contrast on low brightness screens.",
                "Pavan",
                "pavan@example.com"
        );

        SupportResponseDto response = supportService.submitSupportRequest(request, jwt);

        assertThat(response.success()).isTrue();
        verify(emailService, times(1)).sendHtmlEmailWithReplyTo(
                eq("verified-sender@majstro.com"),
                eq("pavan@example.com"),
                anyString(),
                contains("[Feature Request]"),
                anyString()
        );
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenNoRecipientConfigured() {
        ReflectionTestUtils.setField(supportService, "configuredSupportRecipient", "   ");
        ReflectionTestUtils.setField(supportService, "defaultSenderEmail", null);

        given(jwt.getSubject()).willReturn("user-no-config");

        SupportRequestDto request = new SupportRequestDto(
                "Other",
                "General question",
                "How do I export project space documents to PDF?",
                "User",
                "user@example.com"
        );

        assertThatThrownBy(() -> supportService.submitSupportRequest(request, jwt))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Support recipient email is not configured");

        verifyNoInteractions(emailService);
    }

    @Test
    void shouldThrowEmailSendingExceptionWhenDeliveryFails() {
        ReflectionTestUtils.setField(supportService, "configuredSupportRecipient", "support@majstro.com");
        ReflectionTestUtils.setField(supportService, "defaultSenderEmail", "sender@majstro.com");

        given(jwt.getSubject()).willReturn("user-fail");
        given(userRepository.findByAuthSub("user-fail")).willReturn(Optional.empty());

        doThrow(new EmailSendingException("SendGrid API error (403): Sender Identity unverified"))
                .when(emailService)
                .sendHtmlEmailWithReplyTo(anyString(), anyString(), anyString(), anyString(), anyString());

        SupportRequestDto request = new SupportRequestDto(
                "Technical Issue",
                "Login issue",
                "Receiving unexpected session timeout when opening chat.",
                "Tester",
                "tester@example.com"
        );

        assertThatThrownBy(() -> supportService.submitSupportRequest(request, jwt))
                .isInstanceOf(EmailSendingException.class)
                .hasMessageContaining("Sender Identity unverified");
    }

    @Test
    void shouldEnforceRateLimitingAfterFiveRequests() {
        ReflectionTestUtils.setField(supportService, "configuredSupportRecipient", "support@majstro.com");
        ReflectionTestUtils.setField(supportService, "defaultSenderEmail", "sender@majstro.com");

        String sub = "spammer-sub";
        given(jwt.getSubject()).willReturn(sub);
        given(userRepository.findByAuthSub(sub)).willReturn(Optional.empty());

        SupportRequestDto request = new SupportRequestDto(
                "Technical Issue",
                "Repeated request",
                "Testing rate limiting with consecutive calls.",
                "Tester",
                "tester@example.com"
        );

        for (int i = 0; i < 5; i++) {
            supportService.submitSupportRequest(request, jwt);
        }

        assertThatThrownBy(() -> supportService.submitSupportRequest(request, jwt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Too many support requests submitted");
    }
}
