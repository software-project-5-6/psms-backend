package com.majstro.psms.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProjectInvitationDTO {
    private Long id;  // Invitation ID remains Long (auto-increment)
    private String email;
    private String role;
    private String status;
    private LocalDateTime expiresAt;
    private String projectId;  // Changed to String for custom project ID
    private String projectName;
    private String invitedByName;  // Added to show who invited
    private LocalDateTime createdAt;  // Added to show when invited
}
