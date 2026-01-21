package com.majstro.psms.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.majstro.psms.backend.dto.InviteRequest;
import com.majstro.psms.backend.dto.ProjectInvitationDTO;
import com.majstro.psms.backend.service.IProjectInvitationService;
import com.majstro.psms.backend.service.IUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectInvitationController.class)
class ProjectInvitationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IProjectInvitationService invitationService;

    @MockBean
    private IUserService userService;

    @Test
    void shouldInviteUser_WhenValidRequest() throws Exception {
        // Arrange
        // Using a constructor for the Request DTO (assuming it's a Record or Class with AllArgs)
        InviteRequest request = new InviteRequest("proj-1", "test@example.com", "ROLE_VIEWER");

        String inviterId = "user-inviter-123";
        given(userService.getUserIdFromJwt(any())).willReturn(inviterId);

        // Act & Assert
        mockMvc.perform(post("/api/v1/invitations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Invitation sent successfully")));

        verify(invitationService).sendInvitation(eq("proj-1"), any(InviteRequest.class), eq(inviterId));
    }

    @Test
    void shouldAcceptInvite_WhenTokenIsValid() throws Exception {
        // Arrange
        String token = "valid-token-123";
        String email = "test@example.com";

        given(invitationService.acceptInvitation(token, email)).willReturn("Joined project successfully");

        // Act & Assert
        // We simulate the 'email' claim inside the JWT token
        mockMvc.perform(post("/api/v1/invitations/accept")
                        .param("token", token)
                        .with(jwt().jwt(builder -> builder.claim("email", email)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Joined project successfully"));
    }

    @Test
    void shouldGetPendingInvitations() throws Exception {
        // Arrange
        // FIX: Using Builder pattern correctly now that DTO is updated
        ProjectInvitationDTO invite1 = ProjectInvitationDTO.builder()
                .id(1L)
                .email("pending@test.com")
                .projectId("proj-1")
                .projectName("My Project")
                .role("EDITOR")
                .status("PENDING")
                .build();

        given(invitationService.getPendingInvitations("proj-1")).willReturn(List.of(invite1));

        // Act & Assert
        mockMvc.perform(get("/api/v1/invitations/project/proj-1")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].email").value("pending@test.com"))
                .andExpect(jsonPath("$[0].projectName").value("My Project"));
    }

    @Test
    void shouldRevokeInvitation() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/invitations/100")
                        .with(jwt())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Invitation revoked successfully"));

        verify(invitationService).revokeInvitation(100L);
    }

    @Test
    void shouldResendInvitation() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/invitations/100/resend")
                        .with(jwt())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Invitation resent successfully"));

        verify(invitationService).resendInvitation(100L);
    }
}