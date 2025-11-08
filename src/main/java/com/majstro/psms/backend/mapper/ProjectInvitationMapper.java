package com.majstro.psms.backend.mapper;

import com.majstro.psms.backend.dto.ProjectInvitationDTO;
import com.majstro.psms.backend.entity.Project;
import com.majstro.psms.backend.entity.ProjectInvitation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
public class ProjectInvitationMapper {

    // ===== Entity → DTO =====
    public static ProjectInvitationDTO toDto(ProjectInvitation entity) {
        if (entity == null) {
            return null;
        }

        return ProjectInvitationDTO.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .role(entity.getRole())
                .status(entity.getStatus())
                .expiresAt(entity.getExpiresAt())
                .projectId(entity.getProject() != null ? entity.getProject().getId() : null)
                .projectName(entity.getProject() != null ? entity.getProject().getProjectName() : null)
                .build();
    }

    // ===== DTO → Entity =====
    public static ProjectInvitation toEntity(ProjectInvitationDTO dto) {
        if (dto == null) {
            return null;
        }

        ProjectInvitation entity = new ProjectInvitation();
        entity.setId(dto.getId());
        entity.setEmail(dto.getEmail());
        entity.setRole(dto.getRole());
        entity.setStatus(dto.getStatus());
        entity.setExpiresAt(dto.getExpiresAt());

        // Attach project reference (without fetching from DB)
        if (dto.getProjectId() != null) {
            Project project = new Project();
            project.setId(dto.getProjectId());
            entity.setProject(project);
        }

        return entity;
    }

    // ===== List Mappings =====
    public static List<ProjectInvitationDTO> toDtoList(List<ProjectInvitation> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(ProjectInvitationMapper::toDto)
                .collect(Collectors.toList());
    }

    public static List<ProjectInvitation> toEntityList(List<ProjectInvitationDTO> dtos) {
        if (dtos == null) {
            return List.of();
        }
        return dtos.stream()
                .map(ProjectInvitationMapper::toEntity)
                .collect(Collectors.toList());
    }
}
