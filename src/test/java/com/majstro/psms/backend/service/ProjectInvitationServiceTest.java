package com.majstro.psms.backend.service;

import com.majstro.psms.backend.dto.InviteRequest;
import com.majstro.psms.backend.entity.*;
import com.majstro.psms.backend.repository.ProjectInvitationRepository;
import com.majstro.psms.backend.repository.ProjectRepository;
import com.majstro.psms.backend.repository.ProjectUserRoleRepository;
import com.majstro.psms.backend.repository.UserRepository;
import com.majstro.psms.backend.service.impl.ProjectInvitationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectInvitationServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectInvitationRepository invitationRepository;
    @Mock
    private ProjectUserRoleRepository projectUserRoleRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private IEmailService emailService;

    @InjectMocks
    private ProjectInvitationServiceImpl invitationService;

    @BeforeEach
    void setUp() {
        // Manually inject the @Value("${app.frontend.url}") field
        ReflectionTestUtils.setField(invitationService, "frontendUrl", "http://localhost:3000");
    }

    @Test
    void shouldSendInvitation_WhenValidRequest() {
        // Arrange
        String projectId = "proj-1";
        String inviterId = "user-1";
        // FIX: Changed "EDITOR" to "CONTRIBUTOR" (Valid Role)
        InviteRequest request = new InviteRequest(projectId, "newuser@test.com", "CONTRIBUTOR");

        Project project = new Project();
        project.setId(projectId);
        project.setProjectName("Test Project");

        User inviter = new User();
        inviter.setId(inviterId);
        inviter.setGlobalRole("APP_USER");

        // Mock Inviter is an ADMIN of the project
        ProjectUserRole inviterRole = new ProjectUserRole();
        inviterRole.setRole(ProjectRole.ADMIN);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findById(inviterId)).thenReturn(Optional.of(inviter));
        when(projectUserRoleRepository.findByProjectAndUser(project, inviter)).thenReturn(Optional.of(inviterRole));
        when(userRepository.findByEmail("newuser@test.com")).thenReturn(Optional.empty()); // User doesn't exist yet

        // Act
        invitationService.sendInvitation(projectId, request, inviterId);

        // Assert
        verify(invitationRepository).save(any(ProjectInvitation.class));
        verify(emailService).sendEmail(eq("newuser@test.com"), anyString(), anyString());
    }

    @Test
    void shouldAcceptInvitation_WhenTokenIsValid() {
        // Arrange
        String token = "valid-token";
        String userEmail = "newuser@test.com";

        Project project = new Project();
        project.setId("proj-1");
        project.setProjectName("My Project");

        ProjectInvitation invitation = ProjectInvitation.builder()
                .token(token)
                .email(userEmail)
                .project(project)
                .role("CONTRIBUTOR") // FIX: Changed "EDITOR" to "CONTRIBUTOR"
                .status("PENDING")
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        User user = new User();
        user.setEmail(userEmail);

        when(invitationRepository.findByToken(token)).thenReturn(Optional.of(invitation));
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(projectRepository.findById("proj-1")).thenReturn(Optional.of(project));
        // Ensure user is NOT already a member
        when(projectUserRoleRepository.existsByProjectAndUser(project, user)).thenReturn(false);

        // Act
        String result = invitationService.acceptInvitation(token, userEmail);

        // Assert
        assertThat(result).contains("Welcome to My Project");
        verify(projectUserRoleRepository).save(any(ProjectUserRole.class));
        verify(invitationRepository).save(invitation); // Updates status to ACCEPTED
        assertThat(invitation.getStatus()).isEqualTo("ACCEPTED");
    }

    @Test
    void shouldThrowException_WhenRoleIsInvalid() {
        // Arrange
        InviteRequest request = new InviteRequest("p1", "test@test.com", "SUPER_KING"); // Invalid Role

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                invitationService.sendInvitation("p1", request, "u1")
        );
    }

    @Test
    void shouldThrowException_WhenInvitationExpired() {
        // Arrange
        ProjectInvitation invitation = ProjectInvitation.builder()
                .status("PENDING")
                .expiresAt(LocalDateTime.now().minusDays(1)) // Expired
                .build();

        when(invitationRepository.findByToken("expired-token")).thenReturn(Optional.of(invitation));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                invitationService.acceptInvitation("expired-token", "email@test.com")
        );
    }
}