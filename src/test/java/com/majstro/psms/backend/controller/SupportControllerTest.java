package com.majstro.psms.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.majstro.psms.backend.config.SecurityConfig;
import com.majstro.psms.backend.dto.SupportRequestDto;
import com.majstro.psms.backend.dto.SupportResponseDto;
import com.majstro.psms.backend.exception.EmailSendingException;
import com.majstro.psms.backend.exception.GlobalExceptionHandler;
import com.majstro.psms.backend.repository.UserRepository;
import com.majstro.psms.backend.service.ISupportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SupportController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class SupportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ISupportService supportService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void shouldRejectUnauthenticatedRequest() throws Exception {
        SupportRequestDto request = new SupportRequestDto(
                "Technical Issue",
                "Cannot load dashboard",
                "The dashboard hangs on loading projects for several minutes.",
                "Charuka",
                "user@example.com"
        );

        mockMvc.perform(post("/api/v1/support/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAcceptAuthenticatedValidRequest() throws Exception {
        SupportRequestDto request = new SupportRequestDto(
                "Technical Issue",
                "Cannot load dashboard",
                "The dashboard hangs on loading projects for several minutes.",
                "Charuka",
                "user@example.com"
        );

        SupportResponseDto expected = new SupportResponseDto(
                true,
                "Your support request has been received. Ticket reference: SUP-ABC12345",
                "SUP-ABC12345",
                LocalDateTime.now()
        );

        given(supportService.submitSupportRequest(any(SupportRequestDto.class), any(Jwt.class)))
                .willReturn(expected);

        mockMvc.perform(post("/api/v1/support/requests")
                        .with(jwt().authorities(new SimpleGrantedAuthority("APP_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.ticketId").value("SUP-ABC12345"))
                .andExpect(jsonPath("$.message").value(expected.message()));
    }

    @Test
    void shouldRejectInvalidCategory() throws Exception {
        SupportRequestDto request = new SupportRequestDto(
                "NonExistentCategory",
                "Cannot load dashboard",
                "The dashboard hangs on loading projects for several minutes.",
                "Charuka",
                "user@example.com"
        );

        mockMvc.perform(post("/api/v1/support/requests")
                        .with(jwt().authorities(new SimpleGrantedAuthority("APP_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectShortMessage() throws Exception {
        SupportRequestDto request = new SupportRequestDto(
                "Technical Issue",
                "Help",
                "Too short", // under 10 chars
                "Charuka",
                "user@example.com"
        );

        mockMvc.perform(post("/api/v1/support/requests")
                        .with(jwt().authorities(new SimpleGrantedAuthority("APP_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn500InternalServerErrorWhenEmailSendingFails() throws Exception {
        SupportRequestDto request = new SupportRequestDto(
                "Technical Issue",
                "Delivery failure",
                "This will trigger an email dispatch error in service layer.",
                "Charuka",
                "user@example.com"
        );

        given(supportService.submitSupportRequest(any(SupportRequestDto.class), any(Jwt.class)))
                .willThrow(new EmailSendingException("SendGrid API error (403): Sender Identity unverified"));

        mockMvc.perform(post("/api/v1/support/requests")
                        .with(jwt().authorities(new SimpleGrantedAuthority("APP_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Sender Identity unverified")));
    }

    @Test
    void shouldReturn400BadRequestWhenConfigurationMissing() throws Exception {
        SupportRequestDto request = new SupportRequestDto(
                "Technical Issue",
                "Missing config",
                "Testing handling when recipient configuration is missing entirely.",
                "Charuka",
                "user@example.com"
        );

        given(supportService.submitSupportRequest(any(SupportRequestDto.class), any(Jwt.class)))
                .willThrow(new IllegalStateException("Support recipient email is not configured on the server."));

        mockMvc.perform(post("/api/v1/support/requests")
                        .with(jwt().authorities(new SimpleGrantedAuthority("APP_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Support recipient email is not configured")));
    }
}
