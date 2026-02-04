package com.majstro.psms.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder

@NoArgsConstructor       // <--- REQUIRED: Adds 'public ProjectInvitationDTO() {}'
@AllArgsConstructor      // <--- REQUIRED: Adds constructor for @Builder to work

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
