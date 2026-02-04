package com.majstro.psms.backend.dto;

import com.majstro.psms.backend.entity.ProjectUserRole;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDto {

    private String id;
    private String projectName;
    private String description;
    private String clientName;
    private String clientEmail;
    private String clientPhone;
    private String iconUrl;
    private Integer artifactCount;
    private Integer userCount;
    private Instant createdAt;
    private Instant updatedAt;
}