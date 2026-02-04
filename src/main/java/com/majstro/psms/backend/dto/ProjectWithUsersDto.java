package com.majstro.psms.backend.dto;

import lombok.*;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectWithUsersDto {
    private String id;
    private String projectName;
    private String description;
    private String clientName;
    private String clientEmail;
    private String clientPhone;
    private String iconUrl;
    private Integer artifactCount;
    private Instant createdAt;
    private Instant updatedAt;

    private List<UserRoleDto> assignedUsers;
}