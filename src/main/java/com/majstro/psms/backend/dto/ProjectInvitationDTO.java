package com.majstro.psms.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProjectInvitationDTO {
    private Long id;
    private String email;
    private String role;
    private String status;
    private LocalDateTime expiresAt;
    private String projectId;
    private String projectName;
    private String invitedByName;
    private LocalDateTime createdAt;
}
