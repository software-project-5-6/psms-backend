package com.majstro.psms.backend.mapper;

import com.majstro.psms.backend.dto.ProjectDto;
import com.majstro.psms.backend.dto.ProjectWithUsersDto;
import com.majstro.psms.backend.dto.UserRoleDto;
import com.majstro.psms.backend.entity.Project;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProjectMapper {

    public ProjectDto toDto(Project project) {
        if (project == null) return null;

        return ProjectDto.builder()
                .id(project.getId())
                .projectName(project.getProjectName())
                .description(project.getDescription())
                .clientName(project.getClientName())
                .clientEmail(project.getClientEmail())
                .clientPhone(project.getClientPhone())
                .iconUrl(project.getIconUrl())
                .price(project.getPrice())
                .artifactCount(project.getArtifactCount())
                .userCount(project.getUserRoles() != null ? project.getUserRoles().size() : 0)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    public Project toEntity(ProjectDto dto) {
        if (dto == null) return null;

        return Project.builder()
                .id(dto.getId())
                .projectName(dto.getProjectName())
                .description(dto.getDescription())
                .clientName(dto.getClientName())
                .clientEmail(dto.getClientEmail())
                .clientPhone(dto.getClientPhone())
                .iconUrl(dto.getIconUrl())
                .price(dto.getPrice())
                .artifactCount(dto.getArtifactCount())
                .build();
    }

    public ProjectWithUsersDto toProjectWithUsersDto(Project project) {
        List<UserRoleDto> userRoles = project.getUserRoles().stream()
                .map(pur -> UserRoleDto.builder()
                        .userId(pur.getUser().getId())
                        .fullName(pur.getUser().getFullName())
                        .email(pur.getUser().getEmail())
                        .role(pur.getRole())
                        .build())
                .toList();

        return ProjectWithUsersDto.builder()
                .id(project.getId())
                .projectName(project.getProjectName())
                .description(project.getDescription())
                .clientName(project.getClientName())
                .clientEmail(project.getClientEmail())
                .clientPhone(project.getClientPhone())
                .iconUrl(project.getIconUrl())
                .price(project.getPrice())
                .artifactCount(project.getArtifactCount())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .assignedUsers(userRoles)
                .build();
    }
}