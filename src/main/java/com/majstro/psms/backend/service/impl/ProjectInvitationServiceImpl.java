package com.majstro.psms.backend.service.impl;

import com.majstro.psms.backend.dto.InviteRequest;
import com.majstro.psms.backend.dto.ProjectInvitationDTO;
import com.majstro.psms.backend.entity.*;
import com.majstro.psms.backend.exception.InvalidInvitationException;
import com.majstro.psms.backend.exception.ResourceAlreadyExistsException;
import com.majstro.psms.backend.exception.UnauthorizedAccessException;
import com.majstro.psms.backend.mapper.ProjectInvitationMapper;
import com.majstro.psms.backend.repository.ProjectInvitationRepository;
import com.majstro.psms.backend.repository.ProjectRepository;
import com.majstro.psms.backend.repository.ProjectUserRoleRepository;
import com.majstro.psms.backend.repository.UserRepository;
import com.majstro.psms.backend.service.IEmailService;
import com.majstro.psms.backend.service.IProjectInvitationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectInvitationServiceImpl implements IProjectInvitationService {

    private final ProjectRepository projectRepository;
    private final ProjectInvitationRepository invitationRepository;
    private final ProjectUserRoleRepository projectUserRoleRepository;
    private final UserRepository userRepository;
    private final IEmailService emailService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    @Transactional
    public void sendInvitation(String projectId, InviteRequest request, String inviterId) {
        // Validate role
        String roleUpper = request.role().toUpperCase();
        if (!isValidRole(roleUpper)) {
            throw new IllegalArgumentException("Invalid role: " + request.role() +
                ". Valid roles are: ADMIN, MANAGER, CONTRIBUTOR, VIEWER");
        }

        // Find project
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with ID: " + projectId));

        // Check if inviter is a member with permission (only if inviterId provided)
        if (inviterId != null) {
            User inviter = userRepository.findById(inviterId).orElse(null);
            if (inviter != null) {
                // APP_ADMIN can always invite users to any project
                if (!"APP_ADMIN".equals(inviter.getGlobalRole())) {
                    ProjectUserRole inviterRole = projectUserRoleRepository.findByProjectAndUser(project, inviter)
                            .orElseThrow(() -> new UnauthorizedAccessException("You are not a member of this project"));

                    // Only ADMIN and MANAGER can invite
                    if (inviterRole.getRole() != ProjectRole.ADMIN && inviterRole.getRole() != ProjectRole.MANAGER) {
                        throw new UnauthorizedAccessException("Only administrators and managers can send invitations");
                    }
                }
            }
        }

        // Check if user already exists and is a member
        String emailLower = request.email().toLowerCase().trim();
        User existingUser = userRepository.findByEmail(emailLower).orElse(null);
        if (existingUser != null) {
            boolean isMember = projectUserRoleRepository.existsByProjectAndUser(project, existingUser);
            if (isMember) {
                throw new ResourceAlreadyExistsException("User " + emailLower + " is already a member of this project");
            }
        }

        // Check for existing pending invitation
        List<ProjectInvitation> existingInvites = invitationRepository.findByProjectAndEmail(project, emailLower);
        for (ProjectInvitation invite : existingInvites) {
            if (invite.isPending()) {
                throw new ResourceAlreadyExistsException("A pending invitation already exists for " + emailLower);
            }
        }

        // Create invitation
        String token = UUID.randomUUID().toString();
        ProjectInvitation invitation = ProjectInvitation.builder()
                .project(project)
                .email(emailLower)
                .role(roleUpper)
                .token(token)
                .status("PENDING")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .invitedBy(inviterId)
                .build();

        invitationRepository.save(invitation);

        // Send email
        try {
            sendInvitationEmail(invitation, project);
        } catch (Exception e) {
            log.error("Failed to send invitation email: {}", e.getMessage());
            
        }

        log.info("Invitation sent to {} for project {}", emailLower, project.getProjectName());
    }

    @Override
    @Transactional
    public String acceptInvitation(String token, String userEmail) {
        ProjectInvitation invite = invitationRepository.findByToken(token)
                .orElseThrow(() -> new InvalidInvitationException("Invalid invitation link"));

        // Check status
        if (invite.isAccepted()) {
            throw new InvalidInvitationException("This invitation has already been accepted");
        }

        if ("REVOKED".equalsIgnoreCase(invite.getStatus())) {
            throw new InvalidInvitationException("This invitation has been revoked");
        }

        if ("EXPIRED".equalsIgnoreCase(invite.getStatus()) || invite.isExpired()) {
            throw new InvalidInvitationException("This invitation has expired");
        }

        // Check email match
        if (!invite.getEmail().equalsIgnoreCase(userEmail.trim())) {
            throw new InvalidInvitationException("This invitation was sent to " + invite.getEmail());
        }

        // Find user
        User user = userRepository.findByEmail(userEmail.trim())
                .orElseThrow(() -> new EntityNotFoundException("User account not found. Please sign up first."));

        // Get project
        Project project = projectRepository.findById(invite.getProject().getId())
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));

        //Check if already a member BEFORE making any database changes
        boolean isAlreadyMember = projectUserRoleRepository.existsByProjectAndUser(project, user);
        
        if (isAlreadyMember) {
            // User is already a member - this is an info message, not an error
            // Check if invitation was already marked as accepted
            if (!invite.isAccepted()) {
                invite.setStatus("ACCEPTED");
                invitationRepository.save(invite);
            }
            throw new ResourceAlreadyExistsException("You are already a member of this project");
        }

        // Add user to project (only if not already a member)
        try {
            ProjectRole role = ProjectRole.valueOf(invite.getRole());
            ProjectUserRole userRole = ProjectUserRole.builder()
                    .project(project)
                    .user(user)
                    .role(role)
                    .build();

            // Save user role FIRST
            projectUserRoleRepository.save(userRole);
            projectUserRoleRepository.flush(); // Force immediate write to DB

            // THEN mark invitation as accepted
            invite.setStatus("ACCEPTED");
            invitationRepository.save(invite);
            invitationRepository.flush(); // Force immediate write to DB

            log.info("User {} joined project {} with role {}", user.getEmail(), project.getProjectName(), role);
            return "Welcome to " + project.getProjectName() + "!";

        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Handle race condition - user was added between check and insert
            log.warn("User {} was already added to project {} (race condition)", user.getEmail(), project.getProjectName());
            invite.setStatus("ACCEPTED");
            invitationRepository.save(invite);
            throw new ResourceAlreadyExistsException("You are already a member of this project");
        } catch (Exception e) {
            log.error("Error accepting invitation: {}", e.getMessage(), e);
            throw new InvalidInvitationException("Failed to accept invitation: " + e.getMessage());
        }
    }


    @Override
    @Transactional(readOnly = true)
    public List<ProjectInvitationDTO> getPendingInvitations(String projectId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));

        List<ProjectInvitation> invitations = invitationRepository.findByProjectAndStatus(project, "PENDING");
        return ProjectInvitationMapper.toDtoList(invitations);
    }

    @Override
    @Transactional
    public void revokeInvitation(Long invitationId) {
        ProjectInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new EntityNotFoundException("Invitation not found"));

        if (!invitation.isPending()) {
            throw new InvalidInvitationException("Only pending invitations can be revoked");
        }

        invitation.setStatus("REVOKED");
        invitationRepository.save(invitation);

        log.info("Invitation {} revoked", invitationId);
    }

    @Override
    @Transactional
    public void resendInvitation(Long invitationId) {
        ProjectInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new EntityNotFoundException("Invitation not found"));

        if (!invitation.isPending()) {
            throw new InvalidInvitationException("Only pending invitations can be resent");
        }

        // Update expiration
        invitation.setExpiresAt(LocalDateTime.now().plusDays(7));
        invitationRepository.save(invitation);

        // Resend email
        try {
            Project project = projectRepository.findById(invitation.getProject().getId()).orElseThrow();
            sendInvitationEmail(invitation, project);
        } catch (Exception e) {
            log.error("Failed to resend email: {}", e.getMessage());
        }

        log.info("Invitation {} resent", invitationId);
    }

    @Override
    @Transactional
    public void expireOldInvitations() {
        List<ProjectInvitation> allInvitations = invitationRepository.findAll();
        int expired = 0;

        for (ProjectInvitation invitation : allInvitations) {
            if (invitation.isPending() && invitation.isExpired()) {
                invitation.setStatus("EXPIRED");
                expired++;
            }
        }

        if (expired > 0) {
            invitationRepository.saveAll(allInvitations);
            log.info("Expired {} invitations", expired);
        }
    }

    private void sendInvitationEmail(ProjectInvitation invitation, Project project) {
        String inviteLink = frontendUrl + "/invite/accept?token=" + invitation.getToken();
        String subject = "Invitation to join " + project.getProjectName();
        
        String htmlBody = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { 
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                        line-height: 1.6; 
                        color: #333;
                        margin: 0;
                        padding: 0;
                        background-color: #f4f4f4;
                    }
                    .container { 
                        max-width: 600px; 
                        margin: 40px auto; 
                        background: white;
                        border-radius: 12px;
                        overflow: hidden;
                        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
                    }
                    .header { 
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        color: white;
                        padding: 40px 30px;
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 28px;
                        font-weight: 600;
                    }
                    .content { 
                        padding: 40px 30px;
                        background: white;
                    }
                    .content h2 {
                        color: #333;
                        font-size: 24px;
                        margin-top: 0;
                    }
                    .button { 
                        display: inline-block;
                        padding: 14px 32px;
                        background: #667eea;
                        color: white;
                        text-decoration: none;
                        border-radius: 6px;
                        font-weight: 600;
                        margin: 20px 0;
                        transition: background 0.3s ease;
                    }
                    .button:hover {
                        background: #5568d3;
                    }
                    .role-badge { 
                        display: inline-block;
                        padding: 6px 16px;
                        background: #4caf50;
                        color: white;
                        border-radius: 20px;
                        font-weight: 600;
                        font-size: 14px;
                    }
                    .info-box { 
                        background: #f8f9fa;
                        border-left: 4px solid #667eea;
                        padding: 20px;
                        margin: 20px 0;
                        border-radius: 4px;
                    }
                    .info-box p {
                        margin: 8px 0;
                    }
                    .link-box {
                        background: #f8f9fa;
                        padding: 12px;
                        border-radius: 6px;
                        margin: 15px 0;
                        word-break: break-all;
                    }
                    .link-box code {
                        color: #667eea;
                        font-size: 13px;
                    }
                    .expiry-notice {
                        background: #fff3cd;
                        border-left: 4px solid #ffc107;
                        padding: 15px;
                        margin: 20px 0;
                        border-radius: 4px;
                        color: #856404;
                    }
                    .footer { 
                        text-align: center;
                        padding: 30px;
                        background: #f8f9fa;
                        color: #666;
                        font-size: 13px;
                        border-top: 1px solid #e0e0e0;
                    }
                    .footer p {
                        margin: 5px 0;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 Project Invitation</h1>
                    </div>
                    <div class="content">
                        <h2>Hi there! 👋</h2>
                        <p>You've been invited to join an exciting project and collaborate with the team!</p>
                        
                        <div class="info-box">
                            <p><strong>📁 Project:</strong> %s</p>
                            <p><strong>👤 Your Role:</strong> <span class="role-badge">%s</span></p>
                        </div>
                        
                        <p>Click the button below to accept the invitation and start collaborating:</p>
                        
                        <center>
                            <a href="%s" class="button">✓ Accept Invitation</a>
                        </center>
                        
                        <p style="color: #666; font-size: 14px; margin-top: 25px;">
                            Or copy and paste this link into your browser:
                        </p>
                        <div class="link-box">
                            <code>%s</code>
                        </div>
                        
                        <div class="expiry-notice">
                            <strong>⏰ Important:</strong> This invitation expires on <strong>%s</strong>
                        </div>
                    </div>
                    <div class="footer">
                        <p><strong>Project Management System</strong></p>
                        <p>If you didn't expect this invitation, you can safely ignore this email.</p>
                        <p style="margin-top: 15px; color: #999;">© 2026 All rights reserved</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            project.getProjectName(),
            invitation.getRole(),
            inviteLink,
            inviteLink,
            invitation.getExpiresAt().toLocalDate());

        emailService.sendHtmlEmail(invitation.getEmail(), subject, htmlBody);
    }

    private boolean isValidRole(String role) {
        try {
            ProjectRole.valueOf(role);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}